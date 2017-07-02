(defproject leasechecker "0.1.0-SNAPSHOT"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :resource-paths ["resources"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.taoensso/timbre "4.10.0"]
                 [slack-rtm "0.1.6"]
                 [amperity/envoy "0.3.1"]
                 [org.clojure/core.async "0.3.443"]]
  :plugins []
  :main hetty.leasechecker.core
  :profiles
  {:dev {:dependencies []}})
