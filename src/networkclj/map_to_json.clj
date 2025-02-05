(ns networkclj.map-to-json
  (:require [cheshire.core :as json]))

(def tree
  {"Operating System(0)" ["Email(1)" "Browser(2)" "Social Media(3)"]
   "Email(1)" ["File" "Chat" "Cloud"]
   "Browser(2)" ["Docs" "Maps" "Search"]
   "Social Media(3)" ["Games" "Photos" "Videos"]
   "File" ["Word" "Excel" "PowerPoint"]
   "Chat" []
   "Cloud" []
   "Docs" ["Word" "Excel" "PowerPoint"]
   "Maps" []
   "Search" []
   "Games" ["Word" "Excel" "PowerPoint"]
   "Word" ["Word-Child1" "Word-Child2" "Word-Child3"]
   "Word-Child1" []
   "Word-Child2" []
   "Word-Child3" []
   "Excel" ["Excel-Child1" "Excel-Child2" "Excel-Child3"]
   "Excel-Child1" []
   "Excel-Child2" []
   "Excel-Child3" []
   "PowerPoint" ["PowerPoint-Child1" "PowerPoint-Child2" "PowerPoint-Child3"]
   "PowerPoint-Child1" []
   "PowerPoint-Child2" []
   "PowerPoint-Child3" []})

(defn tree->nodes+edges [tree]
  (let [; gather all nodes
        all-nodes (set (concat (keys tree) (mapcat val tree)))
        nodes     (mapv (fn [n] {:id n :label n}) all-nodes)
        edges     (mapcat (fn [[parent children]]
                            (map (fn [c] {:from parent :to c}) children))
                          tree)]
    {:nodes nodes
     :edges (vec edges)}))

(def graph-data (tree->nodes+edges tree))

;; If you want this as JSON (for the client side):
(def graph-data-json
  (json/generate-string graph-data))

(defn build-d3-tree-simple
  "Given a map of parent->children and a `root` string, 
   produce a nested map for D3's hierarchical layouts."
  [tree root]
  (let [children (get tree root)]
    (if (empty? children)
      {:name root}
      {:name root
       :children (mapv (partial build-d3-tree-simple tree) children)})))

(defn build-d3-tree
  [g root & [visited]]
  (let [visited (or visited (atom #{}))]
    (if (contains? @visited root)
      ;; Already visited --> do NOT expand again
      {:name root, :children []}
      (do
        (swap! visited conj root)
        (let [children (g root)]
          (if (empty? children)
            {:name root}
            {:name root
             :children (mapv #(build-d3-tree g % visited) children)}))))))

;; Example:
(def d3-tree-json
  (json/generate-string (build-d3-tree tree "Operating System(0)")))



(defn graphify [m]
  (let [all-nodes (set (concat (keys m) (mapcat val m)))
        nodes     (mapv (fn [n] {:id n}) all-nodes)
        links     (mapcat (fn [[parent children]]
                            (for [child children]
                              {:source parent :target child}))
                          m)]
    {:nodes nodes
     :links (vec links)}))

(def graph-data (graphify tree))

(defn single-parent-items
  [{:keys [nodes links]}]
  (let [target->parents (group-by :target links)]
    (mapv (fn [{:keys [id]}]
            (let [possible-parents (target->parents id)
                  first-parent     (-> possible-parents first :source)]
              {:id id
               :parentId first-parent}))
          nodes)))

(def d3-tree-single-parent (single-parent-items graph-data))