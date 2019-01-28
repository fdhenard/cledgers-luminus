(ns cledgers-luminus.entities)

(def entities [{:name "cledgers-user"
                :fields [{:name "username"
                          :type :character
                          :max-length 30
                          :unique true}
                         {:name "first-name"
                          :type :character
                          :max-length 30}
                         {:name "last-name"
                          :type :character
                          :max-length 30}
                         {:name "email"
                          :type :character
                          :max-length 30}
                         {:name "is-admin"
                          :type :boolean
                          :default false}
                         {:name "last-login"
                          :type :date-time
                          :null true}
                         {:name "is-active"
                          :type :boolean
                          :default false}
                         {:name "pass"
                          :type :character
                          :max-length 300}
                         ]}])
