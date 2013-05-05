porta
=====

To be announced

```clojure
porta.core> (clojure.pprint/pprint
	     (abstraction java.util.Locale))
(clojure.core/defn
 -java-util-locale
 ([arg0] (java.util.Locale. arg0))
 ([arg0 arg1] (java.util.Locale. arg0 arg1))
 ([arg0 arg1 arg2] (java.util.Locale. arg0 arg1 arg2)))
nil
porta.core> (eval (abstraction java.util.Locale))
#'porta.core/-java-util-locale
porta.core> (-java-util-locale "ENGLISH")
#<Locale english>
porta.core> (-java-util-locale "ENGLISH" "US")
#<Locale english_US>
porta.core> (-java-util-locale "ENGLISH" "US" "JP")
#<Locale english_US_JP>

porta.core>
(doseq [_ (constructors java.util.Locale)] 
  (println _))
{:constructor java.util.Locale, :args [java.lang.String]}
{:constructor java.util.Locale, :args [java.lang.String java.lang.String]}
{:constructor java.util.Locale, :args [java.lang.String java.lang.String java.lang.String]}
nil
porta.core> (def -locale (construct java.util.Locale 1))
#'porta.core/-locale
porta.core> (-locale "ENGLISH" "US")
#<Locale english_US>
porta.core> 
(let [one-arg (construct java.util.Locale 0)
      two-arg (construct java.util.Locale 1)
      three-arg (construct java.util.Locale 2)]
  (println (one-arg "ENGLISH"))
  (println (two-arg "ENGLISH" "US"))
  (println (three-arg "ENGLISH" "US" "JP")))
#<Locale english>
#<Locale english_US>
#<Locale english_US_JP>
nil
porta.core> 
```
