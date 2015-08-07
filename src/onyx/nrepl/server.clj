(ns onyx.nrepl.server
  (:require [clojure.core.async :as a]
            [clojure.tools.nrepl.transport :as t]
            [onyx.extensions :as extensions]
            [onyx.api :as onyx]))

(defn log-handler [peer-config]
  (let [
        ;; Probably should pass this in rather than closing over
        send-ch (a/chan 10 (filter (comp (partial = :nrepl-msg-send) :fn)))]
    (fn [msg]
      (let [{:keys [env]} (onyx/subscribe-to-log peer-config send-ch)]
        (extensions/write-log-entry (:log env) (dissoc msg :transport))
        ;; Listen for responses
        #_(let [result nil ; TODO
              ]
          (t/send (:transport msg) result))))))
