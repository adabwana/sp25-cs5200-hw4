(ns task3
  (:require
   [task2 :refer [bfs visualize-bfs dfs visualize-dfs
                  dls visualize-dls ucs visualize-ucs]]
   [uniformed.dag :as dag]
   [hiccup.core :as h]))

; ## Task 3: Search Comparison
; **Run and show the outputs (i.e., text output and visualization) of each algorithm.** (15 pts.)

; Our implementation demonstrates and compares four fundamental **graph search algorithms** through both textual output and visual representation. Each algorithm's unique characteristics become apparent through their different traversal patterns.


(def node-levels
  {0 #{"Operating-System"}
   1 #{"Email" "Browser" "Social-Media"}
   2 #{"File" "Chat" "Cloud" "Docs" "Maps" "Search" "Games" "Photos" "Videos"}
   3 #{"Word" "Excel" "PowerPoint"}
   4 #{"Word-Child1" "Word-Child2" "Word-Child3"
       "Excel-Child1" "Excel-Child2" "Excel-Child3"
       "PowerPoint-Child1" "PowerPoint-Child2" "PowerPoint-Child3"}})

(def dag-structure
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
  {:root-node "Operating-System"})

(def dag-data
  (dag/build-dag-data dag-structure node-levels tree-config))

; ## Algorithm Visualizations
; We'll demonstrate each search algorithm finding a path to "Excel-Child1", which can be reached through multiple paths.
; This target node allows us to clearly observe the different strategies each algorithm employs.

; ### Breadth-First Search Results
(bfs dag-data "Excel-Child1")

^:kind/hiccup
(visualize-bfs dag-data "Excel-Child1")

; ### Uniform-Cost Search Results
(ucs dag-data "Excel-Child1")

^:kind/hiccup
(visualize-ucs dag-data "Excel-Child1")

; ### Depth-First Search Results
(dfs dag-data "Excel-Child1")

^:kind/hiccup
(visualize-dfs dag-data "Excel-Child1")

; ### Depth-Limited Search Results
(dls dag-data "Excel-Child1" 4)

^:kind/hiccup
(visualize-dls dag-data "Excel-Child1" 4)

; ## Cost-Aware Search Comparison
; To demonstrate how UCS differs from BFS when costs are considered, we'll create a version with custom costs.

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

; ### Cost-Aware Path Comparison
(bfs dag-data "Excel-Child1")
(ucs dag-data-with-costs "Excel-Child1")

^:kind/hiccup
(visualize-bfs dag-data "Excel-Child1")

^:kind/hiccup
(visualize-ucs dag-data-with-costs "Excel-Child1")