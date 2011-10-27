(defproject reddit.clj "0.3.1"
  :description "A reddit API wrapper for clojure"
  :author "Sun Ning <classicning@gmail.com>"
  :dependencies [[org.clojure/clojure "1.2.1"], 
                 [clj-http "0.2.1"],
                 [org.clojure/data.json "0.1.1"]]
  :dev-dependencies [[lein-clojars "0.6.0"]
                     [org.clojars.weavejester/autodoc "0.9.0"
                       :exclusions [
                         org.clojure/clojure-contrib
                         org.clojure/clojure]]])
