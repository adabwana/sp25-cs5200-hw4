(ns dls
  (:require
   [task2 :refer [dag-data get-node-children log-path nodes->root]]
   [uniformed.viz :refer [visualize-tree]]))

; ### Depth-Limited Search
; **Depth-Limited Search (DLS)** extends **DFS** by imposing a maximum depth limit $l$, preventing excessive depth exploration. This modification trades *completeness* for *resource control*.

(defn dls
  "Pure function to find path using Depth Limited Search"
  [dag-data target depth-limit]
  (let [root (nodes->root dag-data)]
    (loop [stack (list [root [root] 0])  ; [node path depth]
           visited #{}]
      (when (seq stack)
        (let [[current path depth] (first stack)
              children (get-node-children current dag-data)]
          (cond
            (= current target) path
            (>= depth depth-limit) (recur (rest stack) visited)
            :else (let [next-nodes (->> children
                                        (remove visited)
                                        (map (fn [node]
                                               [node (conj path node) (inc depth)])))]
                    (recur (concat next-nodes (rest stack))
                           (conj visited current)))))))))

(defn visualize-dls
  "Visualize the tree with DLS path highlighted."
  [dag-data target depth-limit]
  (->> (dls dag-data target depth-limit)
       (log-path target)
       (visualize-tree dag-data)))

; ### Depth-Limited Search Results
(dls dag-data "Excel-Child1" 2)
(dls dag-data "Excel-Child1" 3)
(dls dag-data "Excel-Child1" 4)

^:kind/hiccup
(visualize-dls dag-data "Excel-Child1" 3)
