(defproject reddit.clj "0.1.2"
  :description "A reddit API wrapper for clojure"
  :dependencies [[org.clojure/clojure "1.2.1"], 
                 [clj-http "0.1.3"],
                 [org.clojure/clojure-contrib "1.2.0"]]
  :dev-dependencies [[lein-clojars "0.6.0"]
                     [org.clojars.weavejester/autodoc "0.9.0"
                       :exclusions [
                         org.clojure/clojure-contrib
                         org.clojure/clojure]]])
