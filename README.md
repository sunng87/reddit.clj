# reddit.clj

A reddit api wrapper for clojure

Note that this project is no longer maintained. Feel free to fork it and continue.

## Usage

Declare reddit.clj in your project.clj

    (defproject xxxx "1.0.0-SNAPSHOT"
      :dependencies [[reddit.clj "0.4.0"]])

Use reddit.clj in your clojure code:

``` clojure
;; include reddit.clj
(require '[reddit.clj.core :as reddit])

;; create a reddit client with reddit/login.
;; you can also pass nil as username and password to use it
;; anonymously
(def rc (reddit/login "your-reddit-name" "your-reddit-passwd"))

;; load reddits from subreddit "clojure", and print titles
(doseq 
  [title (map :title (reddit/reddits rc "clojure"))] 
  (println title))

;; vote-up a thing on reddit
(reddit/vote-up rc "t3_iz61z")

;; you may also submit links to reddit.
;; permalink will be returned when success.
;; be careful to use this API because reddit may ask you for a 
;; captcha. But as a library, reddit.clj will return nil on this case.
(reddit/submit-link rc "title" "url" "subreddit-name")
```

Check detailed API document [here](http://sunng87.github.com/reddit.clj/)

## License

Copyright (C) 2011 Sun Ning

Distributed under the Eclipse Public License, the same as Clojure.
