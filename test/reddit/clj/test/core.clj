(ns reddit.clj.test.core
  (:use [reddit.clj.core])
  (:use [clojure.test])
  (:import [reddit.clj.core RedditClient]))

(def testuser "redditclj")
(def testpasswd "redditclj")

(deftest test-login
  (let [credential (login testuser testpasswd)]
    (is (re-find #"reddit_session" credential))))

(def r (RedditClient. (login testuser testpasswd)))

(deftest test-subreddits
  (let [rdts (reddits r "Clojure")]
    (is (= 25 (count rdts)))
    (is (= "Clojure" (:subreddit (first rdts))))))

(deftest test-domainreddits
  (let [rdts (domain r "sunng.info")]
    (is (< 0 (count rdts)))
    (is (= "sunng.info" (:domain (first rdts))))))

(deftest test-info
  (let [rdts (info r "http://sunng.info/blog/2011/06/jip-embed-on-the-fly-classpath-resolution-for-jython/")]
    (is (< 0 (count rdts)))
    (is (= "sunng.info" (:domain (first rdts))))))