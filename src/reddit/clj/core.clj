(ns reddit.clj.core
  "Reddit client for clojure"
  (:require [clj-http.client :as client])
  (:require [clojure.contrib.json :as json])
  (:require [clojure.contrib.string :as string])
  (:import (java.net URLEncoder)))

(defrecord RedditItem [author clicked created created_utc
                       domain downs hidden id is_self levenshtein
                       likes media media_embed name num_comments
                       over_18 permalink  saved score selftext
                       selftext_html subreddit subreddit_id thumbnail
                       title ups url])

(defrecord RedditUser [name link_karma comment_karma created created_utc
                       has_mail has_mod_mail id is_gold is_mod])

(defrecord RedditComment [author body body_html created created_utc
                          downs id levenshtein likes link_id name 
                          parent_id replies subreddit subreddit_id ups])

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

(defn- postdata [data]
  (string/join "&"
    (map
      #(str (string/as-str (key %)) 
            "=" (URLEncoder/encode (val %) "utf8")) data)))

(defn- create-reddit-item [r]
  (merge (struct-map RedditItem) r))

(defn- parse-reddits [resp]
  (map :data (:children (:data resp))))

(defn login "Login to reddit" [user passwd]
  (let [resp (client/post "http://www.reddit.com/api/login"
    {
      :body (postdata {:user user :passwd passwd})
      :content-type "application/x-www-form-urlencoded"
    })]
    (if (= (:status resp) 200) 
      (let [cookie (get (:headers resp) "set-cookie")]
        (if-not (nil? (re-find #"reddit_session" cookie)) cookie)))))

(defn subreddit "Get subreddit items"
  ([rname cookie] (subreddit rname cookie nil nil))
  ([rname cookie rcount] (subreddit rname cookie rcount nil))
  ([rname cookie rcount since]
    (parse-reddits 
      (asjson 
        (urlopen 
          (build-subreddit-url rname rcount since) cookie)))))


