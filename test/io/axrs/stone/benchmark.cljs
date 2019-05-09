(ns io.axrs.stone.benchmark
  (:require
    [cljs.test :refer [deftest testing is async use-fixtures]]
    [clojure.core.async :refer [go <! >! put! chan close!]]
    [io.jesi.backpack.random :as rnd]
    [io.axrs.stone.core :as stone]
    [io.jesi.backpack.test.macros :refer [async-go]]))

(defn geo-route []
  {:properties {:id   (rnd/uuid)
                :name (rnd/string 200)}
   :geometry   {:type        :LineString
                :coordinates (doall (repeatedly 2000 rnd/lnglat))}})

(defonce performance-data (doall (repeatedly 900 geo-route)))

(defonce store-name "stone")

(deftest performance-test

  (let [k (rnd/alpha-numeric 10)
        v performance-data
        db (<! (stone/<init "stone-db" {:upgrade (fn [db]
                                                   (-> db (stone/delete-store store-name))
                                                   (is (false? (stone/contains-store? db store-name)))
                                                   (-> db (stone/create-store store-name {:key-path "id"}))
                                                   (is (true? (stone/contains-store? db store-name))))}))]
    (async-go

     (testing "performance"

       (testing "of large values"
         (time
          (do
            (<! (stone/<assoc db store-name k v))
            (<! (stone/<get db store-name k)))))))))
