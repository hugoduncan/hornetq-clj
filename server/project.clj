(defproject hornetq-clj/server "0.2.2-SNAPSHOT"
  :description "Simplify using hornetq server"
  :url "https://github.com/hugoduncan/hornetq-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.hornetq/hornetq-server "2.3.17.Final"]
                 [org.jboss.netty/netty "3.2.1.Final"]]
  :plugins [[lein-modules "0.3.6"]]
  :repositories {"JBoss releases"
                 "http://repository.jboss.org/nexus/content/groups/public-jboss/"})
