(ns hornetq-clj.example.core-api
  (:require
   [hornetq-clj.core-client :as core-client])
  (:import
   (java.util.concurrent
    Callable Future ExecutorService Executors
    ThreadFactory
    CancellationException ExecutionException TimeoutException)))

(defn session-factory
  []
  (core-client/session-factory
   (core-client/server-locator
    :static {} (core-client/transport {:host "localhost"}))))

(defn session
  [session-factory]
  (core-client/session session-factory "admin" "password" {}))

(defn make-queues
  "Make the nrepl queues on the hornetmq server if needed"
  [{:keys [service-queue client-queue]
    :or {service-queue "/queue/service"
         client-queue "/queue/consumer"}}]
  (let [session-factory (session-factory)]
    (with-open [session (session session-factory)]
      (try
        (core-client/create-queue session service-queue {})
        (catch org.hornetq.api.core.HornetQException e
          (println (.getMessage e))))
      (try
        (core-client/create-queue session client-queue {})
        (catch org.hornetq.api.core.HornetQException e
          (println (.getMessage e)))))))

(defn service []
  (let [session-factory (session-factory)]
    (let [session (session session-factory)
          consumer (core-client/create-consumer session "/queue/service" {})
          producer (core-client/create-producer session "/queue/consumer")
          handler (core-client/message-handler
                   (fn [msg]
                     (let [message (core-client/read-message msg)]
                       (core-client/acknowledge msg)
                       (when-let [reply-to (:reply-to message)]
                         (let [m (core-client/create-message session false)]
                           (core-client/write-message-string
                            m (str "Hello: " (:body message)))
                           (core-client/send-message producer m reply-to))))))]
      (.setMessageHandler consumer handler)
      (.start session)
      [session consumer producer])))

(defn consumer []
  (let [session-factory (session-factory)]
    (with-open [session (session session-factory)]
      (let [consumer (core-client/create-consumer session "/queue/consumer" {})
            producer (core-client/create-producer session "/queue/service")
            message (core-client/create-message session false)]
        (.start session)
        (core-client/write-message
         message
         {:reply-to "/queue/consumer" :body "I'm a message"})
        (core-client/send-message producer message "/queue/service")
        (println "Message sent to service")
        (let [message (core-client/receive-message consumer 5000)]
          (when message
            (core-client/acknowledge message)
            (println (core-client/read-message-string message))))))))

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
