# Adit

## Purpose

* Provide an nrepl interface to an Onyx cluster
* Allow hot-loading of function definitions
* Functions are used in:
  * Task functions
  * Flow predicates
  * Group-by functions
  * Error handling with flow post-transform
  * Lifecycle call fn map
  * Restart pred fn
* Examine state of peers in the cluster

## Considerations

* Default nrepl server implementation uses bencode for its transport
  mechanism
  * Advantageous to tap into this so repl evaluation works in existing
    IDEs (emacs, cursive, etc)
* However, direct TCP communication with all onyx peers would be
  difficult to maintain
  * Nodes that come online after repl evaluations would be out of sync
  * Can use the onyx log to distribute commands and aggregate their
  results

## Design

### Background

To facilitate compatibility with existing Clojure IDE infrastructure,
the default nrepl server implementation can be used. Each peer can run
an instance of nrepl server to allow an adit nrepl to be started
against any IP in the cluster.

The default nrepl server implementation allows the handler function to
be
[overridden.](https://github.com/clojure/tools.nrepl/blob/master/src/main/clojure/clojure/tools/nrepl/server.clj#L139)
Evaluation and other nrepl functions are implemented through
middleware which are chained in the
[default handler.](https://github.com/clojure/tools.nrepl/blob/master/src/main/clojure/clojure/tools/nrepl/server.clj#L80-L90)

### Approach

1. Host a gateway nrepl server on each peer on a known (defined) port
2. Replace the default handler with an implementation that wires up no
   middleware and forwards all requests to the log command
   `nrepl-request`
3. Nrepl commands sent through `nrepl-request` are passed through the
   default nrepl middlewares on each peer in the `fire-side-effects!`
   extension hook which executes evaluations, interrupts, etc. on each
   peer
4. The resulting nrepl response (after being passed through the
   default middleware chain) is sent to the log via `nrepl-response`
   with meta data about the peer
5. The gateway nrepl monitors the log for `nrepl-response` commands,
   aggregates all responses for a given nrepl request id into a map
   and returns this map through the bencode transport protocol using
   `t/send`. [Example](https://github.com/clojure/tools.nrepl/blob/master/src/main/clojure/clojure/tools/nrepl/server.clj#L71)
