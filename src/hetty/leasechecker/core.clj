(ns hetty.leasechecker.core
  (:require [clojure.core.async :as a]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [envoy.core :as env :refer [defenv env]]
            [slack-rtm.core :as slack]))

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

(defn cleanse
  [word]
  (str/replace word "," ""))

(defn word-card-map
  [cards]
  (->> (mapcat
        (fn [[card-name data]]
          (loop [parts (str/split card-name #" ")
                 prev nil
                 result []]
            (if (zero? (count parts))
              result
              (let [curr-word (cleanse (first parts))
                    full-word (->> (filter (comp not nil?) [prev curr-word])
                                   (str/join " "))
                    result (conj result
                                 [curr-word data])
                    ;; we don't want the full words
                    ;; and we don't want the first word twice
                    result (if (and (not (nil? prev))
                                    (not (empty? (rest parts))))
                             (conj result [full-word data])
                             result)]
                (recur (rest parts) full-word result))))) cards)
       (group-by first)
       (map (fn [[word data]] [word (map second data)]))
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
  (get card-db (str/lower-case card-name)))

(defn get-words
  [word-map words]
  (when-let [result (get word-map words)]
    (rand-nth result)))

(defn build-link-3
  [card-name card-db word-map]
  (let [lower-name (str/lower-case card-name)]
   (if-let [data (get card-db lower-name)]
     data
     (get-words word-map lower-name))))

(defn write-to-channel
  [dispatcher channel msg]
  (future
    (slack/send-event dispatcher {:type "message"
                                  :channel channel
                                  :text msg})))

(defn process-msg
  [dispatcher* card-db word-map]
  (fn [{:keys [text channel] :as data}]
    (log/info "process-msg:" data)
    (try
      ;; right now it only matches the first card b/c capture groups confused me
      ;; too much
     (let [pattern (re-pattern "(\\[\\[([^\\[\\]]+)\\]\\])")
           matcher (re-matcher pattern text)
           result (re-find matcher)]
       (when result
         (log/info "Found possible card:" result)
         (write-to-channel @dispatcher* channel (build-link-3
                                                 (last result)
                                                 card-db
                                                 word-map))
         (log/info "Finished process-msg")))
     (catch Exception e
       (log/error e "Failed to process")))))

(defenv :slack-api-token
  "Slack RTM API token"
  :type :string
  :missing :abort)

(defn start
  []
  (let [card-db (load-cards)
        word-map (word-card-map card-db)
        dispatcher* (atom nil)
        {:keys [events-publication dispatcher start]}
        (slack/connect
         (:slack-api-token env)
         :on-close (fn [{:keys [status reason]}]
                     (log/error "Received :on-close" status reason))
         :message (process-msg dispatcher* card-db word-map))]
    (reset! dispatcher* dispatcher)
    (log/info "Connection established: " start)
    (loop []
      ;; todo recover failed connections
      (Thread/sleep 100000)
      (recur))))

(defn -main
  [& args]
  (start))
