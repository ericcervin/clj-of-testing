(ns clj-of-testing.core
  (:require [clj-http.client :as client]
            [clojure.test :refer [is deftest run-tests]]
            [net.cgrand.enlive-html :as html]))
            

(def all-sites ["http://ericervin.org" 
                "http://ericervin.com"
                "http://ericcervin.github.io"
                "http://noiselife.org"])

(defn html-title [h]
  (nth (re-seq #"<title>.*</title>" h) 0))

(defn html-header [h]
  (nth (re-seq #"<h1>.*</h1>" h) 0))

(defn html-trs [h]
  (html/select (html/html-snippet h) [:tr]))

(defn html-top-table [h]
  (nth (re-seq #"<table>.*</table>" h) 0))

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
        failing-bodies (mapv :body failing-responses)
        failing-titles (mapv html-title failing-bodies)]
       
    (is (every? #(= 404 %) failing-statuses))
    (is (every? #(= % "<title>Error 404 Not Found</title>") failing-titles))
    (is (every? #(clojure.string/includes? % "<body>404 - Not Found</body>") failing-bodies))))


(deftest destiny
  (let [urls ["http://ericervin.org/destiny" "http://ericervin.com/destiny"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        headers (mapv html-header bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Destiny</title>") titles))    
    (is (every? #(= % "<h1>Star Wars Destiny</h1>") headers))))

(deftest destiny-cards
  (let [urls ["http://ericervin.org/destiny/cards?" "http://ericervin.com/destiny/cards?"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        trs-counts (mapv #(count (html-trs %)) bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>  Cards</title>") titles))
    (is (apply = trs-counts))))    
    
(deftest destiny-reports
  (let [urls ["http://ericervin.org/destiny/reports/rarity_count" "http://ericervin.com/destiny/reports/rarity_count"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        tables (mapv html-top-table bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Count by Rarity</title>") titles))
    (is (apply = tables))))   

(deftest discogs
  (let [urls ["http://ericervin.org/discogs" "http://ericervin.com/discogs"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        headers (mapv html-header bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Discogs</title>") titles))    
    (is (every? #(= % "<h1>My Record Collection</h1>") headers))))

(deftest discogs-releases
  (let [urls ["http://www.ericervin.org/discogs/releases?" "http://www.ericervin.com/discogs/releases?"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        trs-counts (mapv #(count (html-trs %)) bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Releases by Artist</title>") titles))
    (is (apply = trs-counts))))    

(deftest discogs-reports
  (let [urls ["http://www.ericervin.org/discogs/reports/artist_count" "http://www.ericervin.com/discogs/reports/artist_count"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        tables (mapv html-top-table bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Count by Artist</title>") titles))
    (is (apply = tables)))) 

(deftest gematria
  (let [urls ["http://ericervin.org/gematria" "http://ericervin.com/gematria"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        headers (mapv html-header bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Gematria</title>") titles))    
    (is (every? #(= % "<h1>Gematria</h1>") headers))))

(deftest gematria-search-word
  (let [urls ["http://www.ericervin.org/gematria/search?word=fish" "http://www.ericervin.com/gematria/search?word=fish"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        trs-counts (mapv #(count (html-trs %)) bodies)
        tables (mapv html-top-table bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Gematria</title>") titles))
    (is (apply = tables))
    (is (apply = trs-counts))))    
   

(deftest gematria-search-value
  (let [urls ["http://www.ericervin.org/gematria/search?value=65" "http://www.ericervin.com/gematria/search?value=65"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        trs-counts (mapv #(count (html-trs %)) bodies)
        tables (mapv html-top-table bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Gematria</title>") titles))
    (is (apply = trs-counts))))    
   

(defn -main []
  (run-tests))
            
   
