(ns cledgers.luminus.routes.services
  (:require [compojure.core :refer [defroutes POST]]
            [clojure.tools.logging :as log]
            [cledgers.luminus.utils :as utils]))

(defroutes services-routes
  (POST "/api/login/" request
        (do (log/info (str "request: " (utils/pp request)))
            {:status 200
             }))
  (POST "/api/bogus/" request (log/info (str "bogus: " request))))
