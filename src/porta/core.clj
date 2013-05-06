(ns porta.core
  (:use [casing.core])
  (:require [clojure.string :as string])
  (:refer-clojure :exclude [bean]))

(def ^:dynamic *def-space* (ns porta.core))

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

(defn case-map [coll]
  (zipmap (map casing coll) coll))

(defn -keys [object] (keys (bean object)))

(defn fq-name [object]
  (-> (bean object)
      :name
      symbol))

(defn characteristic-names [k object]
  (map (memfn getName)
       ((keyword k)
        (bean object))))

(defn restraints [coll-types]
  (let [args (interleave
              coll-types
              (mapv #(->> % 
                          (str "arg") 
                          symbol)
                    (-> coll-types
                        count
                        range)))
        assertions (loop [ret []
                          dropping args]
                     (if-not (seq dropping)
                       ret
                       (let [[t a] (take 2 dropping)
                             test `(= ~t (type ~a))]
                         (recur
                          (conj ret test)
                          (nnext dropping)))))]
      {:pre `(every? true? ~assertions)}))

               
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
        new-args (flatten
                  (for [[-type -count] counts]
                    (->> (map #(str -type %) 
                              (range -count))
                         (map #(string/replace % ":" ""))
                         (map #(string/replace % "." "-"))
                         (map symbol))))]
    (eval
     `(fn ~lisp-name [~@new-args]
        (~(symbol (str _ ".")) ~@new-args)))))

(defn constructors [object]
  (map constructor-string-to-params
       (constructor-strings object)))

(defn construct [object n-construct]
  (constructor-to-fn
   (nth (constructors object)
        n-construct)))

(defn build-restraints [object num-of-args]
  (let [coll-args (map :args (constructors object))
        to-build (group-by :count
                           (map #(assoc {}
                                   :count (count %)
                                   :args %)
                                coll-args))
        get-restraints (fn [n]
                         (apply merge-with
                                #(list %1 %2)
                                (map restraints
                                     (map :args
                                          (get to-build n)))))
        restraints (get-restraints num-of-args)
        msg "Input Restraints"]

    (if-not (<= 2 (-> to-build
                     (get num-of-args)
                     count))
      `(assert ~(:pre restraints) ~msg)
      (let [_ (:pre restraints)]
          ;; (some identity *) == (applyr or *) work around
        `(assert
          (some identity ~(vec _))
          ~msg)))))

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
        multi-args (map #(cons % (list
                                  `(~(symbol (str c ".")) ~@%)))
                        arg-range)
        multi-args (distinct multi-args)
        add-tests (filter #(-> % first (not= nil))
                   (loop [ret []
                         test (map #(build-restraints
                                     object
                                     (->  % first count))
                                   multi-args)
                         margs multi-args]
                    (if-not (seq test)
                      ret
                      (let [method (first margs)
                            args (first method)
                            with-test (list args
                                            (build-restraints
                                             object
                                             (count args))
                                            (-> method rest flatten))
                            with-test (if (seq args)
                                        with-test
                                        (list (first with-test)
                                              (-> with-test nnext flatten)))]
                        (recur
                         (conj ret with-test)
                         (rest test)
                         (rest margs))))))]
       `(defn ~s
          ~@add-tests)))

                    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Methods

(defn- method-to-fn [method]
  (let [args (symbol "args")
        object (symbol "object")]
    (eval
     `(defn ~(-> method first symbol)
        [~object & ~args]
        (if ~args
          (apply 
           #(.. ~object
                (~(-> method second symbol) %))
           ~args)
          (.. ~object ~(-> method second symbol)))))))
        
(defn methods [object]
  (let [m (characteristic-names 
           :methods
           object)
        cm (case-map m)]
    (zipmap
     (map #(str "-" %)
          (keys cm))
     (vals cm))))

(defn def-methods [object]
  (map method-to-fn (methods object)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Fields

(defn fields [object] 
  (let [f (characteristic-names 
           :fields
           object)]
    (-> (map #(str "-" %)
             (map #(-> %
                       (string/lower-case)
                       (.replace "_" "-")
                       casing)
                  f))
        (zipmap f))))

(defn- field-to-def [field]
  (eval
   `(def ~(-> (first field) symbol)
      ~(-> field last symbol))))

(defn def-fields [object]
  (let [_ (fields object)
        [k v] [(keys _) (vals _)]
        -symbol (-> object
                    str
                    (string/replace #"class " ""))]
        
  (map field-to-def
       (zipmap k
               (map #(str -symbol "/" %) v)))))
               
  
