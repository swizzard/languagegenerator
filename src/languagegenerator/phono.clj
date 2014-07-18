(ns languagegenerator.phono
  (:require [clojure.test :refer [with-test is]])
  (:require [languagegenerator.utils :refer [get-from map-to-ranges]]))

(set! *assert* true)

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

(defn- gen-phonemes
  "Generate vowel and consonant inventories. There's no real need to call this
  directly; get-phonemes is a wrapper around this function to handle multiple
  arities.
  :param vowel-adjustment: 'weight' to change random selection of vowels
  :type vowel-adjustment: integer
  :param consonant-adjustment: 'weight' to change random selection of consonants
  :type consonant-adjustment: integer
  :param vowel-likelihoods: mapping of vowel inventory sizes to likelihoods,
    used to modify the default mapping
  :type vowel-likelihoods: map
  :param consonant-likelihoods: mapping of consonant inventory sizes to
    likelihoods, used to modify the default mapping
  :return: map {:v [vector-of-vowels] :c [vector-of-vowels]}
  :NB: likelihood maps are merged with the respective default map; pre-existing
    count likelihoods must be explicitly clobbered, and the likelihoods resulting
    from the merger must add up to 100"
  [vowel-adjustment vowel-likelihoods consonant-adjustment consonant-likelihoods]
  {:pre [(let [vs (vals vowel-likelihoods)]
          (or (nil? vs) (<= 100 (apply + vs))))
         (let [vs (vals consonant-likelihoods)]
          (or (nil? vs) (<= 100 (apply + vs))))]
   :post [(= 100 (count (:v %))) (= 100 (count (:c %)))]}
    {:v (get-inventory base-vowels extra-vowels
          (get-from vowel-adjustment (map-to-ranges
            (merge vowel-map vowel-likelihoods))))
     :c (get-inventory base-consonants extra-consonants
          (get-from consonant-adjustment (map-to-ranges
            (merge vowel-map vowel-likelihoods))))}))

(defn- get-phonemes
  "Wrapper around gen-phonemes to handle arities properly. Supports the
  following signatures:
    (get-phonemes) -- no weighting, default likelihood maps
    (get-phonemes i) -- weight vowels +i
    (get-phonemes m) -- alter vowel likelihood mapping by merging m
    (get-phonemes i j) -- weight vowels +i, weight consonants +j
    (get-phonemes i m) -- weight vowels +i, alter vowel likelihood mapping by
      merging m
    (get-phonemes m i) -- weight consonants +i, alter consonant likelihood
      mapping by merging m
    (get-phonemes i m j) -- weight vowels +i, weight consonants +j, adjust
      vowel mapping by merging m
    (get-phonemes i m j n) -- weight vowels +i, weight consonants +j, adjust
      vowel mapping by merging m and consonant mapping by merging n
  :return: map {:v [vector-of-vowels] :c [vector-of-consonants]}"
  ([] (gen-phonemes 0 {} 0 {}))
  ([vowel-adjustment-or-map] (if (map? vowel-adjustment-or-map)
                              (gen-phonemes 0 0 vowel-adjustment-or-map {})
                              (gen-phonemes vowel-adjustment-or-map 0 {} {})))
  ([one two]
    (cond
      (and (map? one) (map? two))
        (gen-phonemes 0 one 0 two)
      (and (number? one) (map? two))
        (gen-phonemes one two 0 {})
      (and (map? one) (number? two))
        (gen-phonemes 0 {} two one)
      :else (gen-phonemes one {} two {})))
  ([vowel-adjustment vowel-map consonant-adjustment]
    (gen-phonemes vowel-adjustment consonant-adjustment vowel-map {}))
  ([vowel-adjustment vowel-map consonant-adjustment consonant-map]
    (gen-phonemes vowel-adjustment consonant-adjustment vowel-map]
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


(defn- calculate-maxes
  "Calculate the maximum number of possible distinct syllables of each type,
  given the phonemic inventory.
  :param phonemes: a phonemic inventory
  :type phonemes: map
  :return map (phonemes + :max-syllables map)"
   [phonemes] (let [v-count (count (:v phonemes))
                                 c-count (count (:c phonemes))]
                            (assoc phonemes :max-syllables
                              {:v v-count
                               :vc (* v-count c-count)
                               :cv (* v-count c-count)
                               :cvc (* v-count c-count v-count)
                               :cvv (* v-count v-count c-count)
                               :cvvc (* v-count v-count c-count c-count)})))

(defn str-to-syll-type
  "Convert a syllable string to a syllable type
  :param string: a syllable
  :type string: string
  :return: keyword (:v/:cv/:cvc/:cvvc)"
   [string] (let [v (set (into base-vowels extra-vowels))
                                       elems (seq string)]
                                  (keyword (apply str (for [elem elems]
                                              (if (contains? v elem) \V) \C)))))

(defn get-max
  "List the maximum number of distinct syllables or check if there are any
    possible syllables of a given type remaining. Supports the following
    signatures:
    (get-max) -- return max-syllables map
    (get-max :s) -- return maximum number of syllables of type represented by :s
    (get-max m :s) -- return true if morpheme-to-syllable mapping m contains
      fewer syllables of type represented by :s than the maximum number, else
      false
    :return: map, int, or bool, depending on signature"
  ([inventory] (:max-syllables inventory))
  ([inventory syll-type] (syll-type (get-max inventory)))
  ([inventory sylls syll-type] (let [max-count (get-max inventory syll-type)
                                     syllables (filter #(= syll-type %)
                                                (map str-to-syll-type sylls))]
                                (<= (count syllables) max-count))))

(defn get-inventories
  "Generates phonemic inventory and calculates maximum syllable inventories.
  Arguments are same as those passed to get-phonemes (q.v.)
  :return: map {:v [vector-of-vowels] :c [vector-of-consonants]
    :max-syllables {map-of-maximum-number-of-syllables}}"
  [& args] (calculate-maxes (apply get-phonemes args)))
