(ns io.axrs.stone.core-test
  (:require
    [cljs.test :refer [deftest testing is async use-fixtures]]
    [clojure.core.async :refer [go <! >! put! chan close!]]
    [io.jesi.backpack.random :as rnd]
    [io.axrs.stone.core :as stone]
    [io.jesi.backpack.test.macros :refer [async-go]]))

(defonce ^:dynamic *db* nil)
(defonce store-name "stone")

(use-fixtures :each
  {:before #(async-go
             (set! *db* (<! (stone/<init "stone-db" {:upgrade (fn [db]
                                                                (-> db (stone/delete-store store-name))
                                                                (is (false? (stone/contains-store? db store-name)))
                                                                (-> db (stone/create-store store-name {:key-path "id"}))
                                                                (is (true? (stone/contains-store? db store-name))))}))))})

(deftest names-test

  (testing "returns a set of all current store names"
    (is (= #{store-name} (stone/names *db*)))))

(deftest name-test

  (testing "returns the name of the db"
    (is (= "stone-db" (stone/name *db*)))))

(deftest contains-store?-test

  (testing "true if the store exists"
    (is (true? (stone/contains-store? *db* store-name)))
    (is (false? (stone/contains-store? *db* "asdf")))))

(deftest get-test-no-store
  (async-go

   (testing "returns nil when store doesn't exist"
     (is (false? (stone/contains-store? *db* "asdf")))
     (is (nil? (<! (stone/<get *db* "asdf" "item")))))))

(deftest get-test-no-key
  (async-go

   (testing "returns nil when store doesn't contain the item"
     (is (nil? (<! (stone/<get *db* store-name "asdf")))))))

(deftest assoc-test
  (async-go

   (testing "inserts or replaces an item in the db store"
     (let [k "item"
           v1 {:hello "world"
               :rand  (rand)}
           v2 {:wave-to-the "moon"
               :rand        (rand)}]
       (<! (stone/<assoc *db* store-name k v1))
       (is (= v1 (<! (stone/<get *db* store-name k))))
       (<! (stone/<assoc *db* store-name k v2))
       (is (= v2 (<! (stone/<get *db* store-name k))))))))

(deftest dissoc-test
  (async-go

   (testing "removes an item from the db store"
     (let [k "item-to-delete"
           v "delete me"]
       (<! (stone/<assoc *db* store-name k v))
       (is (= v (<! (stone/<get *db* store-name k))))
       (<! (stone/<dissoc *db* store-name k))
       (is (nil? (<! (stone/<get *db* store-name k))))))))
