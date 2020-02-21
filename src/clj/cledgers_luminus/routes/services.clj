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
        (jdbc/with-db-transaction [tx-conn db/*db*]
          (let [user-id (get-in request [:session :identity :id])
                xaction (-> request :params :xaction)
                payee (:payee xaction)
                ledger (:ledger xaction)
                payee-id
                (if-not (:is-new payee)
                  (:id payee)
                  (let [payee (assoc payee :created-by-id user-id)
                        create-res (db/create-payee! tx-conn payee)]
                    (:id create-res)))
                ledger-id
                (if-not (:is-new ledger)
                  (:id ledger)
                  (let [ledger (assoc ledger :created-by-id user-id)
                        create-res (db/create-ledger! tx-conn ledger)]
                    (:id create-res)))
                new-date (time/local-date
                          (get-in xaction [:date :year])
                          (get-in xaction [:date :month])
                          (get-in xaction [:date :day]))
                updated-xaction (-> xaction
                                    (dissoc :payee)
                                    (dissoc :ledger)
                                    (merge {:date new-date
                                            :amount (-> xaction :amount bigdec)
                                            :created-by-id user-id
                                            :payee-id payee-id
                                            :ledger-id ledger-id}))
                _ (log/debug "new-xaction:" (utils/pp updated-xaction))
                #_ (log/debug (str "xactions post request:\n"
                                   (utils/pp {:request request})))
                _ (db/create-xaction! tx-conn updated-xaction)]
            {:status 200})))

  (GET "/api/payees" request
       (let [q-parm (-> request :params :q)
             result (get-payees q-parm)]
         {:status 200
          :body {:result result}}))

  (GET "/api/ledgers" request
       (let [q-parm (get-in request [:params :q])
             result
             (let [the-hql
                   {:select [:id :name]
                    :from [:ledger]
                    :where [:like :name (str q-parm "%")]
                    :limit 10}
                   results
                   (jdbc/query db/*db* (sql/format the-hql))]
               results)]
         {:status 200
          :body {:result result}}))


  )
