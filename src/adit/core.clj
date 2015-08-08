(ns adit.core
  (:require [onyx.api :as api]
            [clojure.core.async :as a]
            [clojure.tools.nrepl.server :as nrepl]
            [onyx.nrepl.server :as server]
            [onyx.nrepl.transport :as transport]
            [onyx.api :as onyx]))

(defn close-and-drain [ch]
  (a/close! ch)
  (a/reduce (constantly nil) nil ch))

(defn nrepl-msg-xf [direction]
  {:pre [(#{:in :out} direction)]}
  (comp (filter (comp #{:nrepl-msg} :fn))
        (filter (comp #{direction} :direction :args))
        (map :args)
        (map #(dissoc % :direction))))

(defn log-nrepl-server [peer-config]
  (let [in-msg (a/chan 10 (nrepl-msg-xf :in))
        {:keys [env]} (onyx/subscribe-to-log peer-config in-msg)]
    (a/go-loop []
      (when-let [msg (a/<! in-msg)]
        (a/thread (nrepl/handle* msg
                                 nrepl/default-handler
                                 (transport/onyx-log (:log env))))))
    ;; Return a function to close and drain the log channel
    (partial close-and-drain in-msg)))

(defn start-server [peer-config nrepl-config]
  (apply nrepl/start-server
       (mapcat identity
               (merge nrepl-config
                      {:handler (server/log-handler peer-config)}))))
