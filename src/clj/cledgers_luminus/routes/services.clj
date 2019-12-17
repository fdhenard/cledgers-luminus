(ns cledgers-luminus.routes.services
  (:require [compojure.core :refer [defroutes POST GET]]
            [clojure.tools.logging :as log]
            [cledgers-luminus.utils :as utils]
            [cledgers-luminus.db.core :as db]
            [buddy.hashers :as hashers]
            [clojure.java.jdbc :as jdbc]
            [java-time :as time]
            [honeysql.core :as sql]))

(defroutes public-service-routes
  (POST "/api/login/" request
        (do
          ;; (log/info (str "request: " (utils/pp {:request request})))
          (let [params (-> request :params)
                username (-> params :username)
                user (db/get-user-by-uname {:username username})
                session (-> request :session)]
            ;; (log/info (str "user: " (utils/pp user)))
            (if-not (hashers/check (:password params) (:pass user))
              {:status 403}
              (let [user-res (dissoc user :pass)]
                {:status 200
                 :session (assoc session :identity user-res)
                 :body user-res}))))))

(defn get-payees [q]
  (let [the-hsql {:select [:id :name]
                  :from [:payee]
                  :where [:like :name (str q "%")]
                  :limit 10}
        results (jdbc/query db/*db* (sql/format the-hsql))]
    results))

(defroutes services-routes
  (POST "/api/logout/" request {:status 200
                                :session (dissoc (:session request) :identity)})
  (POST "/api/xactions/" request
        (let [xaction (-> request :params :xaction)
              new-date (time/local-date
                        (get-in xaction [:date :year])
                        (get-in xaction [:date :month])
                        (get-in xaction [:date :day]))
              updated-xaction (-> xaction
                                  (assoc :date new-date)
                                  (assoc :amount (-> xaction :amount bigdec))
                                  (assoc :created-by-id (get-in request [:session :identity :id])))
              _ (println "new-xaction:" (utils/pp updated-xaction))]
          (log/info (str "xactions post request:\n" (utils/pp {:request request})))
          ;; (jdbc/insert! db/*db* :xaction updated-xaction)
          (db/create-xaction! updated-xaction)
          {:status 200}))
  (GET "/api/payees" request
       (let [q-parm (-> request :params :q)
             result (get-payees q-parm)]
         {:status 200
          :body {:result result}})))
