(ns onyx.nrepl.transport
  (:require [clojure.tools.nrepl.transport :as t]
            [onyx.extensions :as extensions]
            [onyx.log.entry :as entry]))

(defn onyx-log [log]
  (reify
    t/Transport
    (send [this msg]
      (extensions/write-log-entry
       log (entry/create-log-entry
            :nrepl-msg (merge msg {:direction :out}))))))
