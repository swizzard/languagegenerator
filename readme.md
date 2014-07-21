# LanguageGenerator
Fleshed-out, realistic, complex, "naturalistic" fake languages for your novel, RPG, secret society, etc.

(An attempted re-implementation/completion of
[this](https://github.com/swizzard/language_generator) in
[Clojure](http://clojure.org/).)

## Status/Contents
* `utils` contains some utility functions (to be) used elsewhere (**stable**, but might get added to)
  * `map-to-ranges` translates a mapping of values to likelihoods into a vector containing `likelihood` number of `value` for each `value : likelihood` pair.
  * `get-from` takes a collection and an optional adjustment and returns a random selection from the collection. The adjustment is used to 'weight' the random number generated.
* `phono` contains the phonology-related stuff (**stable-ish**)
  * `get-phonemes` generates a phonemic inventory (vowels and consonants).
  * `get-sylls` takes a phonemic inventory as input and calculates all possible syllables (V, CV, VC, CVC, CVV, CVVC).
  * `*max-root-length*` default maximum root length (in syllables).
  * `gen-root` takes a syllabic inventory and optional `max-length` as inputs and returns a root comprised of at most `max-length` syllables. `max-length` defaults to value of `max-root-length` if not provided.
  * `assoc-syll` associates a random syllable with a provided morpheme. If a syllable type is provided as an argument, a syllable of that type will be selected from the inventory; otherwise, a syllable of the type with the fewest instances will be selected. Either way, the chosen syllable will be removed from the syllabic inventory.
* `core` will eventually hold the main/public-facing material for the project (**nascent**)

## Usage
The project is DEFINITELY not ready for prime, or any, time. If you want to play around with it, however, I can't stop you.

#### Notes for if you do want to play around with it
* Ultimately it will be on the end-user to 'vet' the output, which is intended solely as suggestions/inspiration. You don't have to accept anything it spits out at you if you don't like it. On the other hand, `phono/get-phonemes` offers a significant degree of parameter-tweaking while still behaving in a randomized, "naturalistic" fashion. Its to-be-written morphological and syntactic cousins will behave in basically the same way (see the [Python version](https://github.com/swizzard/language_generator) for examples.)
* The project uses (a subset of) [IPA](https://en.wikipedia.org/wiki/IPA) in its output. If you're not familiar with IPA, [this](https://en.wikipedia.org/wiki/IPA#Letters) is as good a place to start as any.
* The project can't (and probably never will) decide where accents go. It also doesn't do anything with tones. (That being said, tone- and accent-related PRs are totally welcome!) Clojure also seems to have a problem with diacritics, so, e.g., nasality isn't implemented/represented at all, either.
* CVV and CVVC syllables are randomly generated, and are treated as 'first-class' syllables. This is mostly in order to expand the potential syllabic options for morpheme assignment (to avoid possibly generating languages with more features than morphemes.) My feelings won't be hurt if you decide some or all of the VV sequences created are actually in separate syllables instead of diphthongs.
* Speaking of which, in my head, I've been treating doubles as [long vowels](https://en.wikipedia.org/wiki/Vowel_length)/[geminates](https://en.wikipedia.org/wiki/Gemination), e.g. `aa` -> `aː`, `kk` -> `kː`. You don't have to do this. (FWIW, implementing some kind of epenthesis/assimilation functionality is theoretically on the agenda--this would be another great PR opportunity, wink wink.)

## Future Development
The project is **very much** under development. Coming soon, hopefully:
* Phonology
  * <strike>Syllable generation</strike> (`phono/get-sylls`)
  * <strike>Root generation</strike> (`phono/gen-root`)
  * Adding/finishing tests
  * Making phonemic inventory generation more realistic?
  * Epenthesis/assimilation?
* Morphology
  * Feature selection
  * <strike>Mapping morphemes to syllables</strike> (`phono/assoc-syll`)
  * Tests
* Syntax
  * Constituent ordering
  * Combining roots + morphemes
    * Synthesis (macros?)
  * Tests
* Other
  * Persistence ([Korma](http://sqlkorma.com/)?)
  * Frontend
    * Web
    * GUI?

## &c.
### Contact
Interested/intrigued? HMU at &lt;sam dot raker at gmail dot com&gt; or on [twitter](http://twitter.com/swizzard).

### Testing
I'm trying to write inline tests using `clojure.test`. Additional linting is done with [Eastwood](https://github.com/jonase/eastwood), which is pretty cool.

### License  
Copyright © 2014 [Sam Raker](http://samraker.com).  
This work is free. You can redistribute it and/or modify it under the
terms of the Do What The Fuck You Want To Public License, Version 2,
as published by Sam Hocevar. See the COPYING file for more details.
