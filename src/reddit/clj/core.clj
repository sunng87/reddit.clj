(ns reddit.clj.core
  "High level reddit API wrapper for clojure."
  (:require [reddit.clj.client :as client])
)

(defprotocol RedditChannels
  ^{
      :private true
      :doc "The reddit web API interfaces for reading data from reddit"
     }
  (reddits 
    [this rname] [this rname rcount after]
    "Retrieve reddits from subreddit")
  (reddits-new
    [this rname] [this rname rcount after]
    "Retrieve reddits from subreddit, section *new*")
  (reddits-controversial
    [this rname] [this rname rcount after]
    "Retrieve reddits from subreddit, section *controversial*")
  (reddits-top
    [this rname] [this rname rcount after]
    "Retrieve reddits from subreddit, section *hot*")    
  (user 
    [this user] [this user rcount after]
    "Retrieve reddits related by user")
  (user-comments
    [this user] [this user rcount after]
    "Retrieve comments submitted by user")
  (user-submitted
    [this user] [this user rcount after]
    "Retrieve links submitted by user")
  (user-liked
    [this user] [this user rcount after]
    "Retrieve things liked by user")
  (user-disliked
    [this user] [this user rcount after]
    "Retrieve thing disliked by user")
  (user-hidden
    [this user] [this user rcount after]
    "Retrieve links hide by user")
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
    "Retrieve subcribed subreddits according to current credential ")
  (message-inbox
    [this]
    "Retrieve messages from inbox")
  (message-sent
    [this]
    "Retrieve messages from outbox"))
 
(defprotocol RedditOperations  
  ^{
      :private true
      :doc "The reddit web API interfaces for writing data into reddit"
   }
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
    [this title url sr]
    "Submit a link to particular subreddit")
  (submit-text
    [this title text sr]
    "Submit a self post to particular subreddit")
  (hide
    [this id]
    "Hide a post")
  (unhide
    [this id]
    "Unhide a post"))

(defrecord RedditClient [credential]
  RedditChannels
    (reddits [this rname]
      (client/subreddit rname nil credential nil nil))
    (reddits [this rname rcount after] 
      (client/subreddit rname nil credential rcount after))
    (reddits-new [this rname]
      (client/subreddit rname "new" credential nil nil))
    (reddits-new [this rname rcount after] 
      (client/subreddit rname "new" credential rcount after))
    (reddits-controversial [this rname]
      (client/subreddit rname "controversial" credential nil nil))
    (reddits-controversial [this rname rcount after] 
      (client/subreddit rname "controversial" credential rcount after))
    (reddits-top [this rname]
      (client/subreddit rname "top" credential nil nil))
    (reddits-top [this rname rcount after] 
      (client/subreddit rname "top" credential rcount after))
    (user [this user] 
      (client/userreddit user credential nil nil nil))
    (user [this user rcount after] 
      (client/userreddit user credential nil rcount after))
    (user-comments [this user] 
      (client/userreddit user credential "comments" nil nil))
    (user-comments [this user rcount after] 
      (client/userreddit user credential "comments" rcount after))
    (user-submitted [this user] 
      (client/userreddit user credential "submitted" nil nil))
    (user-submitted [this user rcount after] 
      (client/userreddit user credential "submitted" rcount after))
    (user-liked [this user] 
      (client/userreddit user credential "liked" nil nil))
    (user-liked [this user rcount after] 
      (client/userreddit user credential "liked" rcount after))
    (user-disliked [this user] 
      (client/userreddit user credential "disliked" nil nil))
    (user-disliked [this user rcount after] 
      (client/userreddit user credential "disliked" rcount after))
    (user-hidden [this user] 
      (client/userreddit user credential "hidden" nil nil))
    (user-hidden [this user rcount after] 
      (client/userreddit user credential "hidden" rcount after))
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
      (client/me credential))
    (message-inbox [this]
      (client/messages "inbox" credential))
    (message-sent [this]
      (client/messages "sent" credential)))


(defn login "Login to reddit, return cookie as user credential"
  ([] (RedditClient. nil))
  ([user passwd]
  (if (nil? user) (RedditClient. nil)
    (RedditClient. (client/login user passwd)))))

(defn enhance [r]
  (assoc r :modhash (:modhash (me r))))
  
(extend-type RedditClient
  RedditOperations
    (vote-up [this id] 
      (client/vote id "1" (:modhash this) (:credential this)))
    (vote-down [this id] 
      (client/vote id "-1" (:modhash this) (:credential this)))
    (rescind-vote [this id] 
      (client/vote id "0" (:modhash this) (:credential this)))
    (add-comment [this id text] 
      (client/add-comment id text (:modhash this) (:credential this)))
    (save [this id]
      (client/save id (:modhash this) (:credential this)))
    (unsave [this id] 
      (client/unsave id (:modhash this) (:credential this)))
    (submit-link [this title url sr] 
      (client/submit "link" title sr url  (:modhash this) (:credential this)))
    (submit-text [this title text sr] 
      (client/submit "text" title sr text (:modhash this) (:credential this)))
    (hide [this id]
      (client/hide id  (:modhash this) (:credential this)))
    (unhide [this id]
      (client/unhide id  (:modhash this) (:credential this))))

(defn thing-type "test thing type with name" [name]
  (if-not (nil? name)
    (let [type-idx (nth (re-matches #"^t(\d)_.*" name) 1)]
      (if-not (nil? type-idx)
        (nth
          ["comment" "account" "link" "message" "subreddit"]
          (- (Integer. type-idx) 1))))))
