(ns reddit.clj.core
  "Reddit client for clojure"
  (:require [clj-http.client :as client])
  (:require [clojure.contrib.json :as json])
  (:require [clojure.contrib.string :as string])
  (:import (java.net URLEncoder)))

;(defrecord RedditItem [author clicked created created_utc
;                       domain downs hidden id is_self levenshtein
;                       likes media media_embed name num_comments
;                       over_18 permalink  saved score selftext
;                       selftext_html subreddit subreddit_id thumbnail
;                       title ups url])
;
;(defrecord RedditUser [name link_karma comment_karma created created_utc
;                       has_mail has_mod_mail id is_gold is_mod])
;
;(defrecord RedditComment [author body body_html created created_utc
;                          downs id levenshtein likes link_id name 
;                          parent_id replies subreddit subreddit_id ups])

(defn- urlopen [url cookie] 
  (let [response (client/get url {:headers {"Cookie" cookie}})]
    (if (= 200 (:status response))
      (:body response)
      nil)))

(defn- asjson [input]
  (if (nil? input) nil
    (json/read-json input)))

(defn- build-subreddit-url
  [rname rcount since]
    (str "http://www.reddit.com" 
      (if-not (nil? rname) (str "/r/" rname))
      "/.json?" 
      (if-not (nil? since) (str "after=" since))
      (and since rcount "&")
      (if-not (nil? rcount) (str "count=" rcount))))

(defn- build-user-url
  [user qualifier rcount since]
    (str "http://www.reddit.com/user/" user
      (if-not (nil? qualifier) (str "/" qualifier))
      "/.json"
      (if-not (nil? since) (str "after=" since))
      (and since rcount "&")
      (if-not (nil? rcount) (str "count=" rcount))))

(defn- build-comments-url
  [reddit_id]
    (str "http://www.reddit.com/comments/" reddit_id "/.json"))

(defn- build-savedreddit-url 
  [rcount since]
  (str "http://www.reddit.com/saved/.json"
      (if-not (nil? since) (str "after=" since))
      (and since rcount "&")
      (if-not (nil? rcount) (str "count=" rcount))))

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
  ([rname cookie] (subreddit rname cookie nil nil))
  ([rname cookie rcount] (subreddit rname cookie rcount nil))
  ([rname cookie rcount since]
    (parse-reddits 
      (asjson 
        (urlopen 
          (build-subreddit-url rname rcount since) cookie)))))

(defn userreddit "Get user reddits"
  ([user cookie] (userreddit user cookie nil nil nil))
  ([user cookie qualifier] (userreddit user cookie qualifier nil nil))
  ([user cookie qualifier rcount] (userreddit user cookie qualifier rcount nil))
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

(defprotocol RedditProtocol
  "The reddit web API interfaces"
  (reddits [this rname rcount after] "Retrieve reddits from subreddit")
  (user [this user qualifier rcount after] "Retrieve reddits related by user")
  (comments [this reddit-id] "Retrieve comments for a reddit")
  (domain [this domain-name rcount after] "Retrieve reddits under a domain")
  (saved [this rcount after] "Retrieve saved reddits")
  (info [this url] "Retrieve url information from reddit")
  (me [this] "Retrieve user information according to current credential")
  (mine [this] "Retrieve subcribed subreddits according to current credential "))

(defrecord RedditClient [credential]
  RedditProtocol
    (reddits [this rname rcount after] (subreddit rname credential rcount after))
    (user [this user qualifier rcount after] (userreddit user credential qualifier rcount after))
    (comments [this reddit-id] (redditcomments reddit-id credential))
    (saved [this rcount after] (savedreddits credential rcount after)))

