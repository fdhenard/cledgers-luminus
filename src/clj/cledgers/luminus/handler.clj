(ns cledgers.luminus.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [cledgers.luminus.layout :refer [error-page]]
            [cledgers.luminus.routes.home :refer [home-routes]]
            [cledgers.luminus.routes.services :refer [login-service-route services-routes]]
            [compojure.route :as route]
            [cledgers.luminus.env :refer [defaults]]
            [mount.core :as mount]
            [cledgers.luminus.middleware :as middleware]
            [clojure.tools.logging :as log]
            [cledgers.luminus.utils :as utils]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
   (-> #'home-routes
       (wrap-routes (fn [handler] (fn [request]
                                    (log/info (str "request: " (utils/pp request)))
                                    (handler request))))
       ;; (wrap-routes middleware/wrap-restricted)
       (wrap-routes middleware/wrap-csrf)
       (wrap-routes middleware/wrap-formats))
   (-> #'login-service-route
       (wrap-routes middleware/wrap-formats))
   (-> #'services-routes
       (wrap-routes middleware/wrap-restricted)
       (wrap-routes middleware/wrap-formats))
   (route/not-found
    (:body
     (error-page {:status 404
                  :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
