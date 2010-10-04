(ns stomp-example.core-api
  (:require
   [hornetq-clj.core-client :as core-client]))

(defn make-queues
  []
  (let [session-factory (core-client/session-factory {})]
    (with-open [session (core-client/session session-factory "" "")]
      (try
        (core-client/create-queue session "/queue/service")
        (catch org.hornetq.api.core.HornetQException e
          (println (.getMessage e))))
      (try
        (core-client/create-queue session "/queue/consumer")
        (catch org.hornetq.api.core.HornetQException e
          (println (.getMessage e)))))))
