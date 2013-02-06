(ns com.fineshambles.stormplay.web
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [clojure.pprint :as pprint])
  (:import (com.fineshambles.stormplay DefinitionsRepository)))

(defn decorate [body]
  (str "<html><head></head><body>"
       body
       "</body></html>"))


(defn index-page []
  (let [words (. DefinitionsRepository getWords)
        word-lis (map (fn [w] (str "<li><a href=\"/words/" w "\">" w "</a></li>\n")) words)]
    (decorate (str "<ul>" (apply str word-lis) "</ul>"))))

(defn word-page [{{w :word} :params}]
 (let [defns (. DefinitionsRepository get w)]
      (decorate (reduce (fn [a b] (str a "<br />\n" b))
                        defns))))

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/words/:word" [word] word-page)
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))

(defn start []
  (jetty/run-jetty app {:port 9998}))