(ns languagegenerator.phono
  (:require [clojure.test :refer :all])
  (:require [languagegenerator.utils :refer [get-from map-to-ranges]])
  (:require [clojure.math.combinatorics :refer [cartesian-product]]))

(set! *assert* true)

(def ^:dynamic *max-root-length* 4)

(def ^:private vowels [\a \a \a \a \a \a \i \i \i \i \i \i \u \u \u \u \u \u
                       \e \o \y \ə \ɪ \ɒ \ɑ \œ \ʏ \ɯ \ʉ \ɨ \ɵ \ɛ \ʊ \ɔ \æ \ɐ
                       \ɜ \ʌ \ø \ɤ])

(def ^:private consonants [\p \p \p \p \p \p \p \p \p \p \p \p
                           \t \t \t \t \t \t \t \t \t \t \t \t
                           \k \k \k \k \k \k \k \k \k \k \k \k
                           \b \c \d \f \g \h \j \k \l \m \n \p
                           \q \r \s \t \v \w \x \z \ʍ \ɹ \θ \ʃ
                           \ð \ɸ \ɣ \ɥ \ɟ \ɬ \ʒ \χ \ç \ʋ \β
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
  (defn- get-inventory [source max-size]
    (cond
      (>= max-size (count (distinct source)))
      ; if max-size is greater than the total number of consonants available, return 'em all
      (vec (distinct source))

      (<= max-size 0)
      (throw (IllegalArgumentException. "max-size must be greater than 0"))

      :else
      ; randomly add extra elements, casting to a set to ensure uniqueness
      (let [source-size (inc (count source))]
        (loop [inventory #{}]
          (if (>= (count inventory) max-size)
            (vec inventory) ; return a vector for access via rand-int
            (recur (conj inventory (get-from source))))))))

  ; make sure max-size<=0 throws the error
  (is (thrown? IllegalArgumentException (get-inventory vowels 0)))
  ; make sure we're returning the right number of elements
  (is (= 2 (count (get-inventory vowels 2))))
  (is (= 10 (count (get-inventory consonants 10))))
  (is (= 25 (count (get-inventory vowels 25))))
  ; make sure we max out appropriately
  (is (= 25 (count (get-inventory vowels 30))))
  ; make sure we're pulling from the right places
  (is (every? #(contains? (set vowels) %)
        (get-inventory vowels 10)))
  (is (every? #(contains? (set consonants) %)
        (get-inventory consonants 10)))
  )

(with-test
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
          (or (nil? vs) (<= 100 (apply + vs))))]}
    {:v (get-inventory vowels
          (get-from vowel-adjustment (map-to-ranges
            (merge vowel-map vowel-likelihoods))))
     :c (get-inventory consonants
          (get-from consonant-adjustment (map-to-ranges
            (merge vowel-map vowel-likelihoods))))})
