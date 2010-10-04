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

(defn server
  "Create a hornetq server"
  [options]
  (HornetQServers/newHornetQServer
   (doto (ConfigurationImpl.)
     (.setAcceptorConfigurations
      (set (map transport-configuration (:acceptors options))))
     (.setConnectorConfigurations
      (into {}
            (map #(vector (:name %) (transport-configuration (dissoc % :name)))
                 (:connectors options))))
     (.setJournalType
      (if-let [journal-type (:journal-type options)]
        (Enum/valueOf JournalType (name journal-type))
        (if (AIOSequentialFileFactory/isSupported)
          JournalType/ASYNCIO
          JournalType/NIO)))
     (.setSecurityEnabled (boolean (:security-enabled options))))))

(def netty-connector-factory
  "org.hornetq.core.remoting.impl.netty.NettyConnectorFactory")
(def netty-acceptor-factory
  "org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory")

(defn stomp-server
  [options]
  (server (merge-with
           conj
           options
           {:connectors [{:name "netty-connector"
                          :factory-class-name netty-connector-factory}]
            :acceptors [{:factory-class-name netty-acceptor-factory}
                        {:factory-class-name netty-acceptor-factory
                         :protocol "stomp"
                         :port 61613}]})))
