(defproject hornetq-clj "0.2.0-SNAPSHOT"
  :description "hornetq clj interface"
  :url "https://github.com/hugoduncan/hornetq-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-sub "0.2.3"]]
  :sub ["client" "server" "lein-hornetq" "example"]
  :aliases {"clean" ["sub" "clean"]
            "install" ["sub" "install"]
            "deploy" ["sub" "deploy"]
            "test" ["sub" "test"]
            "doc" ["sub" "with-profile" "codox,jdk1.7" "doc"]
            "set-sub-version" ["sub" "with-profile" "release" "set-version"]})
