(ns clj-of-testing.core
  (:require [clj-http.client :as client]
            [clojure.test :refer [is]]))

(defn -main []
  (let [all-sites ["http://ericervin.org" 
                   "http://ericervin.com"
                   "http://ericcervin.github.io"
                   "http://noiselife.org"]]
            
   (is (= [200 200 200 300] (mapv #(:status (client/get %)) all-sites)))))
