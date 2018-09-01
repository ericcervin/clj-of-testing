(ns clj-of-testing.core
  (:require [clj-http.client :as client]
            [clojure.test :refer [is deftest run-tests]]))

(def all-sites ["http://ericervin.org" 
                "http://ericervin.com"
                "http://ericcervin.github.io"
                "http://noiselife.org"])

(deftest sites-up 
  (is (= [200 200 200 200] (mapv #(:status (client/get %)) all-sites))))

(deftest all-sites-have-robots
  (let [robot-urls (mapv #(str % "/robots.txt") all-sites)
        robot-bodies (mapv #(:body (client/get %)) robot-urls)]
       (is (every? #(clojure.string/includes? % "User-agent: *\nDisallow:") robot-bodies))))

(defn -main []
  (run-tests))
            
   
