(ns user
  (:require [mount.core :as mount]
            [cledgers.luminus.figwheel :refer [start-fw stop-fw cljs]]
            cledgers.luminus.core))

(defn start []
  (mount/start-without #'cledgers.luminus.core/http-server
                       #'cledgers.luminus.core/repl-server))

(defn stop []
  (mount/stop-except #'cledgers.luminus.core/http-server
                     #'cledgers.luminus.core/repl-server))

(defn restart []
  (stop)
  (start))


