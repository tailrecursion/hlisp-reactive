(ns hlisp.reactive.macros
  (:require
    [clojure.walk :as    walk]
    [clojure.set  :refer [union intersection]]))

(create-ns 'hlisp.dom)
(create-ns 'tailrecursion.javelin.macros)
(create-ns 'tailrecursion.javelin.core)

(let [clone   (symbol "hlisp.env" "clone")
      cell    (symbol "tailrecursion.javelin.macros" "cell")
      deref*  (symbol "tailrecursion.javelin.core" "deref*")]

  (defn- listy? [form]
    (or (list? form)
        (= clojure.lang.LazySeq (type form))
        (= clojure.lang.Cons (type form)))) 

  (defn- remove-attr [[tag attrs & children] attr]
    (list* tag (dissoc attrs attr) children))

  (defn- do-reactive-1 [[tag maybe-attrs & children :as form]]
    (let [{dostr :do} (if (map? maybe-attrs) maybe-attrs {})
          exprs       (if (seq dostr) (read-string (str "(" dostr ")")))]
      (if exprs
        `(~deref*
           (let [f# (~clone ~(remove-attr form :do))]
             (~cell (doto f# ~@exprs))))
        form)))

  (defmacro reactive-attributes [form]
    (walk/postwalk #(if (listy? %) (do-reactive-1 %) %) form)))
