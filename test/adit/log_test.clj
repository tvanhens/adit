(ns adit.log-test
  (:require [adit.core :as adit]
            [clojure.test :refer :all]
            [clojure.core.async :as a]
            [clojure.tools.nrepl.transport :as t]
            [onyx.api :as onyx]
            [onyx.extensions :as extensions]
            [onyx.log.entry :as entry]
            [onyx.nrepl.transport :as transport]
            [onyx.log.commands.nrepl-msg]))

(defn env-config [id]
  {:onyx/id id
   :zookeeper/address "127.0.0.1:2188"
   :zookeeper/server? true
   :zookeeper.server/port 2188})

(defn peer-config [id]
  {:onyx/id id
   :zookeeper/address "127.0.0.1:2188"
   :onyx.peer/job-scheduler :onyx.job-scheduler/greedy
   :onyx.messaging/impl :core.async
   :onyx.messaging/bind-addr "localhost"})

(def onyx-id (atom nil))

(def onyx-env (atom nil))

(def onyx-peer-group (atom nil))

(def onyx-peers (atom nil))

(defn onyx-fixture [f]
  (reset! onyx-id (java.util.UUID/randomUUID))
  (reset! onyx-env (onyx/start-env (env-config @onyx-id)))
  (reset! onyx-peer-group (onyx/start-peer-group (peer-config @onyx-id)))
  ;; Looks like onyx needs virtual peers in order to initialize
  ;; log... log is consumed by each virtual peer
  (reset! onyx-peers (onyx/start-peers 3 @onyx-peer-group))
  (f)
  (doseq [peer @onyx-peers] (onyx/shutdown-peer peer))
  (onyx/shutdown-peer-group @onyx-peer-group)
  (onyx/shutdown-env @onyx-env))

(use-fixtures :each onyx-fixture)

(deftest log-transport-test
  (testing "writing nrepl messages to the log"
    (let [ch (a/chan 10 (filter (comp #{:nrepl-msg} :fn)))
          ;; Subscribe replays all commands, can use this to
          ;; coordinate the number of available nrepl targets.
          {:keys [env]} (onyx/subscribe-to-log (peer-config @onyx-id) ch)
          msgs [{:value 1}
                {:value 2}
                {:value 3}]
          r-ch (a/reduce (fn [acc msg]
                           (let [next (conj acc (:args msg))]
                             (when (>= (count next) (count msgs))
                               (a/close! ch))
                             next))
                         [] ch)
          log-transport (transport/onyx-log (:log env))]
      (doseq [msg msgs] (t/send log-transport msg))
      (is (= (a/<!! r-ch)
             (mapv #(assoc % :direction :out) msgs))))))

#_(deftest log-nrepl-server-test
  (testing "log nrepl server reads and evaluates from onyx log"
    (let [close-fn (adit/log-nrepl-server (peer-config @onyx-id))
          ch (a/chan 10 (filter (comp #{:nrepl-msg} :fn)))
          {:keys [env]} (onyx/subscribe-to-log (peer-config @onyx-id) ch)
          r-ch (a/reduce (fn [acc x]
                           (conj acc (:args x)))
                         [] ch)]
      (try
        (extensions/write-log-entry
         (:log env)
         (entry/create-log-entry :nrepl-msg
                                 [{:direction :in
                                   :id (str (java.util.UUID/randomUUID))
                                   :op :eval
                                   :code "(+ 2 2)"}]))
        (a/<!! (a/timeout 3000))
        (a/close! ch)
        (>pprint (a/<!! r-ch))
        (finally (close-fn))))))
