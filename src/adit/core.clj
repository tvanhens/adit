(ns adit.core
  (:require [clojure.core.async :as a]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.tools.nrepl.transport :as t]
            [onyx.nrepl.server :as server]
            [onyx.nrepl.transport :as transport]
            [onyx.extensions :as extensions]
            [onyx.log.entry :as entry]
            [onyx.api :as onyx]))

(defn- close-and-drain [ch]
  (a/close! ch)
  (a/reduce (constantly nil) nil ch))

(defn- nrepl-msg-xf [direction]
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
                                 (nrepl/default-handler)
                                 (transport/onyx-log (:log env))))
        (recur)))
    ;; Return a function to close and drain the log channel
    (partial close-and-drain in-msg)))

(defn broadcast-msg [log msg]
  (extensions/write-log-entry
   log (entry/create-log-entry :nrepl-msg
                               (-> msg
                                   (dissoc :transport)
                                   (assoc :direction :in)))))

;; Very primitive strategy... just wait-ms for repl eval results to
;; come back. Need to aggregate somehow
(defn- log-handler [peer-config wait-ms]
  (fn [msg]
    (let [out-msg (a/chan 10 (comp (nrepl-msg-xf :out)
                                   (filter (comp #{(:id msg)} :id))))
          {:keys [env]} (onyx/subscribe-to-log peer-config out-msg)
          r (a/reduce conj [] out-msg)]
      (broadcast-msg (:log env) msg)
      (a/<!! (a/timeout wait-ms))
      (a/close! out-msg)
      (doseq [m (a/<!! r)] (t/send (:transport msg) m)))))

(defn start-server [peer-config nrepl-config]
  (apply nrepl/start-server
       (mapcat identity
               (merge nrepl-config
                      {:handler (log-handler peer-config 1000)}))))

(defn stop-server [server] (nrepl/stop-server server))
