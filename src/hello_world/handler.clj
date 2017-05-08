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

(defn find-card
	[message]
  (def pattern (re-pattern "(\\[\\[([^\\[\\]]+)\\]\\])"))
  (def matcher (re-matcher pattern message))
  (def result (re-find matcher))
  (if result
    {:status 200
      :body {:text (str "You checked: " result "[]")}
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
