(defproject storm-play/storm-play "0.0.1-SNAPSHOT" 
  :dependencies [[commons-collections/commons-collections "3.2.1"]
                 [com.basho.riak/riak-client "1.0.6"]
                 [com.google.protobuf/protobuf-java "2.4.1"]
                 [com.sun.jersey.archetypes/jersey-quickstart-grizzly "1.16"]
                 [com.sun.jersey/jersey-grizzly "1.16"]
                 [com.sun.grizzly/grizzly-http "1.9.18-r"]
                 [javax.ws.rs/jsr311-api "1.1-ea"]]
  :source-paths ["src/clj"]
  :profiles {:dev
             {:dependencies
              [[storm "0.8.1"]
               [org.clojure/clojure "1.4.0"] ]}}
  :resource-paths ["multilang"]
  :aot :all
  :java-source-paths ["src/jvm" "gen"]
  :min-lein-version "2.0.0"
  :javac-options {:debug "true"})
