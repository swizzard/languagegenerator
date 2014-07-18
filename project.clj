(defproject languagegenerator "0.1.0"
  :description "Generate realistic, complex fake languages for fantasy novels, &c."
  :url "http://samraker.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[lein-gorilla "0.2.0"]  ; gorilla for a better repl
            [jonase/eastwood "0.1.4"]  ; eastwood for better testing
            ])                         ; NB: eastwood's official docs say to
                                       ; add it to your .lein/profiles.clj file.