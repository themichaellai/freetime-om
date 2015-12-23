(ns freetime-om.oauth
  (:use
    [clojure.string :only [join split]]
    [freetime-om.secrets :only [CLIENT-ID CLIENT-SECRET]])
  (:require
    [cljs.core.async :as async :refer [chan close! <!]]
    [goog.net.XhrIo :as xhr])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))


(defn qs [dict]
  (join "&"
        (map (fn [[k, v]] (join [(name k) "=" v])) (seq dict))))

(defn build-oauth-url []
  (join ["https://accounts.google.com/o/oauth2/auth"
         "?"
         (qs {"response_type" "token"
              "client_id" CLIENT-ID
              "redirect_uri" (js/escape "http://localhost:3449")
              "scope" "https://www.googleapis.com/auth/calendar.readonly"})]))

(defn validate-token-request [token]
  (let [ch (chan 1)]
    (xhr/send
      (str "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="
           token)
      (fn [e]
        (let [res (-> e .-target .getResponseText)]
          (go (>! ch res)
              (close! ch)))))
    ch))

(defn send-chan [ch msg]
  (js/console.log (str "send-chan: " msg))
  (go (>! ch msg)
      (close! ch)))

(defn validate-token [token]
  (go
    (let [validate-res (<! (validate-token-request token))]
      (-> validate-res (contains? "error") not))))
