(ns networkclj.viz
  (:require
   [networkclj.map-to-json :refer [graph-data graph-data-json d3-tree-json d3-tree-single-parent]]
   [hiccup.page :refer [html5 include-js]]
   [scicloj.kindly.v4.kind :as kind]))

(defn page []
  (html5
   [:head
    [:title "HTMX + vis.js Graph"]
    ;; Load vis.js from a CDN or local
    [:script {:src "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js"}]]
   [:body
    [:h1 "Graph Demo"]
    [:div#network  ;; The container where we'll render the graph
     {:style "width: 800px; height: 600px; border: 1px solid #ccc;"}]

    ;; Then an inline script that uses `graph-data-json`:
    [:script
     (str "const container = document.getElementById('network');\n"
          "const data = " graph-data-json ";\n"  ;; embed your JSON
          "const options = {\n"
          "  nodes: { shape: 'dot', size: 16 },\n"
          "  edges: { arrows: 'to' },\n"
          "  physics: { enabled: true }\n"
          "};\n"
          "const network = new vis.Network(container, data, options);\n")]]))

^:kind/hiccup
(page)

(defn d3-tree-page []
  (html5
   [:head
    [:title "D3 Tree Example"]
    [:script {:src "https://d3js.org/d3.v7.min.js"}]]
   [:body
    [:h1 "D3 Hierarchical Tree"]
    [:svg {:id "mysvg"
           :width 1000
           :height 800}]
    [:script
     (str "
      const data = " d3-tree-json ";

      // Set up dimensions, margins
      const width = 1000, height = 800;
      const svg = d3.select('#mysvg'),
            g = svg.append('g').attr('transform', 'translate(50,50)');

      // Create a tree layout
      const treemap = d3.tree().size([height-100, width-200]);
      let root = d3.hierarchy(data, d => d.children);
      root = treemap(root);

      // Links
      g.selectAll('.link')
       .data(root.links())
       .enter().append('path')
         .attr('class', 'link')
         .attr('fill', 'none')
         .attr('stroke', '#555')
         .attr('d', d3.linkHorizontal()
                     .x(d => d.y)
                     .y(d => d.x));

      // Nodes
      const node = g.selectAll('.node')
                    .data(root.descendants())
                    .enter().append('g')
                      .attr('class', 'node')
                      .attr('transform', d => 'translate(' + d.y + ',' + d.x + ')');

      node.append('circle')
          .attr('r', 8)
          .attr('fill', '#999');

      node.append('text')
          .attr('dy', '.35em')
          .attr('x', d => d.children ? -12 : 12)
          .style('text-anchor', d => d.children ? 'end' : 'start')
          .text(d => d.data.name);
     ")]]))

^:kind/hiccup
(d3-tree-page)

(defn graphify [m]
  (let [all-nodes (set (concat (keys m) (mapcat val m)))
        nodes     (mapv (fn [n] {:id n}) all-nodes)
        links     (mapcat (fn [[parent children]]
                            (for [child children]
                              {:source parent :target child}))
                          m)]
    {:nodes nodes
     :links (vec links)}))

(defn single-parent-items
  [{:keys [nodes links]}]
  (let [target->parents (group-by :target links)]
    (mapv (fn [{:keys [id]}]
            (let [possible-parents (target->parents id)
                  first-parent     (-> possible-parents first :source)]
              {:id id
               :parentId first-parent}))
          nodes)))

(defn d3-tree-single-parent-page
  "Returns an HTML page (as Hiccup) that draws a single‐parent D3 tree
   from the given `graph` data."
  [graph]
  (html5
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "D3 Single-Parent Tree Example"]
       ;; Include D3 from a CDN:
    [:script {:src "https://d3js.org/d3.v7.min.js"}]]
   [:body
    [:h1 "D3 Tree Layout"]
       ;; We'll create an <svg> container and let the JS code do the rest.
    [:svg {:id "mysvg" :width 1000 :height 600
           :style "border:1px solid #ccc;"}]

       ;; Inline script that does the D3 magic
    [:script
     (str "
         const rawData = " d3-tree-single-parent ";
         const margin = {top: 30, right: 40, bottom: 30, left: 100},
         width = 1000 - margin.left - margin.right,
         height = 600 - margin.top - margin.bottom;

         const svg = d3.select('#mysvg')
         .append('g')
         .attr('transform', `translate(${margin.left},${margin.top})`);

         const stratifier = d3.stratify()
         .id(d => d.id)
         .parentId(d => d.parentId);
         let root = stratifier(rawData);


         // Create the tree layout.
         const treeLayout = d3.tree()
         .size([height, width]);
         root = treeLayout(root);


         // Draw links.
         svg.selectAll('.link')
         .data(root.links())
         .enter().append('path')
         .attr('class', 'link')
         .attr('fill', 'none')
         .attr('stroke', '#999')
         .attr('d', d3.linkHorizontal()
                     .x(d => d.y)
                     .y(d => d.x));

         // Draw nodes.
         const node = svg.selectAll('.node')
         .data(root.descendants())
         .enter().append('g')
         .attr('class', 'node')
         .attr('transform', d => `translate(${d.y},${d.x})`);

         node.append('circle')
         .attr('r', 5)
         .attr('fill', '#666');

         node.append('text')
         .attr('dy', '0.35em')
         .attr('x', d => d.children ? -10 : 10)
         .style('text-anchor', d => d.children ? 'end' : 'start')
         .text(d => d.id);")]]))

^:kind/hiccup
(d3-tree-single-parent-page graph-data)
