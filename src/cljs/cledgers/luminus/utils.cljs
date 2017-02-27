(ns cledgers.luminus.utils)


(defn pp [derta]
  (with-out-str (cljs.pprint/pprint derta)))
