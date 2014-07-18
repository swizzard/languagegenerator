(ns languagegenerator.utils
    (:require [clojure.test :refer [with-test is]]))

(defn- get-adjusted [adjustment max-val] (mod (+ adjustment (rand-int max-val))
                                          (dec max-val)))

(defn map-to-ranges [likelihood-map] (into [] (mapcat #(repeat (last %)
                                      (first %)) likelihood-map)))

(with-test
  (defn get-from ([coll] (let [coll-size (inc (count coll)) coll (vec coll)]
                        (get coll (get-adjusted 0 coll-size))))
                ([adjustment coll] (let [coll-size (inc (count coll))
                                    coll (vec coll)]
                        (get coll (get-adjusted adjustment coll-size)))))
  (is (contains? (set (range 101)) (get-from (range 101))))
  (is (contains? (set (range 101)) (get-from 10 (range 101))))
  )
