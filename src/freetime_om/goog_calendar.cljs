(ns freetime-om.goog-calendar
  (:require
    [cljs.core.async :as async :refer [<!]]
    [cljs-http.client :as http])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(defn calendar-list [access-token]
  (go
    (let
      [res (http/get
             "https://www.googleapis.com/calendar/v3/users/me/calendarList"
             {:headers {"Authorization" (str "Bearer " access-token)}})]
      (-> (<! res) :body :items))))
