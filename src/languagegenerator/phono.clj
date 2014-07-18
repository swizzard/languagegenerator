(ns languagegenerator.phono
  (:require [clojure.test :refer [with-test is]])
  (:require [languagegenerator.utils :refer [get-from map-to-ranges]]))

(def base-vowels [\a \i \u])

(def extra-vowels [\e \o \y \ə \ɪ \ɒ \ɑ \œ \ʏ \ɯ \ʉ \ɨ \ɵ \ɛ \ʊ \ɔ \æ \ɐ
                    \ɜ \ʌ \ø \ɤ])

(def base-consonants [\p \t \k])

(def extra-consonants [\b \c \d \f \g \h \j \k \l \m \n \p \q \r \s \t \v \w
                        \x \z \ʍ \ɹ \θ \ʃ \ð \ɸ \ɣ \ɥ \ɟ \ɬ \ʒ \χ \ç \ʋ \β
                        \ɲ \ʀ \ɢ \ʟ \ʙ \ɴ \ɽ \ʈ \ʂ \ʑ \ŋ \ɱ])

(def ^:private vowel-map {1 5
                          2 5
                          3 30
                          4 25
                          5 15
                          6 8
                          7 7
                          8 3
                          9 2})
(def ^:private consonant-map {2 5
                              3 10
                              4 20
                              5 25
                              6 10
                              7 10
                              8 10
                              9 10})




(with-test
  (defn- get-inventory [starting source max-size]
    (cond
      (>= max-size (+ (count starting) (count source)))
      ; if max-size is greater than the total number of consonants available, return 'em all
      (into starting source)

      (<= max-size 0)
      (throw (IllegalArgumentException. "max-size must be greater than 0"))

      (< max-size (count starting))
      ; if max-size is less than the number of base elements, take a random sample of the base elements
      (take max-size (shuffle starting))

      :else
      ; randomly add extra elements to the base, casting to a set to ensure uniqueness
      (let [source-size (inc (count source))]
        (loop [inventory (set starting)]
          (if (>= (count inventory) max-size)
            (vec inventory) ; return a vector for access via rand-int
            (recur (conj inventory (get-from 0 source))))))))

  ; make sure calling it with max-size=3 returns the base inventories
  (is (= base-consonants (get-inventory base-consonants extra-consonants 3)))
  (is (= base-vowels (get-inventory base-vowels extra-vowels 3)))
  ; make sure max-size<=0 throws the error
  (is (thrown? IllegalArgumentException (get-inventory base-vowels extra-vowels 0)))
  ; make sure we're returning the right number of elements
  (is (= 2 (count (get-inventory base-vowels extra-vowels 2))))
  (is (= 10 (count (get-inventory base-consonants extra-consonants 10))))
  (is (= 25 (count (get-inventory base-vowels extra-vowels 25))))
  ; make sure we max out appropriately
  (is (= 25 (count (get-inventory base-vowels extra-vowels 30))))
  ; make sure we're pulling from the right places
  (is (every? #(contains? (set (into base-vowels extra-vowels)) %)
        (get-inventory base-vowels extra-vowels 10)))
  (is (every? #(contains? (set (into base-consonants extra-consonants)) %)
        (get-inventory base-vowels extra-vowels 10)))
  ; make sure we're only pulling from the bases for max-size<3
  (is (every? #(contains? (set base-vowels) %)
        (get-inventory base-vowels extra-vowels 2)))
  (is (every? #(contains? (set base-vowels) %)
        (get-inventory base-consonants extra-consonants 2)))
  )

(defn- gen-phonemes [vowel-adjustment consonant-adjustment
                     vowel-likelihoods consonant-likelihoods]
    {:v (get-inventory base-vowels extra-vowels
          (get-from vowel-adjustment (map-to-ranges
            (merge vowel-map vowel-likelihoods))))
     :c (get-inventory base-consonants extra-consonants
          (get-from consonant-adjustment (map-to-ranges
            (merge vowel-map vowel-likelihoods))))}))

(defn get-phonemes
  "Get vowel and consonant inventories. Mappings between counts and likelihoods
  are translated into vectors containing likelihood number of counts, from which
  one count is randomly selected. This selection occurs separately for vowels
  and consonants, and can be weighted by supplying :vowel-adjustment or
  :consonant-adjustment keyword arguments. Maps passed in with the
  :vowel-likelihoods or :consonant-likelihoods keyword arguments will be used
  to update the pre-definied mappings. Note that this means you have to
  explicitly 'clobber' any counts you want to change."
  ([] (gen-phonemes 0 0 {} {}))
  ([vowel-adjustment-or-map] (if (map? vowel-adjustment-or-map)
                              (gen-phonemes 0 0 vowel-adjustment-or-map {})
                              (gen-phonemes vowel-adjustment-or-map 0 {} {})))
  ([vowel-adjustment-or-map consonant-adjustment-or-map]
    (if (map? vowel-adjustment-or-map)
      (if (map? consonant-adjustment-or-map)
        (gen-phonemes 0 0 vowel-adjustment-or-map consonant-adjustment-or-map))
      (gen-phonemes 0 consonant-adjustment-or-map vowel-adjustment-or-map {}))
    (if (map? consonant-adjustment-or-map) (gen-phonemes vowel-adjustment-or-map
                                            0 {} consonant-adjustment-or-map)
          (gen-phonemes vowel-adjustment-or-map consonant-adjustment-or-map
            {} {})))
  ([vowel-adjustment consonant-adjustment vowel-map]
    (gen-phonemes vowel-adjustment consonant-adjustment vowel-map {}))
  ([vowel-adjustment consonant-adjustment vowel-map consonant-map]
    (gen-phonemes vowel-adjustment consonant-adjustment vowel-map
      consonant-map))
  ([& {:keys [vowel-adjustment
             consonant-adjustment
             vowel-likelihoods
             consonant-likelihoods]
             :or {vowel-adjustment 0
                  consonant-adjustment 0
                  vowel-likelihoods {}
                  consonant-likelihoods {}}}]
   (gen-phonemes vowel-adjustment consonant-adjustment vowel-likelihoods
    consonant-likelihoods)))


(defn get-maxes [phonemes] (let [v-count (count (:v phonemes))
                                 c-count (count (:c phonemes))]
                            (assoc phonemes :max-syllables
                              {:v v-count
                               :cv (* v-count c-count)
                               :cvc (* v-count c-count v-count)
                               :cvv (* v-count v-count c-count)
                               :cvvc (* v-count v-count c-count c-count)})))

(defn get-inventories [& args] (get-maxes (apply get-phonemes args)))
