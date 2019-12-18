(ns cledgers-luminus.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [cledgers-luminus.ajax :refer [load-interceptors!]]
            [cledgers-luminus.handlers]
            [cledgers-luminus.subscriptions]
            [cledgers-luminus.pages.login :as login-page]
            [accountant.core :as accountant]
            [cledgers-luminus.utils :as utils]
            [ajax.core :as ajax]
            [cljs-uuid-utils.core :as uuid]
            [cljs-time.core :as time]
            [cledgers-luminus.bulma-typeahead :as typeahead])
  (:import goog.History))

(defn navbar []
  [:nav.navbar {:role "navigation" :aria-label "main navigation"}
   [:div.navbar-brand
    [:div.navbar-item "cledgers-luminus"]
    [:a {:role "button" :class "navbar-burger burger" :aria-label "menu" :aria-expanded "false" :data-target "navbar-thing"}
     [:span {:aria-hidden true}]
     [:span {:aria-hidden true}]
     [:span {:aria-hidden true}]]]
   [:div.navbar-menu {:id "navbar-thing"}

    [:div.navbar-start
     [:div {:class "navbar-item has-dropdown is-hoverable"}
      [:a.navbar-link "User"]
      [:div.navbar-dropdown
       [:div.navbar-item
        {:on-click #(ajax/POST "/api/logout/"
                              :error-handler (fn [] (.log js/console "error: " (utils/pp %)))
                              :handler (fn [] (rf/dispatch [:logout nil])))}
       "Logout"]]]]]])


(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])




;; fdh


(defn dispatch-timer-event []
  (let [now (js/Date.)]
    (rf/dispatch [:timer now])))

(defn clock []
  [:div.example-clock
   {:style {:color @(rf/subscribe [:time-color])}}
   (let [time @(rf/subscribe [:time])]
     (if (not time)
       "oops"
       (-> time
           .toTimeString
           (clojure.string/split " ")
           first)))])

(defonce do-timer (js/setInterval dispatch-timer-event 1000))

(def last-date-used (atom (time/today)))

(defn empty-xaction [] {:uuid (str (uuid/make-random-uuid))
                        :date {:month (time/month @last-date-used)
                               :day (time/day @last-date-used)
                               :year (time/year @last-date-used)}
                        :description ""
                        :amount ""
                        :add-waiting true})

(def xactions (r/atom {}))

(defn xform-xaction-for-backend [xaction]
  (as-> xaction $
    (dissoc $ :add-waiting)))

(defn get-payees! [q-str callback]
  #_(println "callback:" callback)
  (ajax/GET "/api/payees"
            {:params {:q q-str}
             :handler callback
             :error-handler (fn [err]
                              (.log js/console "error: " (utils/pp err)))}))

