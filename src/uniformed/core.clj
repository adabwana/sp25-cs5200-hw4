(ns uniformed.core
  (:require
   [clojure.string :as str]
   [clojure.set :as set]
   [uniformed.viz :refer [visualize-tree]]
   [uniformed.dag :as dag]))

;; Tree Structure Definition
(def node-levels
  "Define node levels and their members for consistent level assignment"
  {0 #{"Operating-System"}
   1 #{"Email" "Browser" "Social-Media"}
   2 #{"File" "Chat" "Cloud" "Docs" "Maps" "Search" "Games" "Photos" "Videos"}
   3 #{"Word" "Excel" "PowerPoint"}
   4 #{"Word-Child1" "Word-Child2" "Word-Child3"
       "Excel-Child1" "Excel-Child2" "Excel-Child3"
       "PowerPoint-Child1" "PowerPoint-Child2" "PowerPoint-Child3"}})

(def dag-structure
  "Define the DAG structure with parent-child relationships.
   Multiple paths to same targets with different depths to show search differences."
  {"Operating-System" ["Email" "Browser" "Social-Media"]
   "Email" ["File" "Chat" "Cloud" "Excel"]  ; Direct path to Excel (shorter but costly)
   "Browser" ["Docs" "Maps" "Search"]
   "Social-Media" ["Games" "Photos" "Videos"]
   "File" ["Word" "Excel" "PowerPoint"]
   "Docs" ["Word" "Excel" "PowerPoint"]
   "Games" ["Word" "Excel" "PowerPoint"]
   "Word" ["Word-Child1" "Word-Child2" "Word-Child3" "Excel-Child1"]  ; Cross-connection
   "Excel" ["Excel-Child1" "Excel-Child2" "Excel-Child3"]
   "PowerPoint" ["PowerPoint-Child1" "PowerPoint-Child2" "PowerPoint-Child3"]})

(def tree-config
  "Configuration for tree visualization with custom costs"
  {:root-node "Operating-System"})

(def dag-data
  (dag/build-dag-data dag-structure node-levels tree-config))

;; Data transformation helpers
(defn links->children-map
  "Convert links to a map of node -> children for efficient lookup"
  [dag-data]
  (->> (:links dag-data)
       (group-by :source)
       (map (fn [[k v]] [k (mapv :target v)]))
       (into {})))

(defn nodes->root
  "Find the root node efficiently using pre-computed sets"
  [dag-data]
  (let [nodes-set (into #{} (map :id) (:nodes dag-data))
        child-set (into #{} (map :target) (:links dag-data))]
    (first (clojure.set/difference nodes-set child-set))))

;; Memoized version of get-node-children for efficiency
(def get-node-children
  "Get children of a node from the DAG data structure (memoized)"
  (memoize
   (fn [node dag-data]
     (get (links->children-map dag-data) node []))))

;; Separate side effects from pure functions
(defn log-path
  "Log the search path (side effect)"
  [target path]
  (if path
    (println "Search Path:" path)
    (println "Path not found to target:" target))
  path)

; Breadth-First Search
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

;; Visualize BFS path to Excel-Child1
(bfs dag-data "Excel-Child1")

^:kind/hiccup
(visualize-bfs dag-data "Excel-Child1")

;; Cost function for UCS
(defn get-node-cost
  "Get the cost of a node from the DAG data"
  [node dag-data]
  (let [node-data (first (filter #(= (:id %) node) (:nodes dag-data)))]
    (:cost node-data 1)))  ; Default to 1 if cost not found

; Uniform-Cost Search
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

; Depth-First Search
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

; Depth-Limited Search
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


;; Example usage
(comment
  ;; Test different search algorithms
  (println "\nBFS path:")
  (bfs dag-data "Excel-Child1")

  (println "\nUCS path:")
  (ucs dag-data "Excel-Child1")

  (println "\nDFS path:")
  (dfs dag-data "Excel-Child1")

  (println "\nDLS path (depth limit 4):")
  (dls dag-data "Excel-Child1" 4)

  ;; Visualize different paths
  ^:kind/hiccup
  (visualize-bfs dag-data "Excel-Child1")

  ^:kind/hiccup
  (visualize-ucs dag-data "Excel-Child1")

  ^:kind/hiccup
  (visualize-dfs dag-data "Excel-Child1")

  ^:kind/hiccup
  (visualize-dls dag-data "Excel-Child1" 4))

;; Example of creating DAG data with custom costs
(def dag-data-with-costs
  (dag/build-dag-data
   dag-structure
   node-levels
   {:root-node "Operating-System"
    :node-costs {"Email" 5  ; Make Email path more expensive
                 "Browser" 2
                 "Social-Media" 3
                 "File" 2
                 "Docs" 1  ; Make Docs path cheaper
                 "Games" 4
                 "Word" 3
                 "Excel" 4
                 "PowerPoint" 5
                 "Excel-Child1" 1}}))

;; Example usage with costs
(comment
  ;; Compare paths with and without costs
  (println "\nBFS path (uniform costs):")
  (bfs dag-data "Excel-Child1")

  (println "\nUCS path (level-based costs):")
  (ucs dag-data-with-costs "Excel-Child1")

  ;; Visualize the different paths
  ^:kind/hiccup
  (visualize-bfs dag-data "Excel-Child1")

  ^:kind/hiccup
  (visualize-ucs dag-data-with-costs "Excel-Child1"))
