(ns onyx.log.commands.nrepl-msg
  (:require [clojure.tools.nrepl.server :as nrepl]
            [onyx.extensions :as extensions]
            [onyx.log.entry :as entry]
            [onyx.nrepl.transport :as transport]))

(defmethod extensions/apply-log-entry :nrepl-msg
  [_ replica]
  replica)

(defmethod extensions/replica-diff :nrepl-msg
  [_ _ _]
  {})

(defmethod extensions/reactions :nrepl-msg
  [_ _ _ _ _]
  [])

(defmethod extensions/fire-side-effects! :nrepl-msg
  [{:keys [args]} old new diff state]
  state)
