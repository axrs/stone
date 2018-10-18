(ns io.axrs.stone.core-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.axrs.stone.core :as stone]))

(deftest base-test
  (testing "returns :testing"
    (is (== :fail (stone/base)))))
