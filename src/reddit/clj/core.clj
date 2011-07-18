(ns reddit.clj.core
  "Reddit client for clojure"
  (:require [clj-http.client :as client])
  (:require [clojure.contrib.json :as json]))

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

(defn- urlopen [url] 
  (let [response (client/get url)]
    (if (= 200 (:status response))
      (:body response)
      nil)))

(defn- asjson [input]
  (if (nil? input) nil
    (json/read-json input)))

(defn- build-subreddit-url
  [rname rcount since]
    (str "http://www.reddit.com/r/" rname "/.json?" 
      (if-not (nil? since) (str "after=" since))
      (and since rcount "&")
      (if-not (nil? rcount) (str "count=" rcount))))

(defn- create-reddit-item [r]
  (merge (struct-map RedditItem) r))

(defn- parse-reddits [resp]
  (:children (:data) resp))

(defn subreddit "Get subreddit items"
  ([rname] (subreddit rname nil nil))
  ([rname rcount] (subreddit rname rcount nil))
  ([rname rcount since]
    (parse-reddits 
      (asjson 
        (urlopen 
          (build-subreddit-url rname rcount since))))))

