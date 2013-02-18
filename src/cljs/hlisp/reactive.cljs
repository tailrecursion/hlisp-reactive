(ns hlisp.reactive
  (:require-macros
    [tailrecursion.javelin.macros :refer [cell]])
  (:require
    [tailrecursion.javelin        :as j]
    [hlisp.env                    :as hl]
    [hlisp.util                   :as hu]))


(declare ids)

(defn timeout [f t] (.setTimeout js/window f t))

(defn id      [e] (peek (ids e)))
(defn ids     [e] (.-ids e))
(defn id!     [e] (if-not (seq (ids e)) (hl/clone e) e))
(defn is-jq?  [e] (string? (.-jquery e)))

(->
  (js/jQuery "body")
  (.on "submit" (fn [event] (.preventDefault event))))

(defn filter-id
  [x]
  (fn [v]
    (< 0 (->
           (js/jQuery (.-target v))
           (.parentsUntil "body")
           (.andSelf)
           (.filter (str "[data-hl~='" x "']"))
           (.size)))))

(defn filter-not-disabled
  [v]
  (->
    (js/jQuery (.-target v))
    (.is "[data-disabled]")
    not))

(defn find-id
  [x]
  (js/jQuery (str "[data-hl~='" x "']")))

(defn dom-get
  [elem]
  (if (satisfies? hlisp.env/IDomNode elem)
    (find-id (id elem))
    (js/jQuery elem)))

(defn text-val!
  ([e]
   (.val e))
  ([e v]
   (-> e
     (.val v)
     (.trigger "change"))))

(defn check-val!
  ([e]
   (.is e ":checked"))
  ([e v]
   (-> e
     (.prop "checked" (boolean v))
     (.trigger "change"))))

(defn value!
  [elem & args] 
  (let [e (dom-get elem)]
    (case (.attr e "type")
      "checkbox" (apply check-val! e args)
      (apply text-val! e args))))

(defn attr!
  ([elem k]
   (.attr (dom-get elem) k))
  ([elem k v & kvs]
   (js/jQuery
     #(let [e (dom-get elem)] 
        (mapv (fn [[k v]]
                (let [k (name k)]
                  (case v
                    true   (.attr e k k)
                    false  (.removeAttr e k)
                    (.attr e k v))))
              (partition 2 (list* k v kvs)))))))

(defn remove-attr!
  [elem k & ks]
  (js/jQuery
    (fn []
      (let [e (.removeAttr (dom-get elem) k)]
        (when (seq ks)
          (mapv #(.removeAttr e %) ks))))))

(defn class!
  ([elem c] 
   (js/jQuery #(.toggleClass (dom-get elem) (name c)))) 
  ([elem c switch & cswitches] 
   (js/jQuery
     (fn []
       (mapv (partial apply #(.toggleClass (dom-get elem) (name %1) %2)) 
             (partition 2 (list* c switch cswitches)))))))

(defn add-class!
  [elem c & cs]
  (js/jQuery
    (fn []
      (let [e (.addClass (dom-get elem) c)]
        (when (seq cs)
          (mapv #(.addClass e %) cs))))))

(defn remove-class!
  [elem c & cs]
  (js/jQuery
    (fn []
      (let [e (.removeClass (dom-get elem) c)]
        (when (seq cs)
          (mapv #(.removeClass e %) cs))))))

(defn css!
  ([elem k v]
   (js/jQuery #(.css (dom-get elem) k v)))
  ([elem o]
   (js/jQuery #(.css (dom-get elem) (clj->js o)))))

(defn toggle!
  [elem v]
  (js/jQuery #(.toggle (dom-get elem) v)))

(defn slide-toggle!
  [elem v]
  (js/jQuery
    #(if v
       (.slideDown (.hide (dom-get elem)) "fast")
       (.slideUp (dom-get elem) "fast"))))

(defn fade-toggle!
  [elem v]
  (js/jQuery
    #(if v
       (.fadeIn (.hide (dom-get elem)) "fast")
       (.fadeOut (dom-get elem) "fast"))))

(defn focus!
  [elem v]
  (js/jQuery #(if v (timeout (fn [] (.focus (dom-get elem))) 0)
                    (timeout (fn [] (.focusout (dom-get elem))) 0))))

(defn select!
  [elem _]
  (js/jQuery #(.select (dom-get elem))))

(defn text!
  [elem v]
  (js/jQuery #(.text (dom-get elem) v)))

(defn set-nodeValue!
  [node v]
  (set! (.-nodeValue node) v) 
  node)

(defn disabled?
  [elem]
  (.is (dom-get elem) "[data-disabled]"))

(defn- delegate
  [atm event]
  (.on (js/jQuery js/document) event "[data-hl]" #(reset! atm %))
  atm)

(def events {
  :change       (delegate (atom nil) "change")
  :click        (delegate (atom nil) "click")
  :dblclick     (delegate (atom nil) "dblclick")
  :error        (delegate (atom nil) "error")
  :focus        (delegate (atom nil) "focus")
  :focusin      (delegate (atom nil) "focusin")
  :focusout     (delegate (atom nil) "focusout")
  :hover        (delegate (atom nil) "hover")
  :keydown      (delegate (atom nil) "keydown")
  :keypress     (delegate (atom nil) "keypress")
  :keyup        (delegate (atom nil) "keyup")
  :load         (delegate (atom nil) "load")
  :mousedown    (delegate (atom nil) "mousedown")
  :mouseenter   (delegate (atom nil) "mouseenter")
  :mouseleave   (delegate (atom nil) "mouseleave")
  :mousemove    (delegate (atom nil) "mousemove")
  :mouseout     (delegate (atom nil) "mouseout")
  :mouseover    (delegate (atom nil) "mouseover")
  :mouseup      (delegate (atom nil) "mouseup")
  :ready        (delegate (atom nil) "ready")
  :scroll       (delegate (atom nil) "scroll")
  :select       (delegate (atom nil) "select")
  :submit       (delegate (atom nil) "submit")
  :unload       (delegate (atom nil) "unload")})

(defn- do-on!
  [elem event callback]
  (let [c       (cell nil)
        event   (get events (keyword event))
        update  #(if (and (not= %3 %4) ((filter-id (id elem)) %4)) (callback %4))]
    (add-watch event (gensym) update)))

(defn on!
  [elem & event-callbacks]
  (mapv (partial apply do-on! elem) (partition 2 event-callbacks)))


