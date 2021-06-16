(ns cledgers-luminus.core
  (:require [cledgers-luminus.handler :as handler]
            [luminus.repl-server :as repl]
            [luminus.http-server :as http]
            #_[luminus-migrations.core :as migrations]
            [cledgers-luminus.config :refer [env]]
            [cider.nrepl :refer [cider-nrepl-handler]]
            #_[clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [mount.core :as mount]
            #_[declarative-ddl.migrator.core :as migrator]
            #_[cledgers-luminus.entities :as entities]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate ^{:on-reload :noop}
                http-server
                :start
                (http/start
                  (-> env
                      (assoc :handler (handler/app))
                      (update :port #(or (-> env :options :port) %))))
                :stop
                (http/stop http-server))

(mount/defstate ^{:on-reload :noop}
                repl-server
                :start
                (when-let [nrepl-port (env :nrepl-port)]
                  (repl/start {:port nrepl-port :handler cider-nrepl-handler}))
                :stop
                (when repl-server
                  (repl/stop repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (cli/parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

#_(def cli-options
  [[nil "--dry-run" "Dry Run"]])

(defn -main [& args]
  (let [{:keys [_options _arguments _errors _summary]} (cli/parse-opts args cli-options)]
    (start-app args)
    ;; (if (and (= 1 (count arguments))
    ;;          (#{"make-migration" "migrate"} (first arguments)))
    ;;   (let [action (first arguments)]
    ;;     (case action
    ;;       "make-migration"
    ;;       (do
    ;;         ;; (mount/start #'cledgers-luminus.config/env)
    ;;         (migrator/make-migration-file! entities/entities)
    ;;         (System/exit 0))
    ;;       "migrate"
    ;;       (do
    ;;         (mount/start #'cledgers-luminus.config/env)
    ;;         (migrator/migrate! (:database-url env) :dry-run (:dry-run options))
    ;;         (System/exit 0))))
    ;;   (start-app args))
    ))
  
