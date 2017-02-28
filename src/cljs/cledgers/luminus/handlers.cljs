(ns cledgers.luminus.handlers
  (:require [cledgers.luminus.db :as db]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx]]
            [cljs-uuid-utils.core :as uuid]
            [cledgers.luminus.utils :as utils]
            [accountant.core :as accountant]
            ))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))


(defn make-empty-xaction []
  {:date ""
   :description ""
   :amount ""})

;; (reg-event-db
;;  :initialize
;;  (fn [_ _]
;;    (.log js/console "here!")
;;    {:time (js/Date.)
;;     :time-color "#f88"
;;     :xaction-editing (make-empty-xaction)
;;     :xactions {}
;;     :user nil}))

(reg-event-db
 :timer
 (fn [db [_ new-time]]
   (assoc db :time new-time)))

(reg-event-db
 :xaction-editing-change-description
 (fn [db [_ new-description-value]]
   ;; (.log js/console (str "new val = " new-description-value))
   (assoc-in db [:xaction-editing :description] new-description-value)))

(reg-event-db
 :xaction-editing-change-amount
 (fn [db [_ new-amount-value]]
   (assoc-in db [:xaction-editing :amount] new-amount-value)))

(reg-event-db
 :xaction-editing-change-date
 (fn [db [_ new-date-value]]
   (assoc-in db [:xaction-editing :date] new-date-value)))

(reg-event-db
 :add-xaction
 (fn [db _]
   (let [new-id (str (uuid/make-random-uuid))]
     ;; (.log js/console "db" (utils/pp db))
     (-> db
         (assoc-in [:xactions new-id] (merge {:id new-id} (:xaction-editing db)))
         (assoc :xaction-editing (make-empty-xaction))))))

;; (reg-event-db
;;  :login
;;  (fn [db [_ user]]
;;    (.log js/console "logging in user" (utils/pp user))
;;    (-> db
;;        (assoc :user user)
;;        (assoc :page :home))))

;; (reg-fx
;;  :navigate
;;  (fn [value]
;;    (accountant/navigate! value)))

(reg-event-fx
 :navigate
 (fn [cofx [_ path]]
   (accountant/navigate! path)
   {}))

(reg-event-fx
 :login
 (fn [cofx [_ user]]
   {:db (assoc (:db cofx) :user user)
    :dispatch [:navigate "/"]}))
