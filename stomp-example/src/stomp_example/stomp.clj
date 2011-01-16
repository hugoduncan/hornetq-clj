(ns stomp-example.stomp
  "A simple example of using a STOMP message queue.
   The producer and consumer would usually be in different processes, but for
   simplicity are both in the same process here."
  (:require
   stomp)
  (:import
   java.io.IOException
   java.net.Socket
   (java.util.concurrent
    Callable Future ExecutorService Executors TimeUnit
    ThreadFactory
    CancellationException ExecutionException TimeoutException)))


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
