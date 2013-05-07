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

Fields are treated the same after calling the def-fields form.
```clojure
porta.core> (take 3 (def-fields java.text.SimpleDateFormat))
(#'porta.core/-month-field #'porta.core/-millisecond-field #'porta.core/-day-of-year-field)
porta.core> -day-of-year-field
10
porta.core> -month-field
2
```

Install
=====

```bash
git clone https://github.com/runexec/porta
cd porta; lein install
```

Add to your Clojure project and ```(use porta.core)```
```clojure
[porta/porta "0.1.0-SNAPSHOT"]
```

If you don't have the casing dependency, check this out https://github.com/runexec/casing

Examples
=====

View the Clojure abstraction of java.text.SimpleDateFormat

```clojure
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
```

Define the abstraction

```clojure
porta.core> (eval
	     (abstraction java.text.SimpleDateFormat))
#'porta.core/-java-text-simpledateformat
```

Input restraints

```clojure
porta.core> (-java-text-simpledateformat 1)
AssertionError Assert failed: Input Restraints
-- snip error --

porta.core> (-java-text-simpledateformat "h:mm a")
#<SimpleDateFormat java.text.SimpleDateFormat@b4dc7db3>
```

Restraints still apply with polymorphic methods

```clojure
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
```

Make a Clojure abstraction for Class methods

```clojure
porta.core> (def sdf (-java-text-simpledateformat "h:mm:a"))
#'porta.core/sdf
porta.core> (take 3 (def-methods java.text.SimpleDateFormat))
(#'porta.core/-get-date-format-symbols #'porta.core/-is-lenient #'porta.core/-get-calendar)
```

Lisp-case converted (.getDateFormateSymbols object)

```clojure
porta.core> (-get-date-format-symbols sdf)
#<DateFormatSymbols java.text.DateFormatSymbols@840177ab>
```

Lisp-case converted (.toPattern object)

```clojure
porta.core> (-to-pattern sdf)
"h:mm:a"
```

Make a Clojure abstraction for the Class Fields

```clojure
porta.core> (take 3 (def-fields java.text.SimpleDateFormat))
(#'porta.core/-month-field #'porta.core/-millisecond-field #'porta.core/-day-of-year-field)
porta.core> -day-of-year-field
10
porta.core> -month-field
2
```

Call def-all = def-methods, def-fields, (evail (abstraction object))

```clojure
(def-all java.text.SimpleDateFormat)
```

Building Blocks

```clojure
porta.core> (clojure.pprint/pprint
	     (fields java.text.SimpleDateFormat))
{"-month-field" "MONTH_FIELD",
 "-millisecond-field" "MILLISECOND_FIELD",
 "-day-of-year-field" "DAY_OF_YEAR_FIELD",
 "-medium" "MEDIUM",
 "-week-of-month-field" "WEEK_OF_MONTH_FIELD",
 "-day-of-week-in-month-field" "DAY_OF_WEEK_IN_MONTH_FIELD",
 "-date-field" "DATE_FIELD",
 "-long" "LONG",
 "-hour0-field" "HOUR0_FIELD",
 "-hour1-field" "HOUR1_FIELD",
 "-minute-field" "MINUTE_FIELD",
 "-second-field" "SECOND_FIELD",
 "-short" "SHORT",
 "-timezone-field" "TIMEZONE_FIELD",
 "-default" "DEFAULT",
 "-era-field" "ERA_FIELD",
 "-week-of-year-field" "WEEK_OF_YEAR_FIELD",
 "-year-field" "YEAR_FIELD",
 "-day-of-week-field" "DAY_OF_WEEK_FIELD",
 "-hour-of-day0-field" "HOUR_OF_DAY0_FIELD",
 "-full" "FULL",
 "-hour-of-day1-field" "HOUR_OF_DAY1_FIELD",
 "-am-pm-field" "AM_PM_FIELD"}
nil
porta.core> (clojure.pprint/pprint (-methods java.text.SimpleDateFormat))
{"-get-date-format-symbols" "getDateFormatSymbols",
 "-is-lenient" "isLenient",
 "-get-calendar" "getCalendar",
 "-set2-digit-year-start" "set2DigitYearStart",
 "-wait" "wait",
 "-set-number-format" "setNumberFormat",
 "-apply-pattern" "applyPattern",
 "-format" "format",
 "-parse-object" "parseObject",
 "-parse" "parse",
 "-get-time-zone" "getTimeZone",
 "-notify-all" "notifyAll",
 "-to-pattern" "toPattern",
 "-set-date-format-symbols" "setDateFormatSymbols",
 "-equals" "equals",
 "-get-date-instance" "getDateInstance",
 "-clone" "clone",
 "-get-time-instance" "getTimeInstance",
 "-get-available-locales" "getAvailableLocales",
 "-apply-localized-pattern" "applyLocalizedPattern",
 "-get-class" "getClass",
 "-set-time-zone" "setTimeZone",
 "-set-calendar" "setCalendar",
 "-notify" "notify",
 "-to-string" "toString",
 "-get2-digit-year-start" "get2DigitYearStart",
 "-get-number-format" "getNumberFormat",
 "-to-localized-pattern" "toLocalizedPattern",
 "-get-instance" "getInstance",
 "-get-date-time-instance" "getDateTimeInstance",
 "-format-to-character-iterator" "formatToCharacterIterator",
 "-hash-code" "hashCode",
 "-set-lenient" "setLenient"}```
nil
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
