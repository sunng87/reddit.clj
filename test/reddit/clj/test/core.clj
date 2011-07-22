(ns reddit.clj.test.core
  (:use [reddit.clj.core])
  (:use [clojure.test]))

(def testuser "redditclj")
(def testpasswd "redditclj")

(def r (login testuser testpasswd))

(deftest test-login
  (is (not (nil? r))))

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

(deftest test-me
  (let [userinfo (me r)]
    (is (= testuser (:name userinfo)))))

(deftest test-mine
  (let [rdts (mine r)]
    (is (<= 1 (count rdts)))))

(def wr (enhance r))

(deftest test-votes
  (is (true? (vote-up wr "t3_iwt49")))
  (is (true? (vote-down wr "t3_iwt49"))))
