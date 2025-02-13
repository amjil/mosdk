(ns app.util
  (:require
   [cljs-bean.core :as bean]
   [goog.dom :as gdom]
   [goog.object :as gobj]
   [goog.string :as gstring]
   [goog.string.format]
   [goog.userAgent]
   [clojure.string :as string]
   [clojure.pprint :refer [pprint]]
   [clojure.walk :as walk]
   [promesa.core :as p]))

(defn fetch
  ([url on-ok on-failed]
   (fetch url {} on-ok on-failed))
  ([url opts on-ok on-failed]
   (-> (js/fetch url (bean/->js opts))
       (.then (fn [resp]
                (if (>= (.-status resp) 400)
                  (on-failed resp)
                  (if (.-ok resp)
                    (-> (.json resp)
                        (.then bean/->clj)
                        (.then #(on-ok %)))
                    (on-failed resp))))))))

(defn upload
  [url file on-ok on-failed on-progress]
  (let [xhr (js/XMLHttpRequest.)]
    (.open xhr "put" url)
    (gobj/set xhr "onload" on-ok)
    (gobj/set xhr "onerror" on-failed)
    (when (and (gobj/get xhr "upload")
               on-progress)
      (gobj/set (gobj/get xhr "upload")
                "onprogress"
                on-progress))
    (.send xhr file)))

(defn post
  [url body on-ok on-failed]
  (fetch url {:method "post"
              :headers {:Content-Type "application/json"}
              :body (js/JSON.stringify (clj->js body))}
         on-ok
         on-failed))

(defn patch
  [url body on-ok on-failed]
  (fetch url {:method "patch"
              :headers {:Content-Type "application/json"}
              :body (js/JSON.stringify (clj->js body))}
         on-ok
         on-failed))

(defn delete
  [url on-ok on-failed]
  (fetch url {:method "delete"
              :headers {:Content-Type "application/json"}}
         on-ok
         on-failed))

;; Copied from https://github.com/tonsky/datascript-todo
(defmacro profile [k & body]
  `(if goog.DEBUG
     (let [k# ~k]
       (.time js/console k#)
       (let [res# (do ~@body)]
         (.timeEnd js/console k#)
         res#))
     (do ~@body)))

(defn react
  "Works in conjunction with [[reactive]] mixin. Use this function instead of `deref` inside render, and your component will subscribe to changes happening to the derefed atom."
  [ref]
  (when rum.core/*reactions*
    (vswap! rum.core/*reactions* conj ref))
  (and ref @ref))



(comment
  (-> (js/fetch "http://localhost:8700/fonts/mnglwhiteotf.ttf" #js {})
      (.then (fn [resp]
               (if (>= (.-status resp) 400)
                 #(js/console.log "error on fetch")
                 (if (.-ok resp)

                   (def res resp)
                   #(js/console.log "error on fetch"))))))

  (-> (.arrayBuffer res)
      (.then (fn [v]
               (def buff v))))

  buff

  (require '[app.util :as util])

  (def font (js/window.fontkit.create buff))
  font

  (.-variationAxes font)
  (.-availableFeatures font)
  (.-numGlyphs font)
  (.-copyright font)
  (.-namedVariations font)
  (.-unitsPerEm font)
  (.-head font)

  (def aa (.layout font "abc"))
  (def aa (.layout font "ᠡᠷᠬᠡ"))
  aa

  (.-glyphs aa)
  (js/console.log aa)


  (def res nil)
  res
  (-> (js/fetch "http://localhost:8700/fonts/mnglwhiteotf.ttf" #js {})
      (p/then (fn [resp]
                (if (>= (.-status resp) 400)
                  #(js/console.log "error on fetch")
                  (if (.-ok resp)

                    (def res resp)
                    #(js/console.log "error on fetch"))))))

  res)
  