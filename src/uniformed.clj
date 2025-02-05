(ns uniformed
  (:require [hiccup.core :as h]
            [clojure.string :as str]
            [cheshire.core :as json]))

;; Breadth-first search algorithm
(defn bfs [start goal? next-states]
  "Perform breadth-first search starting from the initial state.
   Arguments:
     start: initial state
     goal?: function that returns true if a state is goal state.
     next-states: function that returns successors of a state.
   Returns the path from start to the goal state if found, otherwise nil."
  (loop [queue (conj clojure.lang.PersistentQueue/EMPTY [start [start]])
         visited #{start}]
    (if (empty? queue)
      nil
      (let [[state path] (peek queue)
            queue (pop queue)]
        (if (goal? state)
          path
          (let [neighbors (filter (fn [s] (not (visited s))) (next-states state))
                visited (into visited neighbors)
                new-items (map (fn [s] [s (conj path s)]) neighbors)]
            (recur (into queue new-items) visited)))))))

;; Depth-first search algorithm (recursive)
(defn dfs [start goal? next-states]
  "Perform depth-first search starting from the initial state.
   Arguments:
     start: initial state
     goal?: function that returns true if a state is goal state.
     next-states: function that returns successors of a state.
   Returns the path from start to the goal state if found, otherwise nil."
  (letfn [(search [state path visited]
            (cond
              (goal? state) path
              (visited state) nil
              :else
              (loop [nbrs (next-states state)
                     visited (conj visited state)]
                (if (empty? nbrs)
                  nil
                  (let [next (first nbrs)
                        res (search next (conj path next) visited)]
                    (if res
                      res
                      (recur (rest nbrs) visited)))))))]
    (search start [start] #{})))

;; Tree definition matching the structure from Uninformed-TreeSearch.ipynb with 25 nodes
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

;; Function to print the tree in an ASCII vertical text format
(defn print-tree-ascii [tree root]
  (letfn [(visit [node indent last?]
            (println (str indent (if last? "└── " "├── ") node))
            (let [children (get tree node [])
                  cnt (count children)]
              (doseq [[i child] (map-indexed vector children)]
                (visit child (str indent (if last? "    " "│   ")) (= i (dec cnt))))))]
    (println root)
    (let [children (get tree root [])]
      (doseq [[i child] (map-indexed vector children)]
        (visit child "" (= i (dec (count children))))))))

;; Function to print the tree in a horizontal text format (level-order display)
(defn print-tree-horizontal [tree root]
  (let [levels (loop [curr [root] levels []]
                 (if (empty? curr)
                   levels
                   (let [next (vec (mapcat #(get tree % []) curr))]
                     (recur next (conj levels curr)))))]
    (doseq [lvl levels]
      (println (str/join "   " lvl)))))

;; Function to convert the tree into Hiccup (HTML) nested list format
(defn tree-to-hiccup [tree root]
  (let [children (get tree root [])]
    [:li root (when (seq children)
                [:ul (map #(tree-to-hiccup tree %) children)])]))

;; Node colors based on their level and type
(def node-colors
  {"Operating System" "#FFA500"  ; orange
   "Browser" "#FFA500"          ; orange
   "Word" "#FFA500"            ; orange
   "Docs" "#FFA500"           ; orange
   "default" "#ADD8E6"})        ; light blue

(defn get-node-color [node]
  (or (some #(when (and (string? %) (.startsWith node %))
               (get node-colors %))
            (keys node-colors))
      (get node-colors "default")))

;; Updated tree visualization with proper styling
(defn render-tree []
  (h/html
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:title "Tree Visualization"]
     [:style "
      body {
        background-color: white;
        margin: 0;
        padding: 20px;
      }
      .tree {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 40px;
      }
      .level {
        display: flex;
        justify-content: center;
        margin: 50px 0;
        width: 100%;
        position: relative;
      }
      .node {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin: 0 40px;
        position: relative;
      }
      .node-circle {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        font-size: 12px;
        font-family: Arial, sans-serif;
        color: black;
        padding: 5px;
        word-wrap: break-word;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        position: relative;
        z-index: 2;
        background-color: #ADD8E6;
      }
      .node-container {
        position: relative;
        display: flex;
        flex-direction: column;
        align-items: center;
      }
      .connector {
        position: absolute;
        background-color: #999;
      }
      .connector-vertical {
        width: 2px;
        height: 50px;
        top: 80px;
        left: 50%;
        transform: translateX(-50%);
      }
      .connector-horizontal {
        height: 2px;
        top: 40px;
      }
     "]]
    [:body
     [:div.tree
      (letfn [(create-level [nodes]
                [:div.level
                 (for [node nodes]
                   (let [children (get tree node [])]
                     [:div.node-container
                      [:div.node
                       [:div.node-circle {:style (str "background-color: " (get-node-color node) ";")}
                        node]]
                      (when (seq children)
                        [:div.connector.connector-vertical])
                      (when (> (count children) 1)
                        [:div.connector.connector-horizontal
                         {:style (str "width: " (* (dec (count children)) 160) "px;"
                                      "left: " (* -80 (dec (count children))) "px;")}])
                      (when (seq children)
                        (create-level children))]))])]
        (create-level ["Operating System(0)"]))]]]))

;; Extended example usage (commented out):
(comment
  ;; Demonstration of search algorithms on the defined tree.
  (defn successors [node]
    (get tree node []))

  ;; Goal test for demonstration; e.g., looking for "Games".
  (defn goal? [node]
    (= node "Games"))

  (println "BFS path:" (bfs "Operating System(0)" goal? successors))
  (println "DFS path:" (dfs "Operating System(0)" goal? successors))

  ;; Print the tree structure in vertical format:
  (println "\nVertical Tree Structure:")
  (print-tree-ascii tree "Operating System(0)")

  ;; Print the tree structure in horizontal format:
  (println "\nHorizontal Tree Structure:")
  (print-tree-horizontal tree "Operating System(0)")

  ;; Render the tree as HTML using Hiccup:
  (println "\nHTML Tree Visualization:")
  ^:kind/hiccup
  (render-tree))

(comment
  (defn htmx-button []
    [:button
     {:hx-post "/clicked"
      :hx-trigger "click"
      :hx-target "#result"
      :hx-swap "innerHTML"}
     "Click me"])

  (defn htmx-result []
    [:div#result "Result will appear here"])

  (defn htmx-page []
    (h/html
     [:head
      [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]]
     [:body
      (htmx-button)
      (htmx-result)]))

  ^:kind/hiccup
  (htmx-page))

(defn build-d3-tree [tree root]
  (let [children (get tree root)]
    (if (empty? children)
      {:name root}
      {:name root
       :children (mapv #(build-d3-tree tree %) children)})))

(defn render-d3-tree []
  (let [tree-data (json/generate-string (build-d3-tree tree "Operating System(0)"))
        js-code (str "
      const data = " tree-data ";
      
      const width = window.innerWidth;
      const height = window.innerHeight;
      const nodeRadius = 40;
      
      const tree = d3.tree()
        .nodeSize([100, 160])
        .separation((a, b) => (a.parent == b.parent ? 1.5 : 2));
      
      const root = d3.hierarchy(data);
      tree(root);
      
      const svg = d3.select('#tree-container')
        .append('svg')
        .attr('width', width)
        .attr('height', height)
        .append('g')
        .attr('transform', `translate(${width/2},80)`);
      
      // Add links
      svg.selectAll('.link')
        .data(root.links())
        .join('path')
        .attr('class', 'link')
        .attr('d', d3.linkVertical()
          .x(d => d.x)
          .y(d => d.y));
      
      // Add nodes
      const node = svg.selectAll('.node')
        .data(root.descendants())
        .join('g')
        .attr('class', d => {
          const isOrange = ['Operating System(0)', 'Browser(2)', 'Word', 'Docs']
            .includes(d.data.name);
          return `node ${isOrange ? 'orange' : ''}`;
        })
        .attr('transform', d => `translate(${d.x},${d.y})`);
      
      // Add circles
      node.append('circle')
        .attr('r', nodeRadius);
      
      // Add labels
      node.append('text')
        .text(d => d.data.name)
        .attr('dy', '0.3em')
        .style('font-size', '10px')
        .call(wrap, nodeRadius * 1.8);
      
      // Text wrapping function
      function wrap(text, width) {
        text.each(function() {
          const text = d3.select(this);
          const words = text.text().split(/\\s+/);
          const lines = [];
          let line = [];
          
          words.forEach(word => {
            line.push(word);
            if (line.join(' ').length > width/4) {
              lines.push(line.join(' '));
              line = [];
            }
          });
          if (line.length > 0) lines.push(line.join(' '));
          
          const lineHeight = 1.1;
          text.text(null);
          lines.forEach((l, i) => {
            text.append('tspan')
              .text(l)
              .attr('x', 0)
              .attr('dy', i === 0 ? -((lines.length-1) * lineHeight)/2 + 'em' : lineHeight + 'em');
          });
        });
      }
      ")]
    (h/html
     [:html
      [:head
       [:meta {:charset "UTF-8"}]
       [:title "Tree Visualization"]
       [:script {:src "https://d3js.org/d3.v7.min.js"}]
       [:style "
        body { 
          margin: 0;
          font-family: Arial, sans-serif;
        }
        .node circle {
          fill: #ADD8E6;
          stroke: #666;
          stroke-width: 1.5px;
        }
        .node text {
          font-size: 12px;
          text-anchor: middle;
          dominant-baseline: middle;
        }
        .link {
          fill: none;
          stroke: #999;
          stroke-width: 1.5px;
        }
        .orange circle {
          fill: #FFA500;
        }
       "]]
      [:body
       [:div#tree-container {:style "width: 100vw; height: 100vh;"}]
       [:script {:type "text/javascript"} js-code]]])))

;; Update the comment block to include the new D3 visualization
(comment
  ;; Previous examples remain the same...

  ;; Render the D3 tree visualization:
  ^:kind/hiccup
  (render-d3-tree))