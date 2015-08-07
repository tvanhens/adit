(ns onyx.nrepl.transport
  (:require [clojure.tools.nrepl.transport :as t]
            [onyx.extensions :as extensions]
            [onyx.log.entry :as entry]))

(defn throw-unsupported []
  (throw (Exception. "recv not supported for OnyxLog")))

(defn onyx-log [log]
  (reify
    t/Transport
    (recv [this] (throw-unsupported))
    (recv [this] (throw-unsupported))
    (send [this msg]
      (extensions/write-log-entry
       log (entry/create-log-entry :nrepl-msg-send {:msg msg})))))
