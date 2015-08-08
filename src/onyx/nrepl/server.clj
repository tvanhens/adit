(ns onyx.nrepl.server
  (:require [clojure.core.async :as a]
            [clojure.tools.nrepl.transport :as t]
            [onyx.extensions :as extensions]
            [onyx.log.entry :as entry]
            [onyx.api :as onyx]))

(defn log-handler [peer-config]
  (fn handler [msg]
    (println "got here")
    (let [send-ch
          (a/chan 10 (comp (filter (comp (partial = :nrepl-msg-send) :fn))
                           (filter (comp (partial = (:id msg)) :id :args))))
          {:keys [env]} (onyx/subscribe-to-log peer-config send-ch)]
      (extensions/write-log-entry
       (:log env)
       (entry/create-log-entry :nrepl-msg-recv (dissoc msg :transport)))

      (a/go
        ;; Timeout for sampling responses
        (a/<! (a/timeout 1000))
        (a/close! send-ch))

      (a/go
        (println "gathering")
        (let [transport (:transport msg)
              msg (a/<! (a/reduce conj [] send-ch))]
          (println "sending" msg)
          (t/send transport (assoc msg :transport transport)))))))
