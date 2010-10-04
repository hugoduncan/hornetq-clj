(ns hornetq-clj.core-client
  "Hornetq core client api"
  (:import
   org.hornetq.api.core.TransportConfiguration
   org.hornetq.api.core.client.HornetQClient
   org.hornetq.api.core.client.MessageHandler
   org.hornetq.core.remoting.impl.netty.NettyConnectorFactory))

(defn session-factory
  "Create a session factory for the given host and port"
  [{:keys [host port] :or {host "localhost" port 5445} :as options}]
  (-> NettyConnectorFactory
      .getName
      (TransportConfiguration. {"host" host "port" port})
      (HornetQClient/createClientSessionFactory)
      (doto (.setReconnectAttempts -1))))

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
  [session queue-name  & {:keys [address filter]}]
  (.createTemporaryQueue session (or address queue-name) queue-name filter))

(defn create-queue
  [session queue-name & {:keys [address filter durable] :or {durable false}}]
  (.createQueue session (or address queue-name) queue-name filter durable))

(defn acknowledge [session message]
  (.acknowledge message)
  (.commit session))
