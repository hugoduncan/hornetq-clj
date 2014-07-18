(defproject hornetq-clj/example "0.2.2-SNAPSHOT"
  :description "An example of using stomp"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [hornetq-clj/client "0.2.2-SNAPSHOT"]]
  :dev-dependencies [[hornetq-clj/lein-hornetq "0.2.2-SNAPSHOT"]]
  :plugins [[hornetq-clj/lein-hornetq "0.2.0"]
            [lein-modules "0.3.2"]]
  :profiles {:travis {:modules {:subprocess "lein2"}}})
