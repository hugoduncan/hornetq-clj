(ns hornetq-clj.core-client
  "Hornetq core client api"
  (:import
   (org.hornetq.api.core
    HornetQException HornetQExceptionType SimpleString TransportConfiguration)
   (org.hornetq.api.core.client
    ClientConsumer ClientMessage ClientProducer ClientSession
    ClientSessionFactory HornetQClient MessageHandler ServerLocator)
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

(defn set-server-locator-options
  [^org.hornetq.api.core.client.ServerLocator server-locator
   {:keys [ack-batch-size
           auto-group
           block-on-acknowledge
           block-on-durable-send
           block-on-non-durable-send
           cache-large-message-client
           call-failover-timeout
           call-timeout
           client-failure-check-period
           compress-large-message
           confirmation-window-size
           connection-load-balancing-policy-class-name
           connection-ttl
           consumer-max-rate
           consumer-window-size
           failover-on-initial-connection
           group-id
           initial-connection-attempts
           initial-message-packet-size
           max-retry-interval
           min-large-message-size
           pre-acknowledge
           producer-max-rate
           producer-window-size
           reconnect-attempts
           retry-interval
           retry-interval-multiplier
           scheduled-thread-pool-max-size
           thread-pool-max-size
           use-global-pools] :as options}]
  (when ack-batch-size
    (.setAckBatchSize server-locator ack-batch-size))
  (when-not (nil? auto-group)
    (.setAutoGroup server-locator auto-group))
  (when-not (nil? block-on-acknowledge)
    (.setBlockOnAcknowledge server-locator block-on-acknowledge))
  (when-not (nil? block-on-durable-send)
    (.setBlockOnDurableSend server-locator block-on-durable-send))
  (when-not (nil? block-on-non-durable-send)
    (.setBlockOnNonDurableSend server-locator block-on-non-durable-send))
  (when-not (nil? cache-large-message-client)
    (.setCacheLargeMessageClient server-locator cache-large-message-client))
  (when call-failover-timeout
    (.setCallFailoverTimeout server-locator call-failover-timeout))
  (when call-timeout
    (.setCallTimeout server-locator call-timeout))
  (when client-failure-check-period
    (.setClientFailureCheckPeriod server-locator client-failure-check-period))
  (when-not (nil? compress-large-message)
    (.setCompressLargeMessage server-locator compress-large-message))
  (when confirmation-window-size
    (.setConfirmationWindowSize server-locator confirmation-window-size))
  (when connection-load-balancing-policy-class-name
    (.setConnectionLoadBalancingPolicyClassName
     server-locator connection-load-balancing-policy-class-name))
  (when connection-ttl
    (.setConnectionTTL server-locator connection-ttl))
  (when consumer-max-rate
    (.setConsumerMaxRate server-locator consumer-max-rate))
  (when consumer-window-size
    (.setConsumerWindowSize server-locator consumer-window-size))
  (when-not (nil? failover-on-initial-connection)
    (.setFailoverOnInitialConnection
     server-locator failover-on-initial-connection))
  (when group-id
    (.setGroupId server-locator group-id))
  (when initial-connection-attempts
    (.setInitialConnectionAttempts server-locator initial-connection-attempts))
  (when initial-message-packet-size
    (.setInitialMessagePacketSize server-locator initial-message-packet-size))
  (when max-retry-interval
    (.setMaxRetryInterval server-locator max-retry-interval))
  (when min-large-message-size
    (.setMinLargeMessageSize server-locator min-large-message-size))
  (when-not (nil? pre-acknowledge)
    (.setPreAcknowledge server-locator pre-acknowledge))
  (when producer-max-rate
    (.setProducerMaxRate server-locator producer-max-rate))
  (when producer-window-size
    (.setProducerWindowSize server-locator producer-window-size))
  (when reconnect-attempts
    (.setReconnectAttempts server-locator reconnect-attempts))
  (when retry-interval
    (.setRetryInterval server-locator retry-interval))
  (when retry-interval-multiplier
    (.setRetryIntervalMultiplier server-locator retry-interval-multiplier))
  (when scheduled-thread-pool-max-size
    (.setScheduledThreadPoolMaxSize
     server-locator scheduled-thread-pool-max-size))
  (when thread-pool-max-size
    (.setThreadPoolMaxSize server-locator thread-pool-max-size))
  (when-not (nil? use-global-pools)
    (.setUseGlobalPools server-locator use-global-pools))
  server-locator)

(defmulti create-server-locator
  (fn [locator-type options transport-configurations-array] locator-type))

(defmethod create-server-locator :static
  [locator-type
   {:keys [ha]}
   ^"[Ljava.lang.String;" transport-configurations-array]
  (HornetQClient/createServerLocator
   (boolean ha) transport-configurations-array))

(defmethod create-server-locator :without-ha
  [locator-type options ^"[Ljava.lang.String;" transport-configurations-array]
  (HornetQClient/createServerLocatorWithoutHA transport-configurations-array))

(defmethod create-server-locator :with-ha
  [locator-type options ^"[Ljava.lang.String;" transport-configurations-array]
  (HornetQClient/createServerLocatorWithHA transport-configurations-array))

(defn server-locator
  "Create a server locator from the given transport configuration.
   The server locator type is one of :static, :without-ha or :with-ha.
   For options, see set-server-locator-options."
  [locator-type {:as options} & transport-configurations]
  {:pre [(#{:static :with-ha :without-ha} locator-type)]}
  (-> (create-server-locator
       locator-type
       options
       (into-array TransportConfiguration transport-configurations))
      (set-server-locator-options options)))

(defmulti ^TransportConfiguration transport
  (fn [{:keys [transport host port]}]
    (or (when transport
          (if (keyword? transport)
            transport
            (keyword (name transport))))
        (if (or host port)
          :netty
          :in-vm))))

(defmethod transport :netty
  [{:keys [host port] :or {host "localhost" port 5445} :as options}]
  (TransportConfiguration.
   (.getName NettyConnectorFactory) {"host" host "port" port}))

(defmethod transport :in-vm
  [{:as _}]
  (TransportConfiguration.
   "org.hornetq.core.remoting.impl.invm.InVMConnectorFactory"))

(defn ^ClientSessionFactory session-factory
  [^ServerLocator server-locator]
  (.createSessionFactory server-locator))

(defn create-session-factory
  [locator-type locator-options transport-options]
  (let [transport-configuration (transport transport-options)
        server-locator (server-locator
                        locator-type locator-options transport-configuration)]
    (session-factory server-locator)))


(defn netty-session-factory
  "Create a session factory for the given host and port"
  [{:keys [host port] :or {host "localhost" port 5445} :as options}]
  (let [^TransportConfiguration transport (TransportConfiguration.
                                           (.getName NettyConnectorFactory)
                                           {"host" host "port" port})]
    (-> (HornetQClient/createServerLocatorWithoutHA
         (into-array TransportConfiguration [transport]))
         (doto (.setReconnectAttempts -1))
        .createSessionFactory)))

(defn in-vm-session-factory
  "Create a session factory for an in VM server."
  [_]
  (let [transport (TransportConfiguration.
                   "org.hornetq.core.remoting.impl.invm.InVMConnectorFactory")]
    (-> (HornetQClient/createServerLocatorWithoutHA
         (into-array TransportConfiguration [transport]))
        .createSessionFactory)))

(defn session
  [^ClientSessionFactory session-factory ^String user ^String password
   {:keys [xa auto-commit-sends auto-commit-acks pre-acknowledge
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
  [^ClientSession session ^String queue-name
   {:keys [^String address ^String filter]}]
  (if filter
    (.createTemporaryQueue session (or address queue-name) queue-name filter)
    (.createTemporaryQueue session (or address queue-name) queue-name)))

(defn ensure-temporary-queue
  "Ensure the specified temporary queue exists, creating it if not."
  [^ClientSession session queue-name
   {:keys [address filter] :as options}]
  (try
    (create-temporary-queue session queue-name options)
    (catch HornetQException e
      (when-not (= (.getCode e) HornetQExceptionType/QUEUE_EXISTS)
        (throw e)))))

(defn create-queue
  [^ClientSession session ^String queue-name
   {:keys [^String address ^String filter durable] :or {durable false}}]
  (if filter
    (.createQueue
     session (or address queue-name) queue-name filter (boolean durable))
    (.createQueue
     session (or address queue-name) queue-name (boolean durable))))

(defn ensure-queue
  "Ensure the specified queue exists, creating it if not."
  [^ClientSession session queue-name
   {:keys [address filter durable] :as options}]
  (try
    (create-queue session queue-name options)
    (catch HornetQException e
      (when-not (= (.getCode e) HornetQExceptionType/QUEUE_EXISTS)
        (throw e)))))

(defn query-queue
  "Query a queue"
  [^ClientSession session ^String queue-name]
  {:pre [(stringish? queue-name)]}
  (.queueQuery session
               (if (string? queue-name)
                 (SimpleString. queue-name)
                 queue-name)))

(defn delete-queue
  "Delete the specified queue.  Returns `session`."
  [^ClientSession session ^String queue-name]
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
  ([^ClientSession session ^String address]
     {:pre [(stringish? address)]}
     (.createProducer session address))
  ([^ClientSession session ^String address rate]
     {:pre [(stringish? address)
            (integer? rate)]}
     (.createProducer session address (int rate))))

(defn ^ClientConsumer create-consumer
  "Create a message consumer than can be used to consume messages from the
   queue specified by `queue-name`.

   Options:
    - :filter        a filter string
    - :window-size   a window size (requires max-rate)
    - :max-rate      maximum read rate (requires window-size)
    - :browse-only   flag for browse only"
  [^ClientSession session ^String queue-name
   {:keys [^String filter window-size max-rate browse-only]}]
  {:pre [(stringish? queue-name)
         (not-xor window-size max-rate)]}
  (cond
   (and window-size max-rate) (.createConsumer
                               session queue-name filter
                               (int window-size) (int max-rate)
                               (boolean browse-only))
   (and filter (not (nil? browse-only))) (.createConsumer
                                          session queue-name filter
                                          (boolean browse-only))
   (not (nil? browse-only)) (.createConsumer session queue-name
                                             (boolean browse-only))
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
  (binding [*print-readably* true]
    (..  message getBodyBuffer (writeUTF (pr-str value))))
  message)

(defn ^ClientMessage write-message-string
  "Write s to the message"
  [^ClientMessage message s]
  (..  message getBodyBuffer (writeUTF s))
  message)

(defn read-message
  "Read the value from the message."
  ([^ClientMessage message]
     (read-message message false))
  ([^ClientMessage message eval]
     {:pre [message]}
     (binding [*read-eval* eval]
       (let [msg (..  message getBodyBuffer readUTF)]
         (read-string msg)))))

(defn read-message-string
  "Read a string from the message."
  [^ClientMessage message ]
  {:pre [message]}
  (..  message getBodyBuffer readUTF))

(defn receive-message
  "Receive a message via a consumer"
  ([^ClientConsumer consumer]
     {:pre [consumer]}
     (.receive consumer))
  ([^ClientConsumer consumer ^long timeout]
     {:pre [consumer timeout]}
     (.receive consumer timeout)))

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
  ([^ClientProducer producer ^ClientMessage message ^String address]
     {:pre [producer message (stringish? address)]}
     (.send producer address message)))

(deftype Handler [f]
  MessageHandler
  (onMessage [_ msg]
    (f msg)))

(defn message-handler [f] (Handler. f))
