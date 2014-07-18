(defproject hornetq-clj/example "0.2.2-SNAPSHOT"
  :description "An example of using hornetq-clj client"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [hornetq-clj/client "0.2.2-SNAPSHOT"]]
  :dev-dependencies [[hornetq-clj/lein-hornetq "0.2.2-SNAPSHOT"]]
  :plugins [[hornetq-clj/lein-hornetq "0.2.2-SNAPSHOT"]
            [lein-modules "0.3.2"]])
