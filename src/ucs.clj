(ns ucs
  (:require
   [task2 :refer [dag-data get-node-children log-path nodes->root]]
   [uniformed.viz :refer [visualize-tree]]))

; ### Uniform-Cost Search
; **Uniform-Cost Search (UCS)** extends **BFS** by considering _edge weights_, ensuring the path with minimum total cost is found. The algorithm maintains a *priority queue* ordered by cumulative path cost.

(defn get-node-cost
  "Get the cost of a node from the DAG data"
  [node dag-data]
  (let [node-data (first (filter #(= (:id %) node) (:nodes dag-data)))]
    (:cost node-data 1)))  ; Default to 1 if cost not found

(defn ucs
  "Pure function to find path using Uniform Cost Search"
  [dag-data target]
  (let [root (nodes->root dag-data)
        initial-state [0 root [root]]]  ; [cost node path]
    (loop [queue (list initial-state)  ; Use list as priority queue
           visited #{}]
      (when (seq queue)
        (let [[cost node path] (first (sort-by first queue))]
          (cond
            (= node target) path
            (visited node) (recur (rest queue) visited)
            :else
            (let [next-nodes (->> (get-node-children node dag-data)
                                  (remove visited)
                                  (map (fn [child]
                                         (let [child-cost (get-node-cost child dag-data)]
                                           [(+ cost child-cost)
                                            child
                                            (conj path child)]))))]
              (recur (concat (rest queue) next-nodes)
                     (conj visited node)))))))))

(defn visualize-ucs
  "Visualize the tree with UCS path highlighted."
  [dag-data target]
  (->> target
       (ucs dag-data)
       (log-path target)
       (visualize-tree dag-data)))

; ### Uniform-Cost Search Results
(ucs dag-data "Excel-Child1")

^:kind/hiccup
(visualize-ucs dag-data "Excel-Child1")
