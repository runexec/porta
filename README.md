porta
=====

To be announced

```clojure
porta.core> (doseq [_ (constructors java.util.Locale)] 
		   (println _))
{:constructor java.util.Locale, :args [java.lang.String]}
{:constructor java.util.Locale, :args [java.lang.String java.lang.String]}
{:constructor java.util.Locale, :args [java.lang.String java.lang.String java.lang.String]}
nil
porta.core> (def -locale (construct java.util.Locale 1))
#'porta.core/-locale
porta.core> (-locale "ENGLISH" "US")
#<Locale english_US>
```