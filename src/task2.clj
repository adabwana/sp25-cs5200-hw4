(ns task2
  (:require
   [clojure.set :as set]
   [uniformed.dag :as dag]
   [uniformed.viz :refer [visualize-tree]]))

; ## Helpers & Data Redux
; **Task 2: Complete implementation of the Uniform-Cost Search (UCS), Depth-First Search (DFS), Depth-Limited Search (DLS) with a limit of 3.** (45 pts.)

; Our implementation explores four fundamental **uninformed search algorithms**: _Breadth-First Search_ (BFS), _Uniform-Cost Search_ (UCS), _Depth-First Search_ (DFS), and _Depth-Limited Search_ (DLS). Each algorithm represents a distinct approach to traversing our hierarchical structure, with varying trade-offs between completeness, optimality, and resource utilization.

; ### Mathematical Foundation
; The search algorithms operate on a weighted DAG defined as $G = (V, E, w)$, where:

; - $V$ represents the set of vertices in our graph
; - $E$ represents the directed edges between vertices
; - $w: E \rightarrow \mathbb{R}^+$ assigns positive weights to edges

; Each algorithm implements a distinct strategy for exploring this structure, maintaining a *frontier* $F$ and *explored set* $E$ during traversal.

; ### Helper Functions
; The foundation of our search implementations relies on efficient data structure transformations. These helper functions convert our graph representation into optimized formats for traversal operations.

; The `links->children-map` function transforms our edge list into an adjacency list representation, enabling $O(1)$ access to a node's children. This optimization is important for efficient graph traversal.

(defn links->children-map
  "Convert links to a map of node -> children for efficient lookup"
  [dag-data]
  (->> (:links dag-data)
       (group-by :source)
       (map (fn [[k v]] [k (mapv :target v)]))
       (into {})))

; The `nodes->root` function efficiently identifies the root node by computing set differences between all nodes and child nodes. This operation ensures $O(n)$ complexity for root identification.

(defn nodes->root
  "Find the root node efficiently using pre-computed sets"
  [dag-data]
  (let [nodes-set (into #{} (map :id) (:nodes dag-data))
        child-set (into #{} (map :target) (:links dag-data))]
    (first (set/difference nodes-set child-set))))

; We optimize child node lookup through memoization, preventing redundant computations during search operations. The `get-node-children` function provides cached access to a node's children.

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

; ### Data Revision Motivation
; The original graph structure from Task 1 presented a limitation for algorithmic analysis: its uniform path distribution resulted in identical solutions across different search strategies. To enable meaningful comparison of search behaviors, we introduce minor modifications to the graph structure:

; 1. **Direct Path Addition**: We establish a direct connection from `Email` to `Excel`, creating a shorter but potentially higher-cost path ($w_{Email,Excel} > \sum w_{i,j}$ of alternative routes). This modification specifically challenges UCS's optimality property.

; 2. **Cross-Connection Implementation**: The addition of an edge from `Word` to `Excel-Child1` creates intersecting paths, forcing search algorithms to handle more complex graph traversal scenarios. This modification particularly affects DFS and DLS behavior when exploring different branches.

; These structural modifications ensure that each search algorithm (`BFS`, `UCS`, `DFS`, `DLS`) may now produce distinct paths based on their characteristic behaviors:

; - `BFS` will favor shorter paths in terms of edge count
; - `UCS` will optimize for cumulative edge weights
; - `DFS` will commit to deep path exploration
; - `DLS` will restrict exploration depth while maintaining DFS characteristics

(def node-levels
  {0 #{"Operating-System"}
   1 #{"Browser" "Email" "Social-Media"}
   2 #{"Chat" "Cloud" "Docs" "File" "Games" "Maps" "Photos" "Search" "Videos"}
   3 #{"Excel" "PowerPoint" "Word"}
   4 #{"Word-Child1" "Word-Child2" "Word-Child3"
       "Excel-Child1" "Excel-Child2" "Excel-Child3"
       "PowerPoint-Child1" "PowerPoint-Child2" "PowerPoint-Child3"}})

(def dag-structure
  {"Operating-System" ["Browser" "Email" "Social-Media"]
   "Email" ["Chat" "Cloud" "Excel" "File"]  ; Direct path to Excel (shorter but costly)
   "Browser" ["Docs" "Maps" "Search"]
   "Social-Media" ["Games" "Photos" "Videos"]
   "File" ["Excel" "PowerPoint" "Word"]
   "Docs" ["Excel" "PowerPoint" "Word"]
   "Games" ["Excel" "PowerPoint" "Word"]
   "Word" ["Word-Child1" "Word-Child2" "Word-Child3" "Excel-Child1"]  ; Cross-connection
   "Excel" ["Excel-Child1" "Excel-Child2" "Excel-Child3"]
   "PowerPoint" ["PowerPoint-Child1" "PowerPoint-Child2" "PowerPoint-Child3"]})

(def tree-config
  {:root-node "Operating-System"})

(def dag-data
  (dag/build-dag-data dag-structure node-levels tree-config))

^:kind/hiccup
(visualize-tree dag-data)

