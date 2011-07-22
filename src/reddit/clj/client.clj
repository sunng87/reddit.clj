(ns reddit.clj.client  
  (:require [clj-http.client :as client])
  (:require [clojure.contrib.json :as json])
  (:require [clojure.contrib.string :as string])
  (:import (java.net URLEncoder)))

(defn- urlopen [url cookie] 
  (let [response (client/get url {:headers {"Cookie" cookie "User-Agent" "reddit.clj"}})]
    (if (= 200 (:status response))
      (:body response)
      nil)))

(defn- asjson [input]
  (if (nil? input) nil
    (json/read-json input)))

(defn- build-pagination-param
  [rcount since]
    (str
      (if-not (nil? since) (str "after=" since))
      (and since rcount "&")
      (if-not (nil? rcount) (str "count=" rcount))))

(defn- build-subreddit-url
  [rname rcount since]
    (str "http://www.reddit.com" 
      (if-not (nil? rname) (str "/r/" rname))
      "/.json?" 
      (build-pagination-param rcount since)))

(defn- build-user-url
  [user qualifier rcount since]
    (str "http://www.reddit.com/user/" user
      (if-not (nil? qualifier) (str "/" qualifier))
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

(defn- postdata [data]
  (string/join "&"
    (map
      #(str (string/as-str (key %)) 
            "=" (URLEncoder/encode (val %) "utf8")) data)))

(defn- parse-reddits [resp]
  (map :data (:children (:data resp))))

(defn- parse-comments [resp]
  (map :data (:children (:data (nth resp 1)))))

(defn login "Login to reddit" [user passwd]
  (let [resp (client/post "http://www.reddit.com/api/login"
    {
      :body (postdata {:user user :passwd passwd})
      :content-type "application/x-www-form-urlencoded"
      :headers {"User-Agent" "reddit.clj"}
    })]
    (if (= (:status resp) 200) 
      (let [cookie (get (:headers resp) "set-cookie")]
        (if-not (nil? (re-find #"reddit_session" cookie)) cookie)))))

(defn savedreddits "Get current users' saved reddits"
  [cookie rcount since]
    (parse-reddits
      (asjson
        (urlopen
          (build-savedreddit-url rcount since) cookie))))

(defn subreddit "Get subreddit items"
  ([rname cookie rcount since]
    (parse-reddits 
      (asjson 
        (urlopen 
          (build-subreddit-url rname rcount since) cookie)))))

(defn userreddit "Get user reddits"
  ([user cookie qualifier rcount since]
    (parse-reddits 
      (asjson 
        (urlopen 
          (build-user-url user qualifier rcount since) cookie)))))

(defn redditcomments "Get comments for a reddit"
  ([reddit-id cookie] 
    (parse-comments
      (asjson
        (urlopen
          (build-comments-url reddit-id) cookie)))))

(defn domainreddits "Get reddits from specific domain"
  ([domain-name cookie rcount since] 
    (parse-reddits
      (asjson
        (urlopen
          (build-domain-reddits-url domain-name rcount since) cookie)))))

(defn info "Find information about a url in reddit"
  ([url cookie]
    ))

