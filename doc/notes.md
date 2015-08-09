# Example log initialization:

``` clojure
{:fn :prepare-join-cluster,
 :args
 {:joiner #uuid "dcbe5995-42ef-43ca-a2ff-93e888f63903",
  :peer-site {:site #uuid "3769609c-f704-430f-8f9f-442913e946f9"}},
 :message-id 0,
{:fn :prepare-join-cluster,
 :args
 {:joiner #uuid "31b19b1f-444c-4d20-befe-e206389084ee",
  :peer-site {:site #uuid "1388bca5-b138-4ec5-a381-bb60d8f0273d"}},
 :message-id 1,
 :created-at 1439054631727}
{:fn :notify-join-cluster,
 :args
 {:observer #uuid "31b19b1f-444c-4d20-befe-e206389084ee",
  :subject #uuid "dcbe5995-42ef-43ca-a2ff-93e888f63903"},
 :immediate? true,
 :message-id 2,
 :created-at 1439054631729}
{:fn :accept-join-cluster,
 :args
 {:observer #uuid "31b19b1f-444c-4d20-befe-e206389084ee",
  :subject #uuid "dcbe5995-42ef-43ca-a2ff-93e888f63903",
  :accepted-observer #uuid "dcbe5995-42ef-43ca-a2ff-93e888f63903",
  :accepted-joiner #uuid "31b19b1f-444c-4d20-befe-e206389084ee"},
 :immediate? true,
 :message-id 3,
 :created-at 1439054631733}
{:fn :prepare-join-cluster,
 :args
 {:joiner #uuid "c8433943-7d29-458c-bd2f-c905d0f0b783",
  :peer-site {:site #uuid "1c3a1c40-79d8-4242-9d16-ffc8421ba118"}},
 :message-id 4,
 :created-at 1439054631743}
{:fn :notify-join-cluster,
 :args
 {:observer #uuid "c8433943-7d29-458c-bd2f-c905d0f0b783",
  :subject #uuid "31b19b1f-444c-4d20-befe-e206389084ee"},
 :immediate? true,
 :message-id 5,
 :created-at 1439054631745}
{:fn :accept-join-cluster,
 :args
 {:observer #uuid "c8433943-7d29-458c-bd2f-c905d0f0b783",
  :subject #uuid "31b19b1f-444c-4d20-befe-e206389084ee",
  :accepted-observer #uuid "dcbe5995-42ef-43ca-a2ff-93e888f63903",
  :accepted-joiner #uuid "c8433943-7d29-458c-bd2f-c905d0f0b783"},
 :immediate? true,
 :message-id 6,
 :created-at 1439054631747}
 :created-at 1439054631713}
```

# Repl response

``` clojure
{:fn :nrepl-msg, :args {:direction :in, :id eb5b1e17-c866-4708-9ecf-de5e4d5c0e71, :op eval, :code (+ 2 2)}, :message-id 7, :created-at 1439082318255}

{:fn :nrepl-msg, :args {:id eb5b1e17-c866-4708-9ecf-de5e4d5c0e71, :session f457dbed-feb1-4cfa-bf88-ef375b6e747f, :value 4, :ns user, :direction :out}, :message-id 8, :created-at 143908231861}

{:fn :nrepl-msg, :args {:id eb5b1e17-c866-4708-9ecf-de5e4d5c0e71, :session f457dbed-feb1-4cfa-bf88-ef375b6e747f, :status #{:done}, :direction :out}, :message-id 9, :created-at 1439082318264}
```
