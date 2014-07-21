(ns languagegenerator.core
  (:require [clojure.test :as test])
  (:require [languagegenerator.utils :as utils])
  (:require [languagegenerator.phono :as phono :refer [get-sylls assoc-syll]]))

(def syllables (get-sylls))

(def morphemic-inventory (ref {}))
