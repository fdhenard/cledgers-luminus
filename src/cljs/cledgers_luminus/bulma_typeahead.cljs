(ns cledgers-luminus.bulma-typeahead
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.pprint :as pp]))

(defn query-callback [matches-atom is-loading-atom response]
  (println "calling callback")
  (do
    (reset! matches-atom (-> response :result))
    (reset! is-loading-atom false)))


(defn on-typeahead-change! [new-val count-atom value-atom matches-atom is-loading-atom query-func]
  (do
    (reset! value-atom new-val)
    (swap! count-atom inc)
    (js/setTimeout
     (fn []
       (swap! count-atom dec)
       #_(println "count-atom val in timeout cb:" @count-atom)
       (when (and (<= @count-atom 0)
                  (not (empty? new-val)))
         (reset! is-loading-atom true)
         (println "calling-exec-query! for query =" new-val)
         (query-func new-val (partial query-callback matches-atom is-loading-atom))))
     500)))


(defn typeahead-textbox [value-atom matches-atom query-func]
  (let [change-count-atom (atom 0)
        is-loading-atom (atom false)]
    (fn []
      (println "is-loading:" @is-loading-atom)
      [:div {:class #{:field}}
       [:div {:class #{:control (when @is-loading-atom :is-loading)}}
        [:input {:class #{:input} :type :text :placeholder "something"
                 :value @value-atom
                 :on-change #(let [new-val (-> % .-target .-value)
                                   #_ (println "new-val:" new-val)]
                               (on-typeahead-change!
                                new-val
                                change-count-atom
                                value-atom
                                matches-atom
                                is-loading-atom
                                query-func))}]]])))

(defn create-callback [response])

(defn typeahead-component [parm-map]
  (let [#_ (println "rendering typeahead-component")
        #_ (println "app-state")
        #_ (pp/pprint @app-state)
        #_ (pp/pprint parm-map)
        textbox-val-atom (atom (:value parm-map))
        matches-atom (atom #{})
        selection-val-atom (atom nil)]
    (fn [parm-map]
      (let [value (:value parm-map)
            query-func (:query-func parm-map)
            on-change (:on-change parm-map)
            dropdown-expanded (not (= @textbox-val-atom @selection-val-atom))]
        [:div {:class #{:dropdown (when dropdown-expanded :is-active)}}
         [:div {:class #{:dropdown-trigger}}
          [typeahead-textbox textbox-val-atom matches-atom query-func]]
         [:div {:class #{:dropdown-menu} :id :dropdown-menu :role :menu}
          [:div {:class #{:dropdown-content}}
           (let [matches @matches-atom
                 textbox-val @textbox-val-atom]
             (if (<= (count matches) 0)
               [:a {:href "#"
                    :class #{:dropdown-item}
                    :on-click (fn [evt]
                                (do
                                  (reset! selection-val-atom @textbox-val-atom)
                                  (on-change {:value @textbox-val-atom
                                              :is-new true})))}
                (str "create new \"" textbox-val "\"")]
               (for [item matches]
                 ^{:key (:id item)}
                 [:a {:href "#" :class #{:dropdown-item}} (:text item)])))]]]))))
