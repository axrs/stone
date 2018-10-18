(ns io.axrs.stone.core-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures async]]
    [clojure.core.async :refer [go <! >! put! chan close!]]
    [io.axrs.stone.core :as stone]))

(defonce db (atom nil))

(defn with-done [done f]
  (go (let [res (<! (stone/init "stone-db"
                                {:upgrade #(do (prn "upgrading")
                                               (-> % (stone/delete-store "stone"))
                                               (is (false? (stone/contains-store? % "stone")))
                                               (-> % (stone/create-store "stone" {:key-path "id"}))
                                               (is (true? (stone/contains-store? % "stone"))))}))]
        (reset! db res)
        (f)
        (done))))

(deftest names-test
  (testing "returns a set of all current store names"
    (async done
           (with-done done #(is (= #{"stone"} (stone/names @db)))))))

(deftest name-test
  (testing "returns the name of the db"
    (async done
           (with-done done
             #(is (= "stone-db" (stone/name @db)))))))

(deftest contains-store?-test
  (testing "true if the store exists"
    (async done
           (with-done done
             #(do
                (is (true? (stone/contains-store? @db "stone")))
                (is (false? (stone/contains-store? @db "asdf"))))))))

(deftest get-test-no-store
  (testing "returns nil when store doesn't exist"
    (async done
           (with-done done
             #(do
                (is (false? (stone/contains-store? @db "asdf")))
                (go (is (nil? (<! (stone/get @db "asdf" "item"))))))))))

(deftest get-test-no-key
  (testing "returns nil when store doesn't contain the item"
    (async done
           (with-done done
             #(go (is (nil? (<! (stone/get @db "stone" "asdf")))))))))

(deftest assoc-test
  (testing "inserts or replaces an item in the db store"
    (async done
           (with-done done
             #(go
                (let [k "item"
                      v1 {:hello "world"
                          :rand  (rand)}
                      v2 {:wave-to-the "moon"
                          :rand        (rand)}]
                  (<! (stone/assoc @db "stone" k v1))
                  (is (= v1 (<! (stone/get @db "stone" k))))
                  (<! (stone/assoc @db "stone" k v2))
                  (is (= v2 (<! (stone/get @db "stone" k))))))))))

(deftest dissoc-test
  (testing "removes an item from the db store"
    (async done
           (with-done done
             #(go
                (let [k "item-to-delete"
                      v "delete me"]
                  (<! (stone/assoc @db "stone" k v))
                  (is (= v (<! (stone/get @db "stone" k))))
                  (<! (stone/dissoc @db "stone" k))
                  (is (nil? (<! (stone/get @db "stone" k))))))))))
