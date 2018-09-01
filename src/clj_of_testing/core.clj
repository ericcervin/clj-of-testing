(ns clj-of-testing.core
  (:require [clj-http.client :as client]
            [clojure.test :refer [is deftest run-tests]]))

(def all-sites ["http://ericervin.org" 
                "http://ericervin.com"
                "http://ericcervin.github.io"
                "http://noiselife.org"])

(deftest sites-up 
  (is (every? #(= 200 %) (mapv #(:status (client/get %)) all-sites))))

(deftest all-sites-have-robots
  (let [robot-urls (mapv #(str % "/robots.txt") all-sites)
        robot-bodies (mapv #(:body (client/get %)) robot-urls)]
       (is (every? #(clojure.string/includes? % "User-agent: *\nDisallow:") robot-bodies))))

(deftest all-sites-have-404s
  (let [all-sites (filter #(not= "http://ericcervin.github.io" %) all-sites)
        failing-urls (mapv #(str % "/platypus") all-sites)
        failing-responses (mapv #(client/get % {:throw-exceptions false}) failing-urls)
        failing-statuses (mapv :status failing-responses)
        failing-bodies (mapv :body failing-responses)] 
    (is (every? #(= 404 %) failing-statuses))
    (is (every? #(clojure.string/includes? % "<title>Error 404 Not Found</title>") failing-bodies))
    (is (every? #(clojure.string/includes? % "<body>404 - Not Found</body>") failing-bodies))))


(defn -main []
  (run-tests))
            
   
