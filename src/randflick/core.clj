(ns randflick.core
  (:require
    [clj-http.client :as client]
    [net.cgrand.enlive-html :as html]
    [clojure.string :as string])
  (:import (java.net URL)))


(def platforms (ref {
                     :netflix {
                               :url    "https://editorial.rottentomatoes.com/guide/best-netflix-movies-to-watch-right-now/"
                               :movies `()
                               }
                     :disney  {
                               :url    "https://editorial.rottentomatoes.com/guide/best-disney-movies-to-watch-now/"
                               :movies ()
                               }
                     :hbo     {
                               :url    "https://editorial.rottentomatoes.com/guide/best-movies-on-hbo-max/"
                               :movies ()
                               }
                     :hulu    {
                               :url    "https://editorial.rottentomatoes.com/guide/best-movies-on-hulu/"
                               :movies ()
                               }
                     }))

(defn cacheMoviesFor [platform]
   (as-> platform v
             (v @platforms)
             (:url v)
             (URL. v)
             (html/html-resource v)
             (html/select v [:div.article_movie_title])
             (assoc-in @platforms [platform :movies] v)
             (dosync (ref-set platforms v)))
  )

(defn getMoviesForPlatform [platform]
  (get-in [platform :movies] @platforms))

; grab the movie nodes for a given platform and cache them if necessary.
; platform should be a keyword
(defn getTitleNodes [platform]
  (if (empty? (get-in @platforms [platform :movies]))
    (do (cacheMoviesFor platform) (get-in @platforms [platform :movies]))
    (get-in @platforms [platform :movies])))



(defn getAnchors [platform]
  (html/select (getTitleNodes platform) [:a]))

(defn getTitles [platform]
  (flatten (map :content (getAnchors platform))))

(defn foo
  "Reads the streaming platform to check for movies from stdin."
  []
  (while true (do
                (print "Enter platform: ")
                (flush)
                (-> (read-line)
                    (string/trim)
                    (.toLowerCase)
                    (keyword)
                    (getTitles)
                    (nth (rand-int 100))
                    (println "is your movie!")
                    ))))




