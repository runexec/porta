(ns porta.core
  (:use [casing.core])
  (:require [clojure.string :as string])
  (:refer-clojure :exclude [bean]))

(defn bean [object]
  (let [_ (clojure.core/bean object)
        k (keys _)
        v (vals _)
        k-as-str #(->> %
                       str
                       rest
                       (apply str))
        lisp-keys (map #(-> % casing keyword)
                       (map k-as-str k))]
    (zipmap lisp-keys v)))

(defn nmf-bean 
  "Returns a porta.core/bean without methods and fields"
  [object]
  (dissoc (bean object)
          :fields
          :declared-fields
          :methods
          :declared-methods))

(defn characteristic-names [k object]
  (map (memfn getName)
       ((keyword k)
        (bean object))))

(defn case-map [coll]
  (zipmap (map casing coll) coll))

(defn -keys [object] (keys (bean object)))

(defn fq-name [object]
  (-> (bean object)
      :name
      symbol))

(defn fields [object] 
  (let [f (characteristic-names 
           :fields
           object)]
    (-> (map #(-> %
                  (string/lower-case)
                  (.replace "_" "-")
                  keyword)
             f)
        (zipmap f))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Constructors

(defn- constructor-strings [object]
  (let [c (:declared-constructors
           (bean object))
        oc (filter #(-> % (.startsWith "public"))
                   (map (memfn toGenericString) 
                        c))]
    (map #(-> % (.split " ") last)
         oc)))

(defn- constructor-string-to-params [s]
  (let [_ (string/split s #"\(")
        args (first
              (string/split (last _) #"\)"))]
    {:constructor (-> _ first symbol)
     :args (when args
             (->> (string/split args #",")
                  (map symbol)
                  vec))}))

(defn- constructor-to-fn [constructor & [def-name]]
  (let [_ (:constructor constructor)
        args (:args constructor)
        types (distinct args)
        lisp-name (if def-name 
                    (symbol def-name)
                    (-> (str "-" _)
                        (string/replace #"\." "-")
                        (string/lower-case)
                        symbol))
        counts (loop [args args
                      counts {}]
                 (if-let [_ (first args)]
                   (assoc counts
                     (-> _ str keyword)
                     (count
                      (filter #(= % _) args)))
                   counts))
        new-args (for [[-type -count] counts]
                   (->> (map #(str -type %) 
                             (range -count))
                        (map #(string/replace % ":" ""))
                        (map #(string/replace % "." "-"))
                        (map symbol)))]
    (eval
     `(fn ~lisp-name [~@(flatten new-args)]
         (~(symbol (str _ ".")) ~@(flatten new-args))))))

(defn constructors [object]
  (map constructor-string-to-params
       (constructor-strings object)))

(defn construct [object n-construct]
  (constructor-to-fn
   (nth (constructors object)
        n-construct)))

(defn abstraction [object]
  (let [_ (constructors object)
        c (-> _
              first
              :constructor)
        s (-> (str "-" c)
              (.replace "." "-")
              string/lower-case
              symbol)
        multi (map :args _)
        arg-range (map #(mapv
                         (fn [x]
                           (symbol
                            (str "arg" x)))
                         (range %))
                       (map count multi))
        multi-args (map #(cons % 
                               (list `(~(symbol
                                         (str c "."))
                                       ~@%)))
                               arg-range)
        multi-args (distinct multi-args)]
    `(defn ~s
       ~@multi-args)))
                    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Methods

(defn methods [object]
  (let [m (characteristic-names 
           :methods
           object)]
    (case-map m)))


