(ns reddit.clj.core
  "Reddit client for clojure"
  (:require [reddit.clj.client :as client])
)

(defn login "Login to reddit, return cookie as user credential"
  [user passwd]
  (client/login user passwd))

(defprotocol RedditProtocol
  "The reddit web API interfaces"
  (reddits 
    [this rname] [this rname rcount after]
    "Retrieve reddits from subreddit")
  (user 
    [this user] [this user qualifier] [this user qualifier rcount after]
    "Retrieve reddits related by user")
  (comments 
    [this reddit-id] 
    "Retrieve comments for a reddit")
  (domain 
    [this domain-name] [this domain-name rcount after]
    "Retrieve reddits under a domain")
  (saved 
    [this] [this rcount after]
    "Retrieve saved reddits")
  (info 
    [this url] 
    "Retrieve url information from reddit")
  (me 
    [this] 
    "Retrieve user information according to current credential")
  (mine 
    [this] 
    "Retrieve subcribed subreddits according to current credential "))

(defrecord RedditClient [credential]
  RedditProtocol
    (reddits [this rname] 
      (client/subreddit rname credential nil nil))
    (reddits [this rname rcount after] 
      (client/subreddit rname credential rcount after))
    (user [this user] 
      (client/userreddit user credential nil nil nil))
    (user [this user qualifier] 
      (client/userreddit user credential qualifier nil nil))
    (user [this user qualifier rcount after] 
      (client/userreddit user credential qualifier rcount after))
    (comments [this reddit-id] 
      (client/redditcomments reddit-id credential))
    (saved [this] 
      (client/savedreddits credential nil nil))
    (saved [this rcount after]
      (client/savedreddits credential rcount after))
    (domain [this domain-name] 
      (client/domainreddits domain-name credential nil nil))
    (domain [this domain-name rcount after] 
      (client/domainreddits domain-name credential rcount after)))

