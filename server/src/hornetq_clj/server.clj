(ns hornetq-clj.server
  (:import
   org.hornetq.core.config.impl.ConfigurationImpl
   org.hornetq.api.core.TransportConfiguration
   org.hornetq.core.server.HornetQServer
   org.hornetq.core.server.HornetQServers
   org.hornetq.core.server.JournalType
   org.hornetq.core.journal.impl.AIOSequentialFileFactory))

(defn transport-configuration [{:as m}]
  (TransportConfiguration.
   (:factory-class-name m)
   (let [m (dissoc m :factory-class-name)]
     (zipmap (map name (keys m)) (vals m)))))

(defn make-server
  "Create a hornetq server"
  [{:keys [acceptors connectors journal-type security-enabled]}]
  (HornetQServers/newHornetQServer
   (doto (ConfigurationImpl.)
     (.setAcceptorConfigurations
      (set (map transport-configuration acceptors)))
     (.setConnectorConfigurations
      (into {}
            (map #(vector (:name %) (transport-configuration (dissoc % :name)))
                 connectors)))
     (.setJournalType
      (if journal-type
        (Enum/valueOf JournalType (name journal-type))
        (if (AIOSequentialFileFactory/isSupported)
          JournalType/ASYNCIO
          JournalType/NIO)))
     (.setSecurityEnabled (boolean security-enabled)))))

(def netty-connector-factory
  "org.hornetq.core.remoting.impl.netty.NettyConnectorFactory")
(def netty-acceptor-factory
  "org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory")

(def in-vm-connector-factory
  "org.hornetq.core.remoting.impl.invm.InVMConnectorFactory")
(def in-vm-acceptor-factory
  "org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory")

(defn server
  [{:keys [netty stomp in-vm] :as options}]
  (make-server (merge-with
                conj
                options
                {:connectors (filter
                              identity
                              [(when (or stomp netty)
                                 {:name "netty-connector"
                                  :factory-class-name
                                  netty-connector-factory})
                               (when in-vm
                                 {:name "invm-connector"
                                  :factory-class-name
                                  in-vm-connector-factory})])
                 :acceptors (filter
                             identity
                             [(when netty
                                (merge
                                 {:factory-class-name netty-acceptor-factory
                                  :port (or (and (number? netty) netty) 5445)}))
                              (when stomp
                                {:factory-class-name netty-acceptor-factory
                                 :protocol "stomp"
                                 :port (or (and (number? stomp) stomp)
                                           61613)})
                              (when in-vm
                                {:factory-class-name
                                 in-vm-acceptor-factory})])})))

(defn in-vm-server
  [{:keys [connectors acceptors netty stomp] :as options}]
  (server (assoc options :in-vm true)))

(defn locate-queue [server ^String queue-name]
  (.locateQueue server (org.hornetq.api.core.SimpleString. queue-name)))
