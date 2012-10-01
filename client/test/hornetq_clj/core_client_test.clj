(ns hornetq-clj.core-client-test
  (:use
   clojure.test
   hornetq-clj.core-client)
  (:import
   org.hornetq.api.core.client.ClientMessage))

(deftype TestMessage []
    ClientMessage)

(deftest message-handler-test
  (let [a (atom nil)
        mh (message-handler (fn [msg] (reset! a msg) msg))
        m (TestMessage.)]
    (.onMessage mh m)
    (is (= m @a))))
