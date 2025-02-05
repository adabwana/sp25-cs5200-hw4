(ns dev
  (:require [scicloj.clay.v2.api :as clay]))

(defn build []
  (clay/make!
   {:format              [:quarto :html]
    :book                {:title "CS5200: Uninformed Search"}
    :subdirs-to-sync     ["notebooks"]
    :source-path         ["src/index.clj"
                          "src/task1.clj"
                          "src/task2.clj"
                          "src/bfs.clj"
                          "src/ucs.clj"
                          "src/dfs.clj"
                          "src/dls.clj"
                          "src/ucs_plus.clj"]
    :base-target-path    "docs"
    :clean-up-target-dir true}))

(comment
  (build))
