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

; (defn find-cards
; 	[]
; 	(re-seq #"\[\[(.*?)\]\]" "I used [[big dog]] and [[little dog]]"))

(defn handle-response
	[from]
	(println (compare from "slackbot"))
	(if (not= (compare from "slackbot") 0)
	  {:status 200
	   :body {:text "You just got checked."}
	   }))

(defroutes app-routes
  (POST "/" request
  	(let [from (get-in request[:params :user_name])]
  		(println from)
	  	(handle-response from)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))