(is (every? #(contains? (set vowels) %) (:v (gen-phonemes 0 {} 0 {}))))
(is (every? #(contains? (set consonants) %) (:c (gen-phonemes 0 {} 0 {}))))
)


(defn get-phonemes
  "Wrapper around gen-phonemes to handle arities properly.
  :return: map {:v [vector-of-vowels] :c [vector-of-consonants]}"
  [& args]
  (let [args (vec args)
        default-args-atom (atom {:vowel-adjustment 0
                                 :vowel-likelihoods {}
                                 :consonant-adjustment 0
                                 :consonant-likelihoods {}})
        kwds [:a :b :c :d]]
    (do
      (dorun (map-indexed (fn [idx arg] (if (keyword? arg)
                                          (swap! default-args-atom assoc arg
                                            (get args (inc idx)))
                                          (if (not (keyword? (get args
                                                              (dec idx))))
                                                (swap! default-args-atom assoc
                                                  (get kwds idx) arg))))
              args))
   (gen-phonemes (:vowel-adjustment @default-args-atom)
                 (:vowel-likelihoods @default-args-atom)
                 (:consonant-adjustment @default-args-atom)
                 (:consonant-likelihoods @default-args-atom)))))

(defn- strify [ls] (map #(apply str %) ls))

(defn- cp
  ([coll1] (map str coll1))
  ([coll1 coll2] (strify (cartesian-product coll1 coll2)))
  ([coll1 coll2 coll3] (strify (cartesian-product
                                (strify (cartesian-product coll1 coll2))
                                coll3)))
  ([coll1 coll2 coll3 coll4] (strify
                              (cartesian-product
                                (strify (cartesian-product coll1 coll2))
                                  (strify (cartesian-product coll3 coll4)))))
 )

(with-test
(defn get-sylls [phonemes] (let [inventory @phonemes]
                             (ref {:v (map str (:v inventory))
                             :cv (cp (:c inventory) (:v inventory))
                             :vc (cp (:v inventory) (:c inventory))
                             :cvc (cp (:c inventory) (:v inventory)
                                   (:c inventory))
                             :cvv (cp (:c inventory) (:v inventory)
                                   (:v inventory))
                             :cvvc (cp (:c inventory) (:v inventory)
                                    (:v inventory) (:c inventory))})))
(is (every? #(= java.lang.String (type %) (:v (get-sylls (get-phonemes))))))
(is (every? #(= java.lang.String (type %) (:cv (get-sylls (get-phonemes))))))
(is (every? #(= java.lang.String (type %) (:vc (get-sylls (get-phonemes))))))
(is (every? #(= java.lang.String (type %) (:cvc (get-sylls (get-phonemes))))))
(is (every? #(= java.lang.String (type %) (:cvv (get-sylls (get-phonemes))))))
(is (every? #(= java.lang.String (type %) (:cvvc (get-sylls (get-phonemes))))))
(is (every? #(= 1 (count %) (:v (get-sylls (get-phonemes))))))
(is (every? #(= 2 (count %) (:cv (get-sylls (get-phonemes))))))
(is (every? #(= 2 (count %) (:vc (get-sylls (get-phonemes))))))
(is (every? #(= 3 (count %) (:cvc (get-sylls (get-phonemes))))))
(is (every? #(= 3 (count %) (:cvv (get-sylls (get-phonemes))))))
(is (every? #(= 4 (count %) (:cvvc (get-sylls (get-phonemes))))))
(is (every? #(contains? (set vowels) %) (:v (get-sylls (get-phonemes)))))
(is (every? #(contains? (set consonants) %) (map first (:cv (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set consonants) %) (map (fn [c] (get c 1)) (:cv (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set vowels) %) (map first (:vc (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set consonants) %) (map (fn [c] (get c 1)) (:vc (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set consonants) %) (map first (:cvv (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set vowels) %) (map (fn [c] (get c 1)) (:cvv (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set vowels) %) (map (fn [c] (get c 2)) (:cvv (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set consonants) %) (map first (:cvvc (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set vowels) %) (map (fn [c] (get c 1)) (:cvvc (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set vowels) %) (map (fn [c] (get c 2)) (:cvvc (get-sylls
                                                          (get-phonemes))))))
(is (every? #(contains? (set consonants) %) (map (fn [c] (get c 3)) (:cvvc (get-sylls
                                                          (get-phonemes))))))
)

(with-test
(defn gen-root
  "Generate a random root comprised of randomly-selected consonants and vowels.
  The algorithm is weighted to favor vowels over consonants 2:1, and there's a
  safety valve in place to make sure the result is at least half vowels."
  ([sylls max-length] (let [max-size (inc (rand-int max-length))
                      syllables (apply concat (vals @sylls))]
                    (loop [root-sylls []]
                    (if (= (count root-sylls) max-size)
                      (apply str (vec root-sylls))
                      (recur (conj root-sylls (first (shuffle syllables))))
                      ))))
  ([sylls] (gen-root sylls *max-root-length*)))
(is (every? #(<= (* 4 *max-root-length*) (count %)) (repeatedly 10 (fn [] (gen-root
                                                          (get-sylls
                                                            (get-phonemes)))))))
(is (every? #(<= (* 4 3) (count %)) (repeatedly 10 (fn [] (gen-root
                                                      (get-sylls (get-phonemes))
                                                            3)))))
(is (= java.lang.String (type (gen-root (get-sylls (get-phonemes))))))
(is (= java.lang.String (type (gen-root 5 (get-sylls (get-phonemes))))))
)

(defn assoc-syll
  ([morpheme syll-type syllables morpheme-inventory]
    (let [sylls (shuffle (get @syllables syll-type))
          syll (first sylls)
          remaining (next sylls)]
      (dosync (alter morpheme-inventory assoc morpheme syll)
              (if (nil? remaining)
                  (alter syllables dissoc syll-type)
                  (alter syllables assoc syll-type remaining)))))
  ([morpheme syllables morpheme-inventory]
    (let [syll-type (first (first (sort-by #(count (next %)) @syllables)))]
      (assoc-syll morpheme syll-type syllables morpheme-inventory))))

(deftest test-assoc-syll
  (let [syllables (get-sylls (get-phonemes))
        orig-sylls (ref @syllables)
        morphemes (ref {})
        counts {:v (count (:v @orig-sylls)) :cv (count (:cv @orig-sylls))
                :vc (count (:vc @orig-sylls)) :cvv (count (:cvv @orig-sylls))
                :cvvc (count (:cvvc @orig-sylls))}]
    (assoc-syll :masc :cv syllables morphemes)
    (assoc-syll :fem syllables morphemes)
    (is (contains? (set (:cv @orig-sylls)) (:masc @morphemes)))
    (is (contains? (set (:v @orig-sylls)) (:fem @morphemes)))
    (is (= (dec (:v counts)) (count(:v @syllables))))
    (is (= (dec (:cv counts)) (count (:cv @syllables))))))