(defn new-xaction-row []
  (let [new-xaction (r/atom (empty-xaction))]
    (fn []
      [:tr {:key "new-one"}
       [:td
        [:input {:type "text"
                 :size 2
                 :value (get-in @new-xaction [:date :month])
                 :on-change #(swap! new-xaction assoc-in [:date :month] (-> % .-target .-value))}]
        [:span "/"]
        [:input {:type "text"
                 :size 2
                 :value (get-in @new-xaction [:date :day])
                 :on-change #(swap! new-xaction assoc-in [:date :day] (-> % .-target .-value))}]
        [:span "/"]
        [:input {:type "text"
                 :size 4
                 :value (get-in @new-xaction [:date :year])
                 :on-change #(swap! new-xaction assoc-in [:date :year] (-> % .-target .-value))}]]
       [:td [typeahead/typeahead-component
             {:value (get-in @new-xaction [:payee :name])
              :query-func get-payees!
              :on-change (fn [selection]
                           (let [payee {:name (:value selection)
                                        :is-new (:is-new selection)}]
                             (swap! new-xaction assoc :payee payee)))}]]
       [:td [:input {:type "text"
                     :value (:description @new-xaction)
                     :on-change #(swap! new-xaction assoc :description (-> % .-target .-value))}]]
       [:td [:input {:type "text"
                     :value (:amount @new-xaction)
                     :on-change #(swap! new-xaction assoc :amount (-> % .-target .-value))}]]
       [:td [:button
             {:on-click
              (fn [evt]
                (let [xaction-to-add @new-xaction]
                  (reset! last-date-used (time/local-date
                                          (js/parseInt (get-in @new-xaction [:date :year]))
                                          (js/parseInt (get-in @new-xaction [:date :month]))
                                          (js/parseInt (get-in @new-xaction [:date :day]))))
                  (swap! xactions assoc (:uuid xaction-to-add) xaction-to-add)
                  (reset! new-xaction (empty-xaction))
                  ;; (.log js/console "new-xaction: " (utils/pp xaction-to-add))
                  (ajax/POST "/api/xactions/"
                             {:params {:xaction (xform-xaction-for-backend xaction-to-add)}
                              :error-handler
                              (fn [err]
                                (.log js/console "error: " (utils/pp err))
                                ;; (.log js/console "xactions before dissoc: " (utils/pp @xactions))
                                (swap! xactions dissoc (:uuid xaction-to-add))
                                ;; (.log js/console "xactions after dissoc: " (utils/pp @xactions))
                                )
                              :handler
                              (fn [response]
                                (.log js/console "success adding xaction")
                                (cljs.pprint/pprint response))})))
              }
             "Add"]]])))


(defn home-page []
  [:div.container
   [:div.row>div.col-sm-12
    [:div "Hello world, it is now"]
    [clock]]
   [:div.row>div.col-sm-12
    [:table.table
     [:thead
      [:tr
       [:th "date"]
       [:th "payee"]
       [:th "desc"]
       [:th "amount"]
       [:th "controls"]]]
     [:tbody
      [new-xaction-row]
      (for [[_ xaction] @xactions]
        (let [;; _ (.log js/console "xaction: " (utils/pp xaction))
              class (when (:add-waiting xaction)
                      "rowhighlight")]
          [:tr {:key (:id xaction)
                :class class}
           [:td (str (:date xaction))]
           [:td (get-in xaction [:payee :name])]
           [:td (:description xaction)]
           [:td (:amount xaction)]]))]]]])






(defn luminus-home-page []
  [:div.container
   (when-let [docs @(rf/subscribe [:docs])]
     [:div.row>div.col-sm-12
      [:div {:dangerouslySetInnerHTML
             {:__html (md->html docs)}}]])])

(def pages
  {:home #'home-page
   :lum-home #'luminus-home-page
   :about #'about-page
   ;; :login #'login-page/login-page
   })

(defn page []
  (let [user @(rf/subscribe [:user])]
    ;; (.log js/console "user: " (utils/pp user))
    (if-not user
      [login-page/login-page]
      [:div
       [navbar]
       [(pages @(rf/subscribe [:page]))]
       [:div.container "dater"
        [:ul
         [:li "page: " @(rf/subscribe [:page])]
         [:li "user: " user]]]])))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (.log js/console "home route called")
  (rf/dispatch [:set-active-page :home])
  ;; (let [user @(rf/subscribe [:user])]
  ;;   (do
  ;;     (.log js/console "user" (utils/pp user))
  ;;     (if-not user
  ;;       (rf/dispatch [:navigate "#/login"])
  ;;       (rf/dispatch [:set-active-page :home]))))
  )

(secretary/defroute "/luminus-home" []
  (rf/dispatch [:set-active-page :lum-home]))

(secretary/defroute "/about" []
  (.log js/console "about route called")
  (rf/dispatch [:set-active-page :about]))

;; (secretary/defroute "/login" []
;;   (.log js/console "login route called")
;;   (rf/dispatch [:set-active-page :login]))


;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        ;; (.log js/console "some kind of navigate event happening: " event)
        (secretary/dispatch! (.-token event))))
    (.setEnabled true))

  ;; (accountant/configure-navigation!
  ;;   {:nav-handler
  ;;    (fn [path]
  ;;      (.log js/console "accountant navigating to " path)
  ;;      (secretary/dispatch! path))
  ;;    :path-exists?
  ;;    (fn [path]
  ;;      (secretary/locate-route path))})
  ;; (accountant/dispatch-current!)
  )

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
