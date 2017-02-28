(ns cledgers.luminus.pages.login
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [cledgers.luminus.utils :as utils]))


(defn login [evt]
  ;; (.log js/console "login is getting called")
  (.log js/console "logging in")
  (ajax/POST "/api/login/"
             {:params {:userid "testing" :password "testing"}
              :error-handler #(.log js/console "error" (utils/pp %))
              :handler #(rf/dispatch [:login {:username "testing"}])})
  ;; (rf/dispatch [:login {:username "testing"}])
  )


(defn login-page []
  [:div.container
   [:button {:on-click login} "login"]])
