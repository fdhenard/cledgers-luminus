(ns cledgers.luminus.db)

(def default-db
  {:page :home
   :time (js/Date.)
   :time-color "#f88"
   :xaction-editing nil
   :xactions {}
   :user nil
   })
