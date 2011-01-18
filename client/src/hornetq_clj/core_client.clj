(ns hornetq-clj.core-client
  "Hornetq core client api"
  (:import
   org.hornetq.api.core.HornetQException
   org.hornetq.api.core.SimpleString
   org.hornetq.api.core.TransportConfiguration
   org.hornetq.api.core.client.ClientConsumer
   org.hornetq.api.core.client.ClientMessage
   org.hornetq.api.core.client.ClientProducer
   org.hornetq.api.core.client.ClientSession
   org.hornetq.api.core.client.HornetQClient
   org.hornetq.api.core.client.MessageHandler
   org.hornetq.core.remoting.impl.invm.InVMConnectorFactory
   org.hornetq.core.remoting.impl.netty.NettyConnectorFactory))

(defn- stringish?
  "Predicate for testing whether an argument is a string as recognised by
   HornetQ."
  [s]
  (or (string? s) (instance? SimpleString s)))

(defn- not-xor
  [a b]
  (or (and a b) (not (or a b))))

(defn- xor
  [a b]
  (and (or a b) (not (and a b))))

(defn session-factory
  "Create a session factory for the given host and port"
  [{:keys [host port] :or {host "localhost" port 5445} :as options}]
  (-> NettyConnectorFactory
      .getName
      (TransportConfiguration. {"host" host "port" port})
      (HornetQClient/createClientSessionFactory)
      (doto (.setReconnectAttempts -1))))

(defn in-vm-session-factory
  "Create a session factory for the given host and port"
  []
  (-> InVMConnectorFactory
      .getName
      (TransportConfiguration.)
      (HornetQClient/createClientSessionFactory)))

(defn session
  [session-factory user password
   & {:keys [xa auto-commit-sends auto-commit-acks pre-acknowledge
             ack-batch-size]
      :or {xa false
           auto-commit-sends true
           auto-commit-acks true
           pre-acknowledge false
           ack-batch-size 1}}]
  (.createSession
   session-factory user password xa auto-commit-sends
   auto-commit-acks pre-acknowledge ack-batch-size))

(defn create-temporary-queue
  [^ClientSession session queue-name & {:keys [address filter]}]
  (.createTemporaryQueue session (or address queue-name) queue-name filter))

(defn ensure-temporary-queue
  "Ensure the specified temporary queue exists, creating it if not."
  [^ClientSession session queue-name & {:keys [address filter] :as options}]
  (try
    (apply create-temporary-queue session queue-name (apply concat options))
    (catch HornetQException e
      (when-not (= (.getCode e) HornetQException/QUEUE_EXISTS)
        (throw e)))))

(defn create-queue
  [^ClientSession session queue-name
   & {:keys [address filter durable] :or {durable false}}]
  (if filter
    (.createQueue
     session (or address queue-name) queue-name filter (boolean durable))
    (.createQueue
     session (or address queue-name) queue-name (boolean durable))))

(defn ensure-queue
  "Ensure the specified queue exists, creating it if not."
  [^ClientSession session queue-name
   & {:keys [address filter durable] :as options}]
  (try
    (apply create-queue session queue-name (apply concat options))
    (catch HornetQException e
      (when-not (= (.getCode e) HornetQException/QUEUE_EXISTS)
        (throw e)))))

(defn query-queue
  "Query a queue"
  [session queue-name]
  {:pre [(stringish? queue-name)]}
  (.queueQuery session
               (if (string? queue-name)
                 (SimpleString. queue-name)
                 queue-name)))

(defn delete-queue
  "Delete the specified queue.  Returns `session`."
  [^ClientSession session queue-name]
  (.deleteQueue session queue-name)
  session)

(defn acknowledge
  "Acknowledge the message. Returns `message`."
  [^ClientMessage message]
  (.acknowledge message)
  message)

(defn ^ClientProducer create-producer
  "Create a message producer than can be used to send messages.

    - `address` specifies the default address (string)
    - `rate`    specifies the producer rate (integer)"
  ([^ClientSession session]
     (.createProducer session))
  ([^ClientSession session address]
     {:pre [(stringish? address)]}
     (.createProducer session address))
  ([^ClientSession session address rate]
     {:pre [(stringish? address)
            (integer? rate)]}
     (.createProducer session address rate)))

(defn ^ClientConsumer create-consumer
  "Create a message producer than can be used to consume messages from the
   queue specified by `queue-name`.

   Options:
    - :filter        a filter string
    - :window-size   a window size (requires max-rate)
    - :max-rate      maximum read rate (requires window-size)
    - :browse-only   flag for browse only"
  [^ClientSession session queue-name
   & {:keys [filter window-size max-rate browse-only]}]
  {:pre [(stringish? queue-name)
         (not-xor window-size max-rate)
         (or (not (or window-size max-rate)) (not (nil? browse-only)))]}
  (cond
   (and window-size max-rate) (.createConsumer
                               session queue-name filter
                               window-size max-rate
                               (boolean browse-only))
   (and filter (not (nil? browse-only))) (.createConsumer
                                          session queue-name filter browse-only)
   (not (nil? browse-only)) (.createConsumer session queue-name browse-only)
   filter (.createConsumer session queue-name filter)
   :else (.createConsumer session queue-name)))


(defn ^ClientMessage create-message
  "Create a message.

    - `message-type`  type of the message (byte)
    - `durable`       whether the created message is durable or not
    - `expiration`    the message expiration (long)
    - `timestamp`     the message timestamp (long)
    - `priority`      the message priority (between 0 and 9 inclusive)"
  ([^ClientSession session durable]
     (.createMessage session (boolean durable)))
  ([^ClientSession session message-type durable]
     (.createMessage session (byte message-type) (boolean durable)))
  ([^ClientSession session message-type durable expiration timestamp priority]
     (.createMessage
      session
      (byte message-type) (boolean durable)
      (long expiration) (long timestamp) (byte priority))))

(defn ^ClientMessage write-message
  "Write value to the message in a readable manner"
  [^ClientMessage message value]
  (..  message getBodyBuffer (writeString (pr-str value)))
  message)

(defn read-message
  "Read the value from the message."
  [^ClientMessage message]
  {:pre [message]}
  (binding [*read-eval* false]
    (read-string (..  message getBodyBuffer readString))))

(defn messages
  "Create a lazy sequence of messages using the consumer"
  ([^ClientConsumer consumer]
     (lazy-seq
      (when-not (.isClosed consumer)
        (when-let [msg (.receive consumer)]
          (cons
           msg
           (messages consumer))))))
  ([^ClientConsumer consumer timeout]
     {:pre [(integer? timeout)]}
     (lazy-seq
      (when-not (.isClosed consumer)
        (when-let [msg (.receive consumer timeout)]
          (cons
           msg
           (messages consumer timeout)))))))

(defn send-message
  "Send a message via a producer"
  ([^ClientProducer producer ^ClientMessage message]
     {:pre [producer message]}
     (.send producer message))
  ([^ClientProducer producer ^ClientMessage message address]
     {:pre [producer message (stringish? address)]}
     (.send producer address message)))
