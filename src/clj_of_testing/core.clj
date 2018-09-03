(ns clj-of-testing.core
  (:require [clj-http.client :as client]
            [clojure.test :refer [is deftest run-tests]]
            [net.cgrand.enlive-html :as html]))
            

(def all-sites [{:url "http://ericervin.org" :title "<title>Eric Ervin Dot Org</title>" :header "<h1>Eric Ervin Dot Org</h1>"}  
                {:url "http://ericervin.com" :title "<title>Eric Ervin Dot Com</title>" :header "<h1>Eric Ervin Dot Com</h1>"}
                {:url "http://ericcervin.github.io" :title "<title>ericcervin.github.io</title>" :header nil}
                {:url "http://noiselife.org" :title "<title>noiselife-dot-org</title>" :header nil}])


(defn html-title [h]
  (nth (re-seq #"<title>.*</title>" h) 0))

(defn html-header [h]
  (nth (re-seq #"<h1>.*</h1>" h) 0))

(defn html-trs [h]
  (html/select (html/html-snippet h) [:tr]))

(defn html-top-table [h]
  (nth (re-seq #"<table>.*</table>" h) 0))

(defn parse-pages [v]
  (let [urls v
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        headers (mapv html-header bodies)]
    {:statuses statuses
     :titles titles
     :headers headers}))
     

(deftest all-sites-up
  (is (every? true? (for [s all-sites]
                      (let [url (:url s)
                            response (client/get (:url s))
                            status (:status response)
                            body (:body response)
                            title (html-title body)
                            header (html-header body)]
                       (and (= 200 status)
                            (= (:title s) title)
                            (= (:header s) header)))))))

(deftest all-sites-have-robots
  (let [robot-urls (mapv #(str (:url %) "/robots.txt") all-sites)
        robot-bodies (mapv #(:body (client/get %)) robot-urls)]
       (is (every? #(clojure.string/includes? % "User-agent: *\nDisallow:") robot-bodies))))

(deftest all-sites-have-404s
  (let [all-sites (filter #(not= "http://ericcervin.github.io" (:url %)) all-sites)
        failing-urls (mapv #(str (:url %) "/platypus") all-sites)
        results-map (parse-pages failing-urls)]
        ;;failing-responses (mapv #(client/get % {:throw-exceptions false}) failing-urls)
        ;;failing-statuses (mapv :status failing-responses)
        ;;failing-bodies (mapv :body failing-responses)
        ;;failing-titles (mapv html-title failing-bodies)]
       
    (is (every? #(= 404 %) (:statuses results-map)))
    (is (every? #(= % "<title>Error 404 Not Found</title>") (:titles results-map)))
    (is (every? #(clojure.string/includes? % "<body>404 - Not Found</body>") (:bodies results-map)))))


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
   
(deftest philosophy
  (let [urls ["http://ericervin.org/philosophy" "http://ericervin.com/philosophy"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        headers (mapv html-header bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Philosophy USA</title>") titles))    
    (is (every? #(= % "<h1>Philosophy USA</h1>") headers))))

(deftest philosophy-reports
  (let [urls ["http://ericervin.org/philosophy/reports/inst_count" "http://ericervin.com/philosophy/reports/inst_count"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        trs-counts (mapv #(count (html-trs %)) bodies)
        tables (mapv html-top-table bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Philosophy Degrees Completed by Institution</title>") titles))
    (is (apply = trs-counts)))) 

(deftest powerball
  (let [urls ["http://ericervin.org/powerball" "http://ericervin.com/powerball"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        headers (mapv html-header bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Powerball</title>") titles))    
    (is (every? #(= % "<h1>Powerball</h1>") headers))))

(deftest serialism
  (let [urls ["http://ericervin.org/serialism" "http://ericervin.com/serialism"]
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        headers (mapv html-header bodies)]
    (is (every? #(= 200 %) statuses))
    (is (every? #(= % "<title>Serialism</title>") titles))    
    (is (every? #(= % "<h1>Serialism</h1>") headers))))

(defn -main []
  (run-tests))
            
   
