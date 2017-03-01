(ns cledgers.luminus.routes.services
  (:require [compojure.core :refer [defroutes POST]]
            [clojure.tools.logging :as log]
            [cledgers.luminus.utils :as utils]
            [cledgers.luminus.db.core :as db]
            [buddy.hashers :as hashers]))

(defroutes services-routes
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
              {:status 200
               :session (assoc session :identity username)}))))
  (POST "/api/bogus/" request (log/info (str "bogus: " (utils/pp request)))))
