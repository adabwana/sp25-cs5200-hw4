(ns uniformed.dag)

(defn get-node-level
  "Get the level of a node from the node-levels map. Returns default-level if not found."
  [node node-levels default-level]
  (or (first (keep (fn [[level nodes]]
                     (when (nodes node) level))
                   node-levels))
      default-level))

(defn get-parent
  "Get the parent of a node from the DAG structure."
  [node dag-structure]
  (first (for [[parent children] dag-structure
               :when (some #{node} children)]
           parent)))

(defn get-root-parent
  "Get the root parent of a node by traversing up the parent chain."
  [node parent-fn parent-category-order]
  (when-let [parent (parent-fn node)]
    (if (get parent-category-order parent)
      parent
      (recur parent parent-fn parent-category-order))))

;; Node sorting functions
(defn sort-level-1-nodes
  "Sort level 1 nodes according to predefined order."
  [nodes level-1-order]
  (sort-by #(.indexOf level-1-order (:id %)) nodes))

(defn sort-level-2-nodes
  "Sort level 2 nodes based on their parent's order and position within parent's children."
  [nodes parent-category-order dag-structure]
  (let [get-parent-idx #(get parent-category-order (:parent %) 0)
        get-child-idx #(.indexOf (get dag-structure (:parent %)) (:id %))]
    (sort-by (juxt get-parent-idx get-child-idx) nodes)))

(defn sort-level-3-4-nodes
  "Sort level 3 and 4 nodes based on their root parent's order."
  [nodes root-node dag-structure get-parent-fn parent-category-order]
  (let [parent-order (zipmap (get dag-structure root-node) (range))
        get-parent-idx #(get parent-order
                             (get-root-parent (:id %)
                                              get-parent-fn
                                              parent-category-order)
                             0)]
    (sort-by get-parent-idx nodes)))

(defn sort-nodes-by-level
  "Sort nodes within their level according to the hierarchical structure."
  [nodes level {:keys [level-1-order parent-category-order dag-structure root-node]}]
  (cond
    (= level 0) nodes
    (= level 1) (sort-level-1-nodes nodes level-1-order)
    (= level 2) (sort-level-2-nodes nodes parent-category-order dag-structure)
    :else (sort-level-3-4-nodes nodes
                                root-node
                                dag-structure
                                #(get-parent % dag-structure)
                                parent-category-order)))

(defn position-nodes
  "Position nodes within their level with even spacing."
  [nodes level width sort-config]
  (let [sorted-nodes (sort-nodes-by-level nodes level sort-config)
        total (count sorted-nodes)
        spacing (/ width (inc total))]
    (map-indexed
     (fn [idx node]
       (assoc node
              :x (- (* (inc idx) spacing) (/ width 2))
              :y (* level 150)))
     sorted-nodes)))

(defn add-highlighting
  "Add highlighting information to nodes and links based on a path.
   Returns updated DAG data with highlighted nodes and links."
  [dag-data highlighted-path]
  (if highlighted-path
    (-> dag-data
        (update :nodes (fn [nodes]
                         (mapv #(assoc % :highlighted
                                       (some #{(:id %)} highlighted-path))
                               nodes)))
        (update :links (fn [links]
                         (mapv #(assoc % :highlighted
                                       (and (some #{(:source %)} highlighted-path)
                                            (some #{(:target %)} highlighted-path)
                                            (= (.indexOf highlighted-path (:target %))
                                               (inc (.indexOf highlighted-path (:source %))))))
                               links))))
    dag-data))

(defn build-dag-data
  "Convert tree to DAG format with specific hierarchical layout.
   Accepts:
   - dag-structure: Map of parent-child relationships
   - node-levels: Map of levels to sets of nodes
   - config: Map containing:
     - root-node: The name of the root node (required)
     - level-1-order: Vector of level 1 nodes in desired order (optional)
     - parent-category-order: Map of parent categories to their order (optional)
     - node-costs: Map of node to cost, or function that takes node and returns cost (optional)
       If not provided, all nodes have cost 1"
  [dag-structure node-levels {:keys [root-node level-1-order parent-category-order node-costs] :as config}]
  (let [;; If level-1-order is not provided, get level 1 nodes from dag-structure
        level-1-nodes (or level-1-order
                          (vec (get dag-structure root-node)))
        ;; If parent-category-order is not provided, create from level-1-nodes
        parent-order (or parent-category-order
                         (zipmap level-1-nodes (range)))
        ;; Create default cost function if not provided
        cost-fn (cond
                  (fn? node-costs) node-costs
                  (map? node-costs) #(get node-costs % 1)
                  :else (constantly 1))
        ;; Create complete config with defaults
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
    {:nodes nodes :links links}))

#_(build-dag-data
   {"A" ["B" "C"]
    "B" ["D" "E"]
    "C" ["F"]}
   {0 #{"A"}
    1 #{"B" "C"}
    2 #{"D" "E" "F"}}
   {:root-node "A"})

(comment
  (defrecord TreeNode [name cost children])

  (defn create-node
    "Create a tree node with a name and cost. Similar to Python's TreeNode constructor."
    ([name cost]
     (->TreeNode name cost []))
    ([name cost children]
     (->TreeNode name cost children)))

  (defn add-children!
    "Add children to a node. Returns the modified node."
    [node children]
    (assoc node :children children))

  (defn create-office-children
    "Create Word, Excel, and PowerPoint nodes with their costs"
    [parent-cost]
    [(create-node "Word" (+ parent-cost 1))
     (create-node "Excel" (+ parent-cost 2))
     (create-node "PowerPoint" (+ parent-cost 3))])

  (defn add-word-children
    "Add child nodes to Word nodes if parent is Docs"
    [parent-name office-nodes]
    (if (= parent-name "Docs")
      (map (fn [node]
             (if (= (:name node) "Word")
               (add-children! node
                              [(create-node "Word-Child1" (+ (:cost node) 1))
                               (create-node "Word-Child2" (+ (:cost node) 2))
                               (create-node "Word-Child3" (+ (:cost node) 3))])
               node))
           office-nodes)
      office-nodes))

  (defn create-full-tree
    "Create the full tree structure similar to Python's create_full_tree()"
    []
    (let [;; Create root and main category nodes
          root (create-node "Operating-System" 0)
          email (create-node "Email" 1)
          browser (create-node "Browser" 2)
          social-media (create-node "Social-Media" 3)

          ;; Create second level nodes
          email-children [(create-node "File" 4)
                          (create-node "Chat" 5)
                          (create-node "Cloud" 6)]
          browser-children [(create-node "Docs" 7)
                            (create-node "Maps" 8)
                            (create-node "Search" 9)]
          social-media-children [(create-node "Games" 10)
                                 (create-node "Photos" 11)
                                 (create-node "Videos" 12)]

          ;; Add children to main categories
          email-with-children (add-children! email email-children)
          browser-with-children (add-children! browser browser-children)
          social-media-with-children (add-children! social-media social-media-children)

          ;; Add office children to specific nodes
          nodes-needing-office [["File" 4] ["Docs" 7] ["Games" 10]]
          updated-tree (reduce (fn [tree [parent-name parent-cost]]
                                 (update-in tree
                                            [:children (case parent-name
                                                         "File" 0
                                                         "Docs" 1
                                                         "Games" 2)
                                             :children]
                                            (fn [_] (add-word-children parent-name
                                                                       (create-office-children parent-cost)))))
                               (add-children! root [email-with-children
                                                    browser-with-children
                                                    social-media-with-children])
                               nodes-needing-office)]
      updated-tree))

  (defn tree->map
    "Convert TreeNode structure to map structure for visualization"
    [node]
    (let [children (:children node)]
      (if (seq children)
        (assoc {} (:name node) (mapv :name children))
        {})))

  (defn build-tree-structure
    "Build the complete tree structure map from a TreeNode"
    [root]
    (let [result (atom {})]
      (letfn [(process-node [node]
                (when (seq (:children node))
                  (swap! result assoc (:name node) (mapv :name (:children node)))
                  (doseq [child (:children node)]
                    (process-node child))))]
        (process-node root)
        @result)))

  ;; Create the tree structure and DAG data
  (def tree (build-tree-structure (create-full-tree)))
  (def dag-data (build-dag-data tree)))