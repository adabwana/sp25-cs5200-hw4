(ns dfs
  (:require
   [task2 :refer [dag-data get-node-children log-path nodes->root]]
   [uniformed.viz :refer [visualize-tree]]))

; ### Depth-First Search
; **Depth-First Search (DFS)** implements a **stack-based exploration** strategy, pursuing paths to their maximum depth before backtracking. This approach offers _memory efficiency_ but does not guarantee shortest paths.

(defn dfs
  "Pure function to find path using Depth First Search"
  [dag-data target]
  (let [root (nodes->root dag-data)]
    (loop [stack (list [root [root]])  ; Use list as stack
           visited #{}]
      (when (seq stack)
        (let [[current path] (first stack)
              children (get-node-children current dag-data)]
          (if (= current target)
            path
            (let [next-nodes (->> children
                                  (remove visited)
                                  (map (fn [node] [node (conj path node)])))]
              (recur (concat next-nodes (rest stack))  ; DFS: Add to front
                     (conj visited current)))))))))

(defn visualize-dfs
  "Visualize the tree with DFS path highlighted."
  [dag-data target]
  (->> target
       (dfs dag-data)
       (log-path target)
       (visualize-tree dag-data)))

; ### Depth-First Search Results
(dfs dag-data "Excel-Child1")

^:kind/hiccup
(visualize-dfs dag-data "Excel-Child1")
