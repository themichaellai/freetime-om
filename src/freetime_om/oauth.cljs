(ns freetime-om.oauth
  (:use
    [clojure.string :only [join split]]
    [freetime-om.secrets :only [CLIENT-ID CLIENT-SECRET]])
  (:require
    [cljs-http.client :as http])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


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
  (http/get "https://www.googleapis.com/oauth2/v1/tokeninfo"
            {:query-params {"access_token" token}}))

(defn validate-token [token]
  (go
    (let [res (<! (validate-token-request token))]
      (and
        (= (-> res :body :audience) CLIENT-ID)
        (= (:status res) 200)))))
