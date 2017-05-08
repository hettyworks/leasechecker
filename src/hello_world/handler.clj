; (ns hello-world.handler
;   (:require [compojure.core :refer :all]
;             [compojure.route :as route]
;             [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
;             [ring.middleware.json :as json]
;             [ring.util.response :refer [response]]))

; (defroutes app-routes
;   (GET "/" request 
;   	(let [name (or (get-in request [:params :name])
;                    (get-in request [:body :name])
;                    "John Doe")]
;       {:status 200
;        :body {:name name
;        :desc (str "The name you sent to me was " name)}}))
;   (POST "/check" [] "Checked")
;   (route/not-found "Not Found"))

; (def app
;   (wrap-defaults app-routes api-defaults))

; (def app
;   (-> app-routes api-defaults
;       (json/wrap-json-body {:keywords? true})
;       (json/wrap-json-response)))

(ns hello-world.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [compojure.route :as route]))

(require 'clojure.string)

(defn capitalize-words
  [string]
  (->> (clojure.string/split (str string) #"\b")
    (map clojure.string/capitalize)
    clojure.string/join))

(defn build-link
  [cardname]
  ; ideally this would be a more robust API call
  ; it would make sense to see if the built link 404s or not before returning it
  (str "http://www.numotgaming.com.rsz.io/cards/images/cards/" (clojure.string/replace (capitalize-words cardname) #" " "%20") ".png?width=200")
)

(defn find-card
	[message]
  ; right now it only matches the first card b/c capture groups confused me too much
  (def pattern (re-pattern "(\\[\\[([^\\[\\]]+)\\]\\])"))
  (def matcher (re-matcher pattern message))
  (def result (re-find matcher))
  (if result
    {:status 200
      :body {:text (build-link (last result))} ; primitive way of getting the right capture group since I don't understand regex really
    }
    {}
  )
)

(defn handle-response
	[from message]
	(if (not= (compare from "slackbot") 0)
    (find-card message)
  )
)

(defroutes app-routes
  (POST "/" request
  	(let [from (get-in request[:params :user_name]) message (get-in request[:params :text])]
	  	(handle-response from message)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))
