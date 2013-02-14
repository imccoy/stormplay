(defproject storm-play/storm-play "0.0.1-SNAPSHOT" 
  :dependencies [[commons-collections/commons-collections "3.2.1"]
                 [com.basho.riak/riak-client "1.0.6"]
                 [com.google.protobuf/protobuf-java "2.4.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.2"]
                 [org.codehaus.jackson/jackson-core-asl "1.9.4"]]
  :source-paths ["src/clj"]
  :profiles {:dev
             {:dependencies
              [[storm "0.8.1"]
               [org.clojure/clojure "1.4.0"]
               [ring-mock "0.1.3"]]}}
  :resource-paths ["multilang"]
  :java-source-paths ["src/jvm" "gen"]
  :min-lein-version "2.0.0"
  :javac-options {:debug "true"})
