
(ns cledgers.luminus.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [cledgers.luminus.ajax :refer [load-interceptors!]]
            [cledgers.luminus.handlers]
            [cledgers.luminus.subscriptions]
            [cledgers.luminus.pages.login :as login-page]
            [accountant.core :as accountant]
            [cledgers.luminus.utils :as utils]
            [ajax.core :as ajax]
            [cledgers.luminus.bootstrap :as bs])
  (:import goog.History))

(defn nav-link [uri title page collapsed?]
  (let [selected-page (rf/subscribe [:page])]
    [:li.nav-item
     {:class (when (= page @selected-page) "active")}
     [:a.nav-link
      {:href uri
       :on-click #(reset! collapsed? true)} title]]))

;; (defn navbar []
;;   (r/with-let [collapsed? (r/atom true)]
;;     [:nav.navbar.navbar-dark.bg-primary
;;      [:button.navbar-toggler.hidden-sm-up
;;       {:on-click #(swap! collapsed? not)} "☰"]
;;      [:div.collapse.navbar-toggleable-xs
;;       (when-not @collapsed? {:class "in"})
;;       [:a.navbar-brand {:href "#/"} "cledgers.luminus"]
;;       [:ul.nav.navbar-nav
;;        [nav-link "#/" "Home" :home collapsed?]
;;        [nav-link "#/about" "About" :about collapsed?]]]]))

(defn navbar []
  [bs/Navbar {:class "navbar-inverse"}
   [bs/Navbar.Header
    [bs/Navbar.Brand
     [:a {:href "#"} "cledgers.luminus"]]
    [bs/Navbar.Toggle]]
   [bs/Navbar.Collapse
    [bs/Nav
     [bs/NavItem {:href "#/"} "Home"]
     [bs/NavItem {:href "#/about"} "About"]
     [bs/NavDropdown {:title "User" :id "user-dropdown"}
      [bs/MenuItem
       {:on-click #(ajax/POST "/api/logout/"
                              :error-handler (fn [] (.log js/console "error: " (utils/pp %)))
                              :handler (fn [] (rf/dispatch [:logout nil])))}
       "Logout"]]]]])


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

(defn empty-xaction [] {:date ""
                        :description ""
                        :amount ""})

(defn new-xaction-row []
  (let [new-xaction (r/atom (empty-xaction))]
    (fn []
      [:tr {:key "new-one"}
       [:td [:input {:type "text"
                     :value (:date @new-xaction)
                     :on-change #(swap! new-xaction assoc :date (-> % .-target .-value))}]]
       [:td [:input {:type "text"
                     :value (:description @new-xaction)
                     :on-change #(swap! new-xaction assoc :description (-> % .-target .-value))}]]
       [:td [:input {:type "text"
                     :value (:amount @new-xaction)
                     :on-change #(swap! new-xaction assoc :amount (-> % .-target .-value))}]]
       [:td [:button
             {:on-click
              (fn [evt]
                (rf/dispatch [:add-xaction @new-xaction])
                (reset! new-xaction (empty-xaction)))}
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
       [:th "desc"]
       [:th "amount"]
       [:th "controls"]]]
     [:tbody
      [new-xaction-row]
      (for [xaction (map #(get % 1) @(rf/subscribe [:xactions]))]
        [:tr {:key (:id xaction)}
         [:td (:date xaction)]
         [:td (:description xaction)]
         [:td (:amount xaction)]])]]]])






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
         [:li "user: " user]]]
       ])))

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
