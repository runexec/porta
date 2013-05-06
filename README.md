Porta
=====

Porta is a Clojure utility that generates a Clojure abstraction
for an existing Java Class.

How?
=====

Porta's def-methods form retrieves all the methods for a Java
Class and binds them to a lisp-case defn symbol.
```object.doThisThing => (-do-this-thing object)```

Porta's abstraction form retrieves all possible constructors
from a Java Class and defines a multi-fn with a lisp-case symbol.
```clojure
porta.core> (eval (abstraction java.util.Random))
#'porta.core/-java-util-random
```

Install
=====

```bash
git clone https://github.com/runexec/porta
cd porta; lein install
```

Add to your Clojure project and import ```porta.core```
```clojure
[porta/porta "0.1.0-SNAPSHOT"]
```

Examples
=====

```clojure

;; View the Clojure abstraction of java.text.SimpleDateFormat
porta.core> (clojure.pprint/pprint
	     (abstraction java.text.SimpleDateFormat))
(clojure.core/defn
  -java-text-simpledateformat
  ([arg0 arg1]
     (clojure.core/assert
      (clojure.core/some
       clojure.core/identity
       [(clojure.core/every?
         clojure.core/true?
         [(clojure.core/= java.lang.String (clojure.core/type arg0))
          (clojure.core/=
           java.text.DateFormatSymbols
           (clojure.core/type arg1))])
        (clojure.core/every?
         clojure.core/true?
         [(clojure.core/= java.lang.String (clojure.core/type arg0))
          (clojure.core/= java.util.Locale (clojure.core/type arg1))])])
      "Input Restraints")
     (java.text.SimpleDateFormat. arg0 arg1))
  ([arg0]
     (clojure.core/assert
      (clojure.core/every?
       clojure.core/true?
       [(clojure.core/= java.lang.String (clojure.core/type arg0))])
      "Input Restraints")
     (java.text.SimpleDateFormat. arg0))
  ([] (java.text.SimpleDateFormat.)))
nil

;; Define the abstraction
porta.core> (eval
	     (abstraction java.text.SimpleDateFormat))
#'porta.core/-java-text-simpledateformat

;; Input restraints
porta.core> (-java-text-simpledateformat 1)
AssertionError Assert failed: Input Restraints
-- snip error --

porta.core> (-java-text-simpledateformat "h:mm a")
#<SimpleDateFormat java.text.SimpleDateFormat@b4dc7db3>

;; Restraints still apply with polymorphic methods
porta.core> (-java-text-simpledateformat "h:mm:a"
                                         (java.util.Locale. "ENGLISH"))
#<SimpleDateFormat java.text.SimpleDateFormat@b4dc80d9>
porta.core> (-java-text-simpledateformat "h:mm:a" 
					 (java.text.DateFormatSymbols.
					  (java.util.Locale. "ENGLISH")))
#<SimpleDateFormat java.text.SimpleDateFormat@b4dc80d9>
porta.core> (-java-text-simpledateformat "h:mm:a"  "h:mm:a")

AssertionError Assert failed: Input Restraints
-- snip error --

;; Now lets make a Clojure abstraction for Class methods
porta.core> (def sdf (-java-text-simpledateformat "h:mm:a"))
#'porta.core/sdf
porta.core> (take 3 (def-methods java.text.SimpleDateFormat))
(#'porta.core/-get-date-format-symbols #'porta.core/-is-lenient #'porta.core/-get-calendar)

;; Lisp-case converted (.getDateFormateSymbols object)
porta.core> (-get-date-format-symbols sdf)
#<DateFormatSymbols java.text.DateFormatSymbols@840177ab>

;; Lisp-case converted (.toPattern object)
porta.core> (-to-pattern sdf)
"h:mm:a"

;; Building Blocks
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
