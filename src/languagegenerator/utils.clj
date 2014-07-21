(ns languagegenerator.utils
    (:require [clojure.test :refer [with-test is]])
    (:require [debug.core :refer [debug]]))

(with-test
  (defn- get-adjusted [adjustment max-val] (min (+ adjustment
                                                  (rand-int max-val))
                                            (dec max-val)))
  (is (<= 10 (get-adjusted 0 10)))
  (is (<= 10 (get-adjusted 5 10)))
  (is (= 10 (get-adjusted 10 10)))
)

(defn map-to-ranges [likelihood-map] (into [] (mapcat #(repeat (last %)
                                      (first %)) likelihood-map)))

(with-test
  (defn get-from ([coll] (let [coll-size (count coll) coll (vec coll)]
                        (get coll (get-adjusted 0 coll-size))))
                ([adjustment coll] (let [coll-size (count coll)
                                    coll (vec coll)
                                    get-val (get-adjusted adjustment coll-size)]
                        (get coll get-val))))
  (is (every? #(contains? (set (range 100)) %) (dotimes [_ 100]
                                                (get-from (range 100)))))
  (is (every? #(contains? (set (range 100)) %) (dotimes [_ 100]
                                              (get-from 10 (range 100)))))
  (is (= (max 100 (get-from 100 (range 100)))))
  )
