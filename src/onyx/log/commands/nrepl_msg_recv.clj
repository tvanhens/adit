(ns onyx.log.commands.nrepl-msg-recv
  (:require [clojure.tools.nrepl.server :as nrepl]
            [onyx.extensions :as extensions]
            [onyx.log.entry :as entry]
            [onyx.nrepl.transport :as transport]))

(defmethod extensions/apply-log-entry :nrepl-msg-recv
  [_ replica]
  replica)

(defmethod extensions/replica-diff :nrepl-msg-recv
  [_ _ _]
  {})

(defmethod extensions/reactions :nrepl-msg-recv
  [_ _ _ _ _]
  [])

(defmethod extensions/fire-side-effects! :nrepl-msg-recv
  [{:keys [args]} old new diff state]
  (future (nrepl/handle* (:msg args)
                         nrepl/default-handler
                         (transport/onyx-log (:log state)))))
