(ns bfs
  (:require
   [task2 :refer [dag-data get-node-children log-path nodes->root]]
   [uniformed.viz :refer [visualize-tree]]))

; ## Breadth-First Search
; **Breadth-First Search (BFS)** implements a **level-order traversal** strategy, exploring all nodes at depth $d$ before proceeding to depth $d+1$. This ensures the _shortest path_ in terms of edge count is always found.

(defn bfs
  "Pure function to find path using BFS"
  [dag-data target]
  (let [root (nodes->root dag-data)]
    (loop [queue (conj clojure.lang.PersistentQueue/EMPTY [root [root]])
           visited #{}]
      (when (seq queue)
        (let [[current path] (peek queue)
              children (get-node-children current dag-data)]
          (if (= current target)
            path
            (let [next-nodes (remove visited children)]
              (recur
               (into (pop queue)
                     (map (fn [node] [node (conj path node)]) next-nodes))
               (conj visited current)))))))))

(defn visualize-bfs
  "Visualize the tree with BFS path highlighted."
  [dag-data target]
  (->> target
       (bfs dag-data)
       (log-path target)
       (visualize-tree dag-data)))

; ### Breadth-First Search Results
(bfs dag-data "Excel-Child1")

^:kind/hiccup
(visualize-bfs dag-data "Excel-Child1")
