(ns io.axrs.stone.core
  (:refer-clojure :exclude [name assoc get dissoc])
  (:require
    [clojure.reader :as reader]
    [clojure.core.async :refer [go <! >! put! chan close!]]))

(defn noop [])

(defn- target-result [^js e]
  (-> e
      .-target
      .-result))

(defn names
  "A set of the names of the stores currently in the db"
  [^js db]
  (if-let [^js names (some-> db .-objectStoreNames)]
    (set (map-indexed #(.item names %) (range (.-length names))))
    #{}))

(defn name
  "Name of the connected database."
  [^js db]
  (some-> db (.-name)))

(defn version
  "Version of the connected database."
  [^js db]
  (some-> db (.-version)))

(defn contains-store?
  "True if the db contains the given store name"
  [^js db name]
  (some-> db .-objectStoreNames (.contains name)))

(defn delete-store
  "Destroys the object store with the given name in the connected database, along with any indexes that reference it.

  Note: Similar to `create-store`, this function can be called only within a upgrade transaction on db init.
  "
  [^js db name]
  (when (contains-store? db name)
    (.deleteObjectStore db name)))

(defn create-store [^js db name & [{:keys [key-path] :as opts
                                    :or   {key-path "id"}}]]
  (.createObjectStore ^js db name #js {:keyPath key-path}))

(defn- close-and-error [chan err]
  (js/console.error err)
  (close! chan))

(defn init
  [name & [{:keys [version open upgrade close]
            :or   {version 1
                   open    noop
                   close   noop
                   upgrade noop}
            :as   opts}]]
  (let [chan (chan)
        ^js request (.open js/indexedDB name version)]
    (set! (.-onsuccess request) #(if-let [db (target-result %)]
                                   (put! chan db)
                                   (close! chan)))
    (set! (.-onclose request) (comp close target-result))
    (set! (.-onupgradeneeded request) (comp upgrade target-result))
    (set! (.-onerror request) close-and-error)
    chan))

(defn assoc
  "Associates the value (v) against the given key (k) if the store exists"
  [^js db store k v]
  (let [chan (chan 1)]
    (if-not (contains-store? db store)
      (close! chan)
      (let [tx (.transaction db store "readwrite")
            store (.objectStore ^js tx store)
            request (.put ^js store #js {:id k :data (pr-str v)})]
        (set! (.-onerror request) (partial close-and-error chan))
        (set! (.-onsuccess request) #(close! chan))))
    chan))

(defn dissoc
  "Dissociates the key (k) (and subsequent value) from the store if it exists"
  [^js db store k]
  (let [chan (chan 1)]
    (if-not (contains-store? db store)
      (close! chan)
      (let [tx (.transaction db store "readwrite")
            store (.objectStore ^js tx store)
            request (.delete ^js store k)]
        (set! (.-onerror request) (partial close-and-error chan))
        (set! (.-onsuccess request) #(close! chan))))
    chan))

(defn get
  "Gets the value of the key (k) from the store if it exists"
  [^js db store k]
  (let [chan (chan 1)]
    (if-not (contains-store? db store)
      (close! chan)
      (let [tx (.transaction db store "readonly")
            store (.objectStore ^js tx store)
            request (.get ^js store k)]
        (set! (.-onerror request) (partial close-and-error chan))
        (set! (.-onsuccess request) #(if-let [v (target-result %)]
                                       (put! chan (reader/read-string (.-data ^js v)))
                                       (close! chan)))))
    chan))
