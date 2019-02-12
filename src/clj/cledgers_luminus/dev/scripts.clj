(ns cledgers-luminus.dev.scripts
  (:require [cledgers-luminus.db.core :as db]
            [buddy.hashers :as hashers]
            [cledgers-luminus.utils :as utils]))

(defn create-user-s! [& {:keys [username first-name last-name email pass is-admin? is-active?],
                         :as u-map
                         }]
  (println (str "u-map = " (utils/pp u-map)))
  (db/create-user! {:username username
                    :first_name first-name
                    :last_name last-name
                    :email email
                    :is_admin is-admin?
                    :is_active is-active?
                    :pass (hashers/derive pass)}))
