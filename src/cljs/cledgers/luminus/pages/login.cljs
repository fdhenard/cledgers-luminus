(ns cledgers.luminus.pages.login
  (:require [re-frame.core :as rf]))


(defn login [evt]
  (.log js/console "login is getting called")
  (rf/dispatch [:login {:username "testing"}]))


(defn login-page []
  [:div.container
   [:button {:on-click login} "login"]])
