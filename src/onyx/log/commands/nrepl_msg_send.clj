(ns onyx.log.commands.nrepl-msg-send
  (:require [onyx.extensions :as extensions]))

(defmethod extensions/apply-log-entry :nrepl-msg-send
  [_ replica]
  replica)

(defmethod extensions/replica-diff :nrepl-msg-send
  [_ _ _]
  {})

(defmethod extensions/reactions :nrepl-msg-send
  [_ _ _ _ _]
  [])

(defmethod extensions/fire-side-effects! :nrepl-msg-send
  [{:keys [args]} old new diff state]
  ;; Don't fire any side effects since the onyx log api can be used to
  ;; monitor for log commands.
  )
