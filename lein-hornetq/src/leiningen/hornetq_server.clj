(ns leiningen.hornetq-server
  (:use [leiningen.compile :only [eval-in-project]]))

(defn hornetq-server
  "Launch hornetq server"
  ([project & {:as opts}]
     (eval-in-project
      project
      `(do (require '~'hornetq-clj.server)
           (let [server# (@(ns-resolve '~'hornetq-clj.server '~'stomp-server)
                          '~(zipmap
                             (map read-string (keys opts))
                             (map read-string (vals opts))))]
             (.start server#)
             (loop []
               (when (.isStarted server#)
                 (Thread/sleep 10000)
                 (recur))))))))
