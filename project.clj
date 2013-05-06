(defproject tailrecursion/hlisp-reactive "1.0.3-SNAPSHOT"
  :description  "Javelin FRP for HLisp"
  :url          "https://github.com/tailrecursion/hlisp-reactive/"
  :license      {:name "Eclipse Public License"
                 :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[tailrecursion/javelin "1.0.0-SNAPSHOT"]
                 [hlisp-jquery "0.1.0-SNAPSHOT"]]
  :plugins      [[lein-cljsbuild "0.3.0"]]
  :source-paths ["src/clj" "src/cljs"]
  :cljsbuild    {:builds {:test {:source-paths  ["src/clj" "src/cljs" "test"]
                                 :jar           false
                                 :compiler      {:output-to "test/test.js"
                                                 ;; :optimizations :whitespace
                                                 ;; :pretty-print true
                                                 :optimizations :advanced
                                                 :warnings true}}}
                 :test-commands {"unit" ["phantomjs" "test/runner.js"]}})
