(def VERSION (.trim (slurp "VERSION")))

(defproject io.axrs/stone VERSION
  :description "Stone"
  :url "https://github.com/axrs/stone.git"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :min-lein-version "2.8.1"
  :source-paths ["src"]
  :test-paths ["test"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target" "test-target/js"]
  :dependencies [[thheller/shadow-cljs "2.8.36"]
                 [io.jesi/backpack "0.0.30-SNAPSHOT"]]
  :profiles {:dev {:plugins      [[lein-cljfmt "0.6.0"]]
                   :dependencies [[binaryage/devtools "0.9.10"]
                                  [pjstadig/humane-test-output "0.8.3"]]}}
  :cljfmt {:indents {println [[:inner 0]]
                     ns      [[:inner 0] [:inner 1]]}}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_username
                                    :password      :env/clojars_password
                                    :sign-releases false}]])
