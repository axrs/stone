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
  :dependencies []
  :profiles {:dev {:plugins      [[lein-cljfmt "0.6.0"]]
                   :dependencies [[thheller/shadow-cljs "2.6.13"]
                                  [org.clojure/clojure "1.9.0"]
                                  [org.clojure/clojurescript "1.10.238" :exclusions [com.google.javascript/closure-compiler-unshaded]]
                                  [binaryage/devtools "0.9.10"]
                                  [pjstadig/humane-test-output "0.8.3"]]}}
  :cljfmt {:indents {println [[:inner 0]]
                     ns      [[:inner 0] [:inner 1]]}}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_username
                                    :password      :env/clojars_password
                                    :sign-releases false}]])
