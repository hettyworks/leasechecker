(ns hetty.leasechecker.core
  (:require [slack-rtm.core :as slack]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [clojure.core.async :as a]
            [clojure.java.io :as io]))


;;(def rtm-conn (slack/connect ""))

(defn capitalize-words
  [string]
  (->> (clojure.string/split (str string) #"\b")
    (map clojure.string/capitalize)
    clojure.string/join))

(defn load-cards []
  (->> (json/read-str (slurp (io/resource "eternal-cards.json"))
                      :key-fn keyword)
       (map (fn [{:keys [Name ImageUrl]}]
              [(str/lower-case Name) ImageUrl]))
       (into {})))

(defn build-link
  [card-name]
  ; ideally this would be a more robust API call
  ; it would make sense to see if the built link 404s or not before returning it
  (str "http://www.numotgaming.com.rsz.io/cards/images/cards/"
       (-> card-name
           capitalize-words
           (str/replace #" " "%20"))
       ".png?width=200"))

(defn build-link-2
  [card-name card-db]
  ;; ideally this would be a more robust API call
  ;; it would make sense to see if the built link 404s or not before returning it
  (get card-db (str/lower-case card-name)))

(defn write-to-channel
  [dispatcher channel msg]
  (slack/send-event dispatcher {:type "message"
                                :channel channel
                                :text msg}))

(defn process-msg
  [rtm-conn card-db]
  (fn [{:keys [text channel] :as data}]
    ;; right now it only matches the first card b/c capture groups confused me too much
    (let [pattern (re-pattern "(\\[\\[([^\\[\\]]+)\\]\\])")
          matcher (re-matcher pattern text)
          result (re-find matcher)]
      (when result
        (write-to-channel (:dispatcher rtm-conn) channel (build-link-2
                                                          (last result)
                                                          card-db))))))

(defn start
  []
  (let [rtm-conn (slack/connect "")
        events (:events-publication rtm-conn)
        card-db (load-cards)
        c (slack/sub-to-event events :message (process-msg rtm-conn card-db))]
    (loop []
      (a/<!! c)
      (recur))))
