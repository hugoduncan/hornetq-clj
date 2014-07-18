(ns leiningen.hornetq-server
  "A leiningen plugin to start a HornetQ server."
  (:require
   [clojure.string :as string]
   [clojure.tools.cli :refer [parse-opts]]
   [leiningen.core.main :refer [debug]]))

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

(def deps '[[hornetq-clj/server "0.2.2-SNAPSHOT"]
            [org.clojure/clojure "1.6.0"]
            [org.slf4j/jul-to-slf4j "1.6.4"]
            [ch.qos.logback/logback-classic "1.0.0"]])

(def cli-options
  [["-s" "--stomp PORT"
    "Use stomp (port if specified)" :default false]
   ["-n" "--netty PORT"
    "Use netty (port if specified)" :default true]
   ["-u" "--user USER"
    "Set the cluster user" :default "admin"]
   ["-p" "--password PASSWORD"
    "Set the cluster password" :default "password"]
   ["-S" "--disable-security"
    "Disable security" :default false]
   ["-P" "--persistence-enabled"
    "Enable persistence" :default true]])

(defn hornetq-server
  "Launch hornetq server"
  [project & args]
  (let [{:keys [errors options]} (parse-opts args cli-options)
        {:keys [netty stomp] :as opts} (-> options
                                           (assoc :enable-security
                                             (not (:disable-security options)))
                                           (dissoc :disable-security))]
    (when errors
      (throw (ex-info (string/join errors) {:exit-code 1})))
    (debug "options" options)
    (debug "opts" opts)
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
