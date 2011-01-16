(defproject hornetq-clj/server "0.1.0"
  :description "Simplify using hornetq server"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.hornetq/hornetq-core "2.1.2.Final"]
                 [org.hornetq/hornetq-logging "2.1.2.Final"]
                 [org.jboss.netty/netty "3.2.1.Final"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :repositories {"JBoss releases"
                 "http://repository.jboss.org/nexus/content/groups/public-jboss/"})
