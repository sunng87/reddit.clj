(ns reddit.clj.client  
  "DO NOT use forms under this ns."
  (:require [clj-http.client :as client])
  (:require [clojure.string :as string])
  (:import [java.net URLEncoder]))

(defn- post-data [data]
  (string/join "&"
    (map
      #(str (name (key %)) 
            "=" (URLEncoder/encode (str (val %)) "utf8")) data)))

(defn- urlopen [url cookie] 
  (let [response (client/get url
                             {:headers {"User-Agent" "reddit.clj"}
                              :cookies cookie
                              :as :json})]
    (if (= 200 (:status response))
      (:body response)
      nil)))

(defn- urlpost [url data cookie]
  (let [response 
    (client/post url 
                 {:headers {"User-Agent" "reddit.clj"}
                  :cookies cookie
                  :content-type "application/x-www-form-urlencoded"
                  :body (post-data data)
                  :as :json})]
    (if (= 200 (:status response)) response)))

(defn- build-pagination-param
  [rcount since]
    (str
      "?"
      (and since (str "after=" since))
      (and since rcount "&")
      (and rcount (str "count=" rcount))))

(defn- build-subreddit-url
  [rname qualifier rcount since]
    (str "http://www.reddit.com" 
      (and rname (str "/r/" rname "/"))
      (str qualifier "/")
      ".json" 
      (build-pagination-param rcount since)))

(defn- build-user-url
  [user qualifier rcount since]
    (str "http://www.reddit.com/user/" user
      (and qualifier (str "/" qualifier))
      "/.json"
      (build-pagination-param rcount since)))

(defn- build-comments-url
  [reddit_id]
    (str "http://www.reddit.com/comments/" reddit_id "/.json"))

(defn- build-savedreddit-url 
  [rcount since]
  (str "http://www.reddit.com/saved/.json"
    (build-pagination-param rcount since)))

(defn- build-domain-reddits-url
  [domain-name rcount since]
    (str "http://www.reddit.com/domain/" domain-name
      "/.json"
      (build-pagination-param rcount since)))  

(defn- parse-reddits [resp]
  (map :data (:children (:data resp))))

(defn- parse-comment-replies [comment]
  (assoc comment :replies
    (if-let [replies (:replies comment)] 
      (map parse-comment-replies (parse-reddits replies)))))

(defn- parse-comment [comment-root]
  (map parse-comment-replies (parse-reddits comment-root)))

(defn- parse-comments [resp]
  (vector 
    (parse-reddits (first resp)) 
    (parse-comment (last resp))))

(defn login "Login to reddit" [user passwd]
  (let [resp (urlpost 
              (str "http://www.reddit.com/api/login/" user)
              {:user user :passwd passwd :api_type "json"} nil)
        cookie (:cookies resp)
        result (:body resp)
        resultmap (:json result)]
    (if (empty? (:errors resultmap))
      {:modhash (:modhash (:data resultmap)) :cookies cookie})))

(defn savedreddits "Get current users' saved reddits"
  [cookie rcount since]
    (parse-reddits
     (urlopen
      (build-savedreddit-url rcount since) cookie)))

(defn subreddit "Get subreddit items"
  ([rname qualifier cookie rcount since]
    (parse-reddits 
     (urlopen 
      (build-subreddit-url rname qualifier rcount since) cookie))))

(defn userreddit "Get user reddits"
  ([user cookie qualifier rcount since]
    (parse-reddits 
     (urlopen 
      (build-user-url user qualifier rcount since) cookie))))

(defn redditcomments "Get comments for a reddit"
  ([reddit-id cookie] 
    (parse-comments
     (urlopen
      (build-comments-url reddit-id) cookie))))

(defn domainreddits "Get reddits from specific domain"
  ([domain-name cookie rcount since] 
    (parse-reddits
     (urlopen
      (build-domain-reddits-url domain-name rcount since) cookie))))

(defn info "Find information about a url in reddit"
  ([url cookie]
    (parse-reddits 
     (urlopen 
      (str "http://www.reddit.com/api/info.json?url=" (URLEncoder/encode url)) cookie))))

(defn mine "Load user's subscribed subreddits"
  ([cookie]
    (parse-reddits
     (urlopen "http://www.reddit.com/reddits/mine.json?limit=100" cookie))))

(defn me "Load current user information"
  ([cookie]
    (:data
     (urlopen "http://www.reddit.com/api/me.json" cookie))))

(defn- post-success? [response]
  (if (nil? response)
    false
    (empty? (:body response))))

(defn vote [id value uh cookie]
  (post-success? (urlpost "http://www.reddit.com/api/vote"
    {:id id :dir value :uh uh} cookie)))

(defn add-comment [id text uh cookie]
  (post-success? (urlpost "http://www.reddit.com/api/comment"
    {:thing_id id :text text :uh uh} cookie)))

(defn save [id uh cookie]
  (post-success? (urlpost "http://www.reddit.com/api/save"
    {:id id :uh uh} cookie)))

(defn unsave [id uh cookie]
  (post-success? (urlpost "http://www.reddit.com/api/unsave"
    {:id id :uh uh} cookie)))

(defn hide [id uh cookie]
  (post-success? (urlpost "http://www.reddit.com/api/hide"
    {:id id :uh uh} cookie)))

(defn unhide [id uh cookie]
  (post-success? (urlpost "http://www.reddit.com/api/unhide"
    {:id id :uh uh} cookie)))

(defn submit [kind title sr content uh cookie]
    (let [result 
      (-> (:body
        (let [params {:title title :kind kind :sr sr :r sr :uh uh}]
          (urlpost "http://www.reddit.com/api/submit" 
            (case kind
              "link" (assoc params :url content)
              "self" (assoc params :text content))
            cookie)))
        last last last last first)]
      (if-not (nil? result) 
        (re-matches #"^http:\/\/www\.reddit\.com\/.*" result))))

(defn messages [mailbox cookie]
  (parse-reddits
   (urlopen
    (str "http://www.reddit.com/message/" mailbox "/.json") cookie)))

