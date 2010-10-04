(ns hornetq-clj.server-test
  (:use
   clojure.test)
  (:require
   [hornetq-clj.server :as server]))

(deftest stomp-server-test
  (let [server (server/stomp-server {})]
    (.start server)
    (is (.isInitialised server))
    (is (.isStarted server))
    (.stop server)))
