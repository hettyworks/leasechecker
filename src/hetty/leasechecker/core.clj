(ns hetty.leasechecker.core
  (:require [slack-rtm.core :as slack]
            [clojure.string :as str]))

(def rtm-conn (slack/connect ""))

(defn capitalize-words
  [string]
  (->> (clojure.string/split (str string) #"\b")
    (map clojure.string/capitalize)
    clojure.string/join))

(defn build-link
  [card-name]
  ; ideally this would be a more robust API call
  ; it would make sense to see if the built link 404s or not before returning it
  (str "http://www.numotgaming.com.rsz.io/cards/images/cards/"
       (-> card-name
           capitalize-words
           (str/replace #" " "%20"))
       ".png?width=200"))

(defn write-to-channel
  [channel msg]
  (slack/send-event (:dispatcher rtm-conn) {:type "message"
                                            :channel channel
                                            :text msg}))

(defn process-msg
  [{:keys [text channel] :as data}]
  ;; right now it only matches the first card b/c capture groups confused me too much
  (println "got this:" data)
  (let [pattern (re-pattern "(\\[\\[([^\\[\\]]+)\\]\\])")
        matcher (re-matcher pattern text)
        result (re-find matcher)]
    (when result
      (write-to-channel channel (build-link (last result))))))

(def events (:events-publication rtm-conn))
(slack/sub-to-event events :message process-msg)
