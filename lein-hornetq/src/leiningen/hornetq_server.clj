(ns leiningen.hornetq-server
  "A leiningen plugin to start a HornetQ server."
  (:use
   [clojure.tools.cli :only [cli]]))

(defn eval-in-project
  "Support eval-in-project in both Leiningen 1.x and 2.x."
  [& args]
  (let [eip (or (try (require 'leiningen.core.eval)
                     (resolve 'leiningen.core.eval/eval-in-project)
                     (catch java.io.FileNotFoundException _))
                (try (require 'leiningen.compile)
                     (resolve 'leiningen.compile/eval-in-project)
                     (catch java.io.FileNotFoundException _)))]
    (apply eip args)))

(def deps '[[hornetq-clj/server "0.2.1"]
            [org.slf4j/jul-to-slf4j "1.6.4"]
            [ch.qos.logback/logback-classic "1.0.0"]])

(defn hornetq-server
  "Launch hornetq server"
  [project & args]
  (let [[{:keys [netty stomp] :as opts} args]
        (cli args
             ["-s" "--stomp" "Use stomp (port if specified)" :default false]
             ["-n" "--netty" "Use netty (port if specified)" :default true])]
    (eval-in-project
     (assoc-in project [:dependencies] deps)
     `(do (require '~'hornetq-clj.server)
          (let [server# (@(ns-resolve
                           '~'hornetq-clj.server
                           '~'server)
                         '~opts)]
            (.start server#)
            (loop []
              (when (.isStarted server#)
                (Thread/sleep 10000)
                (recur))))))))
