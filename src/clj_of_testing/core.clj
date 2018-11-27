
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
  (clojure.string/join (take 512 (nth (re-seq #"(?s)<table.*</table>" h) 0))))

(defn parse-pages [v]
  (let [urls v
        responses (mapv #(client/get % {:throw-exceptions false}) urls)
        statuses (mapv :status responses)
        bodies (mapv :body responses)
        titles (mapv html-title bodies)
        headers (mapv html-header bodies)
        trs-counts (mapv #(count (html-trs %)) bodies)
        tables (mapv html-top-table bodies)]
    {:statuses statuses
     :titles titles
     :headers headers
     :trs-counts trs-counts
     :tables tables}))
     

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
    (is (every? #(= 404 %) (:statuses results-map)))
    (is (every? #(= % "<title>Error 404 Not Found</title>") (:titles results-map)))
    (is (every? #(clojure.string/includes? % "<body>404 - Not Found</body>") (:bodies results-map)))))


(deftest destiny
  (let [urls ["http://ericervin.org/destiny" "http://ericervin.com/destiny"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Destiny</title>") (:titles results-map)))    
    (is (every? #(= % "<h1>Star Wars Destiny</h1>") (:headers results-map)))))

(deftest destiny-cards
  (let [urls ["http://ericervin.org/destiny/cards?" "http://ericervin.com/destiny/cards?"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>  Cards</title>") (:titles results-map)))
    (is (apply = (:trs-counts results-map)))))    
    
(deftest destiny-reports
  (let [urls ["http://ericervin.org/destiny/reports/rarity_count" 
              "http://ericervin.com/destiny/reports/rarity_count"]
              ;;"http://127.0.0.1:5000/destiny/reports/rarity_count"]
              
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Count by Rarity</title>") (:titles results-map)))
    (is (apply = (:tables results-map)))))   

(deftest discogs
  (let [urls ["http://ericervin.org/discogs" "http://ericervin.com/discogs"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Discogs</title>") (:titles results-map)))    
    (is (every? #(= % "<h1>My Record Collection</h1>") (:headers results-map)))))

(deftest discogs-releases
  (let [urls ["http://www.ericervin.org/discogs/releases?" "http://www.ericervin.com/discogs/releases?"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Releases by Artist,Title</title>") (:titles results-map)))
    (is (apply = (:trs-counts results-map)))))    

(deftest discogs-reports
  (let [urls ["http://www.ericervin.org/discogs/reports/artist_count" "http://www.ericervin.com/discogs/reports/artist_count"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Count by Artist</title>") (:titles results-map)))
    (is (apply = (:trs-counts results-map))))) 

(deftest gematria
  (let [urls ["http://ericervin.org/gematria" "http://ericervin.com/gematria"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Gematria</title>") (:titles results-map)))    
    (is (every? #(= % "<h1>Gematria</h1>") (:headers results-map)))))

(deftest gematria-search-word
  (let [urls ["http://www.ericervin.org/gematria/search?word=fish" "http://www.ericervin.com/gematria/search?word=fish"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Gematria</title>") (:titles results-map)))
    (is (apply = (:tables results-map)))
    (is (apply = (:trs-counts results-map)))))    
   

(deftest gematria-search-value
  (let [urls ["http://www.ericervin.org/gematria/search?value=65" "http://www.ericervin.com/gematria/search?value=65"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Gematria</title>") (:titles results-map)))
    (is (apply = (:trs-counts results-map)))))    
   
(deftest philosophy
  (let [urls ["http://ericervin.org/philosophy" "http://ericervin.com/philosophy"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Philosophy USA</title>") (:titles results-map)))    
    (is (every? #(= % "<h1>Philosophy USA</h1>") (:headers results-map)))))

(deftest philosophy-reports
  (let [urls ["http://ericervin.org/philosophy/reports/inst_count" "http://ericervin.com/philosophy/reports/inst_count"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Philosophy Degrees Completed by Institution</title>") (:titles results-map)))
    (is (apply = (:tables results-map)))
    (is (apply = (:trs-counts results-map))))) 

(deftest powerball
  (let [urls ["http://ericervin.org/powerball" "http://ericervin.com/powerball"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Powerball</title>") (:titles results-map)))    
    (is (every? #(= % "<h1>Powerball</h1>") (:headers results-map)))))

(deftest serialism
  (let [urls ["http://ericervin.org/serialism" "http://ericervin.com/serialism"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Serialism</title>") (:titles results-map)))    
    (is (every? #(= % "<h1>Serialism</h1>") (:headers results-map)))))

(deftest wh-champions
  (let [urls ["http://ericervin.org/wh_champions" "http://ericervin.com/wh_champions"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>Warhammer Champions</title>") (:titles results-map)))    
    (is (every? #(= % "<h1>Warhammer Champions</h1>") (:headers results-map)))))

(deftest wh-champions-cards
  (let [urls ["http://ericervin.org/wh_champions/cards?" "http://ericervin.com/wh_champions/cards?"]
        results-map (parse-pages urls)]
    (is (every? #(= 200 %) (:statuses results-map)))
    (is (every? #(= % "<title>  Cards</title>") (:titles results-map)))
    (is (apply = (:trs-counts results-map)))))    

(defn -main []
  (run-tests))
            
   
