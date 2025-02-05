(ns ucs-plus
  (:require
   [bfs :refer [bfs visualize-bfs]]
   [task2 :refer [dag-data dag-structure node-levels]]
   [ucs :refer [ucs visualize-ucs]]
   [uniformed.dag :as dag]))

; ## UCS With Costs
; To demonstrate how **UCS** differs from **BFS** when costs are considered, we'll create a version with custom costs.

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
(visualize-ucs dag-data-with-costs "Excel-Child1")