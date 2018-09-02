(defproject clj_of_testing "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.9.1"]
                 [enlive "1.1.6"]]  
  :main ^:skip-aot clj-of-testing.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
