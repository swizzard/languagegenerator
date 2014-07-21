(defproject languagegenerator "0.1.0"
  :description "Generate realistic, complex fake languages for fantasy novels, &c."
  :url "http://samraker.com"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                  [org.clojure/math.combinatorics "0.0.8"]
                  [info.sunng/debug "0.1.1"]]
  :plugins [[lein-gorilla "0.2.0"]  ; gorilla for a better repl
            [jonase/eastwood "0.1.4"]  ; eastwood for better testing
            ])                         ; NB: eastwood's official docs say to
                                       ; add it to your .lein/profiles.clj file.
