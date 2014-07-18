# LanguageGenerator
Fleshed-out, realistic, complex, "naturalistic" fake languages for your novel, RPG, secret society, etc.

(An attempted re-implementation/completion of
[this](https://github.com/swizzard/language_generator) in
[Clojure](http://clojure.org/).)

## Status/Contents
* `utils` contains some utility functions (to be) used elsewhere (**stable**, but might get added to)
  * `map-to-ranges` translates a mapping of values to likelihoods into a vector containing `likelihood` number of `value` for each `value : likelihood` pair
  * `get-from` takes a collection and an optional adjustment and returns a random selection from the collection. The adjustment is used to 'weight' the random number generated
* `phono` contains the phonology-related stuff (**stable-ish**)
  * `str-to-syll-type` maps strings (like "ba") to keyword syllable types (like `:cv`)
  * `get-max` returns the maximum number of possible syllables of a provided type (`[# of consonants in inventory] ** [number of consonants in syllable type] * [# of vowels in inventory] ** [number of vowels in syllable]`)
  * `get-inventories` generates a phonemic inventory (vowels and consonants) and also calculates the maximum number of syllables for each type
* `core` will eventually hold the main/public-facing material for the project (**nascent**)

## Usage
Don't. Just wait.

## Future Development
The project is **very much** under development. Coming soon, hopefully:
* Phonology
  * Syllable generation
  * Root generation
  * Adding/finishing tests
* Morphology
  * Feature selection
  * Mapping morphemes to syllables
  * Synthesis (macros?)
* Syntax
  * Constituent ordering
  * Combining roots + morphemes
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
