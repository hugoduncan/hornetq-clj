(defproject hornetq-clj/server "0.2.0-SNAPSHOT"
  :description "Simplify using hornetq server"
  :url "https://github.com/hugoduncan/hornetq-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.hornetq/hornetq-core "2.2.18.Final"]
                 [org.hornetq/hornetq-logging "2.2.18.Final"]
                 [org.jboss.netty/netty "3.2.1.Final"]]
  :repositories {"JBoss releases"
                 "http://repository.jboss.org/nexus/content/groups/public-jboss/"})
