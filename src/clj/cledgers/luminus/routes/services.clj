(ns cledgers.luminus.routes.services
  (:require [compojure.core :refer [defroutes POST]]
            [clojure.tools.logging :as log]
            [cledgers.luminus.utils :as utils]
            [cledgers.luminus.db.core :as db]
            [buddy.hashers :as hashers]))

(defroutes login-service-route
  (POST "/api/login/" request
        (do
          ;; (log/info (str "request: " (utils/pp {:request request})))
          (let [params (-> request :params)
                username (-> params :username)
                user (db/get-user-by-uname {:username username})
                session (-> request :session)]
            (log/info (str "user: " (utils/pp user)))
            (if-not (hashers/check (:password params) (:pass user))
              {:status 403}
              (let [user-res (dissoc user :pass)]
                {:status 200
                 :session (assoc session :identity user-res)
                 :body user-res}))))))

(defroutes services-routes
  (POST "/api/logout/" request {:status 200
                                :session (dissoc (:session request) :identity)}))
