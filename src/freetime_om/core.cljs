(ns freetime-om.core
  (:use
    [clojure.string :only [split]]
    [freetime-om.oauth :only [validate-token build-oauth-url]])
  (:require
    [cljs.core.async :as async :refer [chan close! <!]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom
                     {:text "Hello world!"
                      :access-token nil}))

(. js/console (log (str "oauth: " (build-oauth-url))))

(defn parse-qs [hash-str]
  (into {} (map #(split % "=") (split hash-str "&"))))

(defn get-access-token [hash-str]
  (let [qs (parse-qs hash-str)]
    (get qs "access_token")))

(if (> (.-length location.hash) 0)
  (go
    (let [access-token (get-access-token (subs location.hash 1))
          token-is-validated (<! (validate-token access-token))]
      (if token-is-validated
        (swap! app-state (fn [s] (assoc s :access-token access-token)))
        (js/console.log "not validated"))))
  nil)

(om/root
  (fn [data owner]
    (reify om/IRender
      (render [_]
        (dom/div nil
          (dom/h1 nil (:text data))))))
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
