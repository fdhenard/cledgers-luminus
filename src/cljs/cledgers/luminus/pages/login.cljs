(ns cledgers.luminus.pages.login
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [cledgers.luminus.utils :as utils]
            [reagent.core :as r]))


(defn login [username password]
  ;; (.log js/console "login is getting called")
  (.log js/console "logging in")
  (ajax/POST "/api/login/"
             {:params {:username @username :password @password}
              :error-handler #(.log js/console "error" %)
              :handler #(rf/dispatch [:login {:username @username}])})
  ;; (rf/dispatch [:login {:username "testing"}])
  )

(defonce temp-uname "frank")

(defn login-page []
  (let [username (r/atom "")
        passwd (r/atom "")]
    (fn []
      [:div.container
       [:input {:type "text"
                :value @username
                :on-change #(reset! username (-> % .-target .-value))}]
       [:input {:type "password"
                :value (or @passwd "")
                :on-change #(reset! passwd (-> % .-target .-value))}]
       [:button {:on-click #(login username passwd)} "login"]])))
