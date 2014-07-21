(ns languagegenerator.core
  (:require [clojure.test :as test])
  (:require [languagegenerator.utils :as utils])
  (:require [languagegenerator.phono :as phono :refer [get-phonemes gen-root
                                                       get-sylls assoc-syll]]))

(def phonemic-inventory (get-phonemes))

(def syllables (get-sylls phonemic-inventory))

(def morphemic-inventory (ref {}))
