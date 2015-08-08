(ns adit.log-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            [onyx.api :as onyx]
            [onyx.extensions :as extensions]
            [onyx.log.entry :as entry]
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

(use-fixtures :once onyx-fixture)

(deftest write-to-log-test
  (testing "writing nrepl messages to the log"
    (let [ch (a/chan 10 (filter (comp #{:nrepl-msg} :fn)))
          ;; Subscribe replays all commands, can use this to
          ;; coordinate the number of available nrepl targets.
          {:keys [env]} (onyx/subscribe-to-log (peer-config @onyx-id) ch)
          r-ch (a/reduce (fn [acc x]
                           (when (= :done (:args x))
                             (a/close! ch))
                           (conj acc (:args x)))
                         [] ch)]
      (extensions/write-log-entry
       (:log env)
       (entry/create-log-entry :nrepl-msg 1))
      (extensions/write-log-entry
       (:log env)
       (entry/create-log-entry :nrepl-msg 2))
      (extensions/write-log-entry
       (:log env)
       (entry/create-log-entry :nrepl-msg 3))
      (extensions/write-log-entry
       (:log env)
       (entry/create-log-entry :nrepl-msg :done))
      (is (= (a/<!! r-ch) [1 2 3 :done])))))
