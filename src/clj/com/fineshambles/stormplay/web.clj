(ns com.fineshambles.stormplay.web
  (:use compojure.core hiccup.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty])
  (:import (com.fineshambles.stormplay DefinitionsRepository)))

(defn decorate [body]
  (html [:head [:body body]]))


(defn index-page []
  (let [words (. DefinitionsRepository getWords)
        word-lis (map (fn [w] [:li [:a {:href (str "/words/" w)} w]]) words)]
    (decorate [:ul word-lis])))

(defn word-page [{{w :word} :params}]
 (let [defns (. DefinitionsRepository get w)]
      (decorate (reduce (fn [a b] (concat a [b [:br]]))
                        []
                        defns))))

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/words/:word" [word] word-page)
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))

(defn start []
  (jetty/run-jetty app {:port 9998}))