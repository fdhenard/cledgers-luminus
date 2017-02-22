(ns cledgers.luminus.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [cljs-uuid-utils.core :as uuid]
            [cledgers.luminus.ajax :refer [load-interceptors!]]
            [cledgers.luminus.handlers]
            [cledgers.luminus.subscriptions])
  (:import goog.History))

(defn nav-link [uri title page collapsed?]
  (let [selected-page (rf/subscribe [:page])]
    [:li.nav-item
     {:class (when (= page @selected-page) "active")}
     [:a.nav-link
      {:href uri
       :on-click #(reset! collapsed? true)} title]]))

(defn navbar []
  (r/with-let [collapsed? (r/atom true)]
    [:nav.navbar.navbar-dark.bg-primary
     [:button.navbar-toggler.hidden-sm-up
      {:on-click #(swap! collapsed? not)} "☰"]
     [:div.collapse.navbar-toggleable-xs
      (when-not @collapsed? {:class "in"})
      [:a.navbar-brand {:href "#/"} "cledgers.luminus"]
      [:ul.nav.navbar-nav
       [nav-link "#/" "Home" :home collapsed?]
       [nav-link "#/about" "About" :about collapsed?]]]]))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])




;; fdh

(defn pp [derta]
  (with-out-str (cljs.pprint/pprint derta)))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   (.log js/console "here!")
   {:time (js/Date.)
    :time-color "#f88"
    :xaction-editing {:date ""
                      :description ""
                      :amount ""}
    :xactions {}}))

(rf/reg-event-db
 :timer
 (fn [db [_ new-time]]
   (assoc db :time new-time)))

(rf/reg-event-db
 :xaction-editing-change-description
 (fn [db [_ new-description-value]]
   ;; (.log js/console (str "new val = " new-description-value))
   (assoc-in db [:xaction-editing :description] new-description-value)))

(rf/reg-event-db
 :xaction-editing-change-amount
 (fn [db [_ new-amount-value]]
   (assoc-in db [:xaction-editing :amount] new-amount-value)))

(rf/reg-event-db
 :xaction-editing-change-date
 (fn [db [_ new-date-value]]
   (assoc-in db [:xaction-editing :date] new-date-value)))

(rf/reg-event-db
 :add-xaction
 (fn [db _]
   (let [new-id (str (uuid/make-random-uuid))]
     ;; (.log js/console "db" (pp db))
     (assoc-in db [:xactions new-id] (merge {:id new-id} (:xaction-editing db))))))

(defn dispatch-timer-event []
  (let [now (js/Date.)]
    (rf/dispatch [:timer now])))

(rf/reg-sub
 :time
 (fn [db _]
   (-> db :time)))

(rf/reg-sub
 :time-color
 (fn [db _]
   (:time-color db)))

(rf/reg-sub
 :xaction-editing-description
 (fn [db _]
   (get-in db [:xaction-editing :description])))

(rf/reg-sub
 :xaction-editing-amount
 (fn [db _]
   (get-in db [:xaction-editing :amount])))

(rf/reg-sub
 :xaction-editing-date
 (fn [db _]
   (get-in db [:xaction-editing :date])))

(rf/reg-sub
 :xactions
 (fn [db _]
   ;; (.log js/console (str "db = " (pp db)))
   (get db :xactions)))

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
       [:th "desc"]
       [:th "amount"]
       [:th "controls"]]]
     [:tbody
      [:tr {:key "new-one"}
       [:td [:input {:type "text"
                     :value @(rf/subscribe [:xaction-editing-date])
                     :on-change #(rf/dispatch [:xaction-editing-change-date (-> % .-target .-value)])}]]
       [:td [:input {:type "text"
                     :value @(rf/subscribe [:xaction-editing-description])
                     :on-change #(rf/dispatch [:xaction-editing-change-description (-> % .-target .-value)])
                     }]]
       [:td [:input {:type "text"
                     :value @(rf/subscribe [:xaction-editing-amount])
                     :on-change #(rf/dispatch [:xaction-editing-change-amount (-> % .-target .-value)])}]]
       [:td [:button {:on-click (fn [evt] (rf/dispatch [:add-xaction]))} "Add"]]]
      (for [xaction (map #(get % 1) @(rf/subscribe [:xactions]))]
        [:tr {:key (:id xaction)}
         [:td (pp xaction)]])]]]])






(defn luminus-home-page []
  [:div.container
   (when-let [docs @(rf/subscribe [:docs])]
     [:div.row>div.col-sm-12
      [:div {:dangerouslySetInnerHTML
             {:__html (md->html docs)}}]
      ])])

(def pages
  {:home #'home-page
   :lum-home #'luminus-home-page
   :about #'about-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/luminus-home" []
  (rf/dispatch [:set-active-page :lum-home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))


;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

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
