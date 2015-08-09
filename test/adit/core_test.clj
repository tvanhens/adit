(ns adit.core-test
  (:require [clojure.test :refer :all]
            [adit.core :refer :all]
            [clojure.tools.nrepl.server :as server]
            [clojure.tools.nrepl :as repl]
            [clojure.tools.nrepl.transport :as t]
            [clojure.tools.nrepl.misc :refer [response-for]]))

(comment
  (def nrepl-port 9000)

  (defn handler [{:keys [op transport] :as msg}]
    (t/send transport (response-for msg :status :done :value 5)))

  (defn nrepl-server [f]
    (let [s (server/start-server :port nrepl-port :handler handler)]
      (f)
      (.close s)))

  (use-fixtures :once nrepl-server)

  (deftest nrepl-behavior
    (testing "nrepl should run clojure code remotely"
      (with-open [conn (repl/connect :port 9000)]
        (let [client (repl/client conn 1000)]
          (-> client
              (repl/message {:op :eval :code "(in-ns 'adit.core-test)"})
              doall))))))
