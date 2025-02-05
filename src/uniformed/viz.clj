(ns uniformed.viz
  (:require
   [hiccup.core :as h]
   [hiccup.page :refer [html5]]
   [cheshire.core :as json]
   [uniformed.dag :as dag]))

;; CSS styles for DAG visualization (unchanged)
(def ^:private dag-styles
  "body { 
     margin: 0;
     padding: 0;
     overflow: auto;
     background: white;
   }
   .tree-container {
    width: fit-content;
    min-width: 800px;
    margin: 0 auto;
    height: 100%;
    min-height: 500px;
    background: white;
    overflow: auto;
  }
  .node circle {
    fill: #ADD8E6;
    stroke: #666;
    stroke-width: 1.5px;
  }
  .node.highlighted circle {
    fill: #FFA500 !important;
    stroke: #666;
    stroke-width: 2px;
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
  .link.highlighted {
    stroke: #FFA500 !important;
    stroke-width: 2.5px;
  }
  ")

(def ^:private d3-script
  "D3.js visualization script"
  "
  const data = DATA_PLACEHOLDER;
  console.log('DAG data:', data);
  
  // Calculate required width based on node positions
  const nodeExtent = d3.extent(data.nodes, d => d.x);
  const width = Math.max(800, nodeExtent[1] - nodeExtent[0] + 200); // Add padding
  const height = window.innerHeight;
  const nodeRadius = 30;
  
  const svg = d3.select('.tree-container')
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .append('g')
    .attr('transform', `translate(${width/2},60)`);

  // Create links
  const link = svg.append('g')
    .selectAll('path')
    .data(data.links)
    .join('path')
    .attr('class', d => d.highlighted ? 'link highlighted' : 'link')
    .attr('d', d => {
      const sourceNode = data.nodes.find(n => n.id === d.source);
      const targetNode = data.nodes.find(n => n.id === d.target);
      return `M${sourceNode.x},${sourceNode.y}
              C${sourceNode.x},${(sourceNode.y + targetNode.y) / 2}
               ${targetNode.x},${(sourceNode.y + targetNode.y) / 2}
               ${targetNode.x},${targetNode.y}`;
    });

  // Create nodes
  const node = svg.append('g')
    .selectAll('.node')
    .data(data.nodes)
    .join('g')
    .attr('class', d => d.highlighted ? 'node highlighted' : 'node')
    .attr('transform', d => `translate(${d.x},${d.y})`);

  // Add circles to nodes
  node.append('circle')
    .attr('r', nodeRadius);

  // Add labels to nodes
  node.append('text')
    .text(d => d.id)
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

  // Add zoom behavior
  const zoom = d3.zoom()
    .scaleExtent([0.1, 4])
    .on('zoom', (event) => {
      svg.attr('transform', event.transform);
    });

  d3.select('svg').call(zoom);
  ")

(defn render-hierarchical-dag
  "Render a hierarchical DAG visualization using D3.js"
  [dag-data & [highlighted-path]]
  (let [graph-data (if highlighted-path
                     (dag/add-highlighting dag-data highlighted-path)
                     dag-data)
        graph-data-json (json/generate-string graph-data)]
    (html5
     [:html
      [:head
       [:meta {:charset "UTF-8"}]
      ;;  [:title "Hierarchical DAG Visualization"]
       [:script {:src "https://d3js.org/d3.v7.min.js"}]
       [:style dag-styles]]
      [:body
       [:div.tree-container]
       [:script {:type "text/javascript"}
        (-> d3-script
            (clojure.string/replace "DATA_PLACEHOLDER" graph-data-json))]]])))

(defn visualize-tree
  "Visualize the tree with optional highlighted path"
  [dag-data & [highlighted-path]]
  (render-hierarchical-dag dag-data highlighted-path))

;; Example usage
(comment
  ^:kind/hiccup
  (let [example-dag-data (dag/build-dag-data
                          {"A" ["B" "C"]
                           "B" ["D" "E"]
                           "C" ["F"]}
                          {0 #{"A"}
                           1 #{"B" "C"}
                           2 #{"D" "E" "F"}}
                          {:root-node "A"})]
    (visualize-tree example-dag-data ["A" "B" "D"])))