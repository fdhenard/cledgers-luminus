(ns cledgers.luminus.pages.login
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [cledgers.luminus.utils :as utils]
            [reagent.core :as r]))

(defn login [username password]
  (.log js/console "logging in")
  (ajax/POST "/api/login/"
             {:params {:username @username :password @password}
              :error-handler #(.log js/console "error" (utils/pp %))
              :handler #(do
                          ;; (.log js/console "res:" (utils/pp %))
                          (rf/dispatch [:login %]))}))

(defn login-page []
  (let [username (r/atom "")
        passwd (r/atom "")]
    (fn []
      [:div.container
       [:input {:type "text"
                :value @username
                :on-change #(reset! username (-> % .-target .-value))}]
       [:input {:type "password"
                :value @passwd
                :on-change #(reset! passwd (-> % .-target .-value))}]
       [:button {:on-click #(login username passwd)} "login"]])))
