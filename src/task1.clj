(ns task1
  (:require
   [uniformed.dag :as dag]
   [uniformed.viz :refer [visualize-tree]]))

; ## Visualize Data
; **Task 1: Modify the code so that at least two of the tree nodes at Level 3 have their children.** (10 pts)

; Our implementation focuses on extending a **hierarchical tree structure** to demonstrate the organization of an _operating system's application ecosystem_. The task requires adding child nodes to *Level 3* of our tree, deepening the representation of `document processing applications`.

; ### Mathematical Foundation
; The mathematical foundation of our implementation rests on a **Directed Acyclic Graph (DAG)** expressed as $T = (V, E)$. The _vertex set_ $V$ comprises nodes organized across multiple levels $L = \{L_0, L_1, L_2, L_3, L_4\}$, while the _edge set_ $E$ defines connections between adjacent levels through $e_{ij}$ relationships. This formal structure ensures a **clear hierarchical organization** while preventing *cyclic dependencies*.

; ### Core Data Structures
; The `node-levels` map forms the **structural foundation** of our hierarchy. Starting from the _root node_ at the operating system, we progress through increasingly specialized layers of functionality. **Primary applications** serve as major categorical divisions, branching into _specific functionality domains_, and culminating in specialized `document processing applications`. This progression creates a natural *taxonomy of software functionality*.

(def node-levels
  {0 #{"Operating-System"}
   1 #{"Email" "Browser" "Social-Media"}
   2 #{"File" "Chat" "Cloud" "Docs" "Maps" "Search" "Games" "Photos" "Videos"}
   3 #{"Word" "Excel" "PowerPoint"}})

; The `dag-structure` implements our **hierarchical relationships** through an _adjacency list representation_. Each *parent node* maintains explicit connections to its children, creating a clear path of specialization through the system. To fulfill our requirements, we extend the `Level 3` nodes `Word`, `Excel`, and `PowerPoint` with child nodes, demonstrating deeper specialization in document processing capabilities.

(def dag-structure
  {"Operating-System" ["Email" "Browser" "Social-Media"]
   "Email" ["File" "Chat" "Cloud"]
   "Browser" ["Docs" "Maps" "Search"]
   "Social-Media" ["Games" "Photos" "Videos"]
   "File" ["Word" "Excel" "PowerPoint"]
   "Docs" ["Word" "Excel" "PowerPoint"]
   "Games" ["Word" "Excel" "PowerPoint"]
   "Word" ["Word-Child1" "Word-Child2" "Word-Child3"]
   "Excel" ["Excel-Child1" "Excel-Child2" "Excel-Child3"]
   "PowerPoint" ["PowerPoint-Child1" "PowerPoint-Child2" "PowerPoint-Child3"]})

; The `tree-config` ensures **consistent and reproducible rendering** of our hierarchy. By specifying the _root node_ and enforcing a deliberate ordering of *Level 1* nodes, we maintain a predictable visual representation that clearly communicates the system's structure.

(def tree-config
  {:root-node "Operating-System"
   :level-1-order ["Browser" "Email" "Social-Media"]})

; ### Graph Construction
; The **DAG construction process** transforms our conceptual structure into a concrete visualization format. This transformation begins with _validation_ of the DAG structure, ensuring consistency with our defined levels. The process continues by establishing and verifying *parent-child relationships*, maintaining the integrity of our hierarchical design. Each node receives its appropriate level assignment, creating a clear vertical organization of functionality.

(comment
  (defn build-dag-data
    [dag-structure node-levels {:keys [root-node level-1-order
                                       parent-category-order node-costs] :as config}]
    (let [level-1-nodes (or level-1-order
                            (vec (get dag-structure root-node)))
          parent-order (or parent-category-order
                           (zipmap level-1-nodes (range)))
          cost-fn (cond
                    (fn? node-costs) node-costs
                    (map? node-costs) #(get node-costs % 1)
                    :else (constantly 1))
          complete-config (assoc config
                                 :level-1-order level-1-nodes
                                 :parent-category-order parent-order
                                 :dag-structure dag-structure)
          width 800
          all-nodes (distinct (concat (keys dag-structure)
                                      (mapcat identity (vals dag-structure))))
          nodes-with-levels (map (fn [node]
                                   {:id node
                                    :name node
                                    :level (get-node-level node node-levels 4)
                                    :cost (cost-fn node)
                                    :parent (get-parent node dag-structure)})
                                 all-nodes)
          nodes-by-level (group-by :level nodes-with-levels)
          nodes (vec (mapcat #(position-nodes (get nodes-by-level %) % width complete-config)
                             (sort (keys nodes-by-level))))
          links (vec (for [[parent children] dag-structure
                           child children]
                       {:source parent
                        :target child
                        :cost (cost-fn child)}))]
      {:nodes nodes :links links})))

; The `build-dag-data` function orchestrates this transformation, creating a comprehensive **graph representation**. _Node metadata_ captures essential properties including `level placement`, `relationships`, and `costs`. *Edge definitions* formalize the connections between components, while _layout constraints_ ensure aesthetic presentation. This refined data structure serves as the foundation for our visualization system, enabling clear communication of the system's **hierarchical nature**.

(def dag-data
  (dag/build-dag-data dag-structure node-levels tree-config))

(second dag-data)

; ### Final Visualization
; Using the `uniformed.viz` library's `visualize-tree` function, we render our **hierarchical structure** into a clear, visual representation. The resulting visualization demonstrates the _complete system organization_, from the root operating system through all levels of specialization, including our extended *Level 3* document processing capabilities.

^:kind/hiccup
(visualize-tree dag-data)
