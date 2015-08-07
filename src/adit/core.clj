(ns adit.core
  (:require [onyx.api :as api]
            [clojure.core.async :as a]
            [clojure.tools.nrepl.server :as nrepl]
            [onyx.nrepl.server :as server]))

;; API Namespace:

(defn start-server [peer-config nrepl-config]
  (apply nrepl/start-server
         (merge nrepl-config
                {:handler (server/log-handler peer-config)})))
