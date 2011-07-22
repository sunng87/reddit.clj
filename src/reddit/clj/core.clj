(ns reddit.clj.core
  "Reddit client for clojure"
  (:require [reddit.clj.client :as client])
)

(defn login "Login to reddit, return cookie as user credential"
  [user passwd]
  (client/login user passwd))

(defprotocol RedditChannels
  "The reddit web API interfaces for reading data from reddit"
  (reddits 
    [this rname] [this rname rcount after]
    "Retrieve reddits from subreddit")
  (user 
    [this user] [this user qualifier] [this user qualifier rcount after]
    "Retrieve reddits related by user")
  (about
    [this user]
    "Retrieve user information")
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
  
(defprotocol RedditOperations  
  "The reddit web API interfaces for writing data into reddit"
  (vote-up
    [this id]
    "Vote up a comment or post")
  (vote-down
    [this id]
    "Vote down a comment or post")
  (rescind-vote
    [this id]
    "Rescind vote to a comment or post")
  (add-comment
    [this id text]
    "Comment on a post or comment")
  (save
    [this id]
    "Add a post to your saved reddits")
  (unsave
    [this id]
    "Remove a post from your saved reddits")
  (submit-link
    "Submit a link to particular subreddit"
    [this title sr url])
  (submit-text
    "Submit a self post to particular subreddit"
    [this title sr text]))

(defrecord RedditClient [credential]
  RedditChannels
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
      (client/domainreddits domain-name credential rcount after))
    (info [this url] 
      (client/info url credential))
    (mine [this]
      (client/mine credential))
    (me [this]
      (client/me credential)))

