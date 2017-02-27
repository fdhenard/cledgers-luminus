(ns cledgers.luminus.login
  (:require [re-frame.core :as rf]))


(defn login [evt]
  (rf/dispatch [:login {:username "testing"}]))


(defn login-page []
  [:div.container
   [:button {:on-click login} "login"]])
