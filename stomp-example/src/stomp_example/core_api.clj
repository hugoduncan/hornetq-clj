(ns stomp-example.core-api
  (:require
   [hornetq-clj.core-client :as core-client]))



(defn make-queues
  "Make the nrepl queues on the hornetmq server if needed"
  [{:keys [service-queue client-queue]}]
  (let [session-factory (core-client/session-factory {})]
    (with-open [session (core-client/session session-factory "" "" {})]
      (try
        (core-client/create-queue session "/queue/service" {})
        (catch org.hornetq.api.core.HornetQException e
          (println (.getMessage e))))
      (try
        (core-client/create-queue session "/queue/consumer" {})
        (catch org.hornetq.api.core.HornetQException e
          (println (.getMessage e)))))))




(defn service []
  (with-open [socket (java.net.Socket. "localhost" 61613)]
    (stomp/with-connection socket {:login "" :password ""}
      (println "Service connected")
      (stomp/subscribe socket {:destination "/queue/service"})
      (println "Service subscribed")
      (loop []
        (let [message (stomp/receive socket)]
          (println "Service received message")
          (when-let [reply-to (:reply-to (:headers message))]
            (stomp/send socket {:destination reply-to}
                        (str "Hello: " (:body message)))
            (println "Service replied to message"))
          (recur))))))


(defn consumer []
  (with-open [socket (java.net.Socket. "localhost" 61613)]
    (stomp/with-connection socket {:login "" :password ""}
      (println "Consumer connected")
      (stomp/subscribe socket {:destination "/queue/consumer"})
      (println "Consumer subscribed")
      (stomp/send socket
                  {:destination "/queue/service" :reply-to "/queue/consumer"}
                  "i'm a message")
      (println "Consumer sent message")
      (let [message (stomp/receive socket)]
        (println {:body message})))))

(defonce #^ExecutorService executor
  (Executors/newCachedThreadPool
   (proxy [ThreadFactory] []
     (newThread [#^Runnable r]
                (doto (Thread. r)
                  (.setDaemon true))))))

(defn execute
  [#^Callable f]
  (.submit executor f))

(defn run-service []
  (execute service))
