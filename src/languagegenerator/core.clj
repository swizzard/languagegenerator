(ns languagegenerator.core
  (:require [clojure.test :as test]
            [languagegenerator [utils :as utils]
                               [phono :as phono :refer [get-inventories]]]))

(def phonemic-inventory (atom (get-inventories)))
