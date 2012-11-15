{:release
 {:set-version
  {:updates [{:path "README.md" :no-snapshot true}
             {:path "src/leiningen/hornetq_server.clj"
              :search-regex
              #"\"hornetq-clj/server\" \"\d+\.\d+\.\d+(-SNAPSHOT)?\""}]}}}
