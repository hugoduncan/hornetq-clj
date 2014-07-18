(ns hornetq-clj.server-test
  (:use
   clojure.test)
  (:require
   [hornetq-clj.server :as server]))

(deftest stomp-server-test
  (let [server (server/server {:netty 55445})]
    (.start server)
    (is (.isActive server))
    (is (.isStarted server))
    (.stop server)))
