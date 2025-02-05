^:kindly/hide-code
(ns index
  (:require
   [scicloj.kindly.v4.api :as kindly]
   [scicloj.kindly.v4.kind :as kind])
  (:import [java.time LocalDate]
           [java.time.format DateTimeFormatter]))

^:kindly/hide-code
(def md (comp kindly/hide-code kind/md))

#_(comment
    "<p align=\"center\" style=\"font-size: 1.5em;\">
**[Please click HERE to view as a webpage](https://adabwana.github.io/sp25-cs5200-hw1/).**
</p>")

(let [formatter (DateTimeFormatter/ofPattern "M/d/yy")
      current-date (str (.format (LocalDate/now) formatter))]
  (md (str "
            
### Jaryt Salvo
            
**Spring 2025 | CS 5200 Artificial Intelligence | Date: " current-date "**

*****

## 1. The architecture of the program and brief explanation of the key methods/functions, and variables. (5 pts.)

This program implements several **Uninformed Search Algorithms** in `Clojure`. The main data structure, defined in `task2.clj`, is a tree representing a **25-node application hierarchy** that starts from 'Operating System'. The tree is stored as a *hash map* where each node points to a list of its children.

The search algorithms are split into separate files for clarity: `bfs.clj` contains **Breadth-First Search**, `dfs.clj` has **Depth-First Search**, `ucs.clj` implements **Uniform Cost Search**, `dls.clj` contains **Depth-Limited Search**, and `ucs_plus.clj` provides an updated version of the graph to show the difference between UCS and BFS. Each search method takes two inputs: the data structure to search, and the target node (with `dls` taking an additional depth parameter).

The code uses *Clojure's built-in data structures* to handle the search process. For example, `bfs` uses a `PersistentQueue` to keep track of nodes to visit, while `dfs` uses recursion to explore paths. Helper functions like `get-node-children` find connected nodes, and `nodes->root` identifies the starting point of the tree.

The program keeps track of its progress using standard Clojure data structures: the `tree` variable stores the node connections, *sets* remember which nodes we've seen, and *vectors* store the paths we find. By using `Clojure`'s approach to handling data, we get clean code that's easy to understand and runs efficiently.

This setup makes it simple to run different search algorithms on the same problem and compare how they work. The code is organized so that each part does one specific job, making it easier to understand how uninformed search strategies work in practice.

## 2. Explanation on how to run the program. (5 pts.)

To run this program, you'll need to install `Docker` and `VSCode` (or fork). Using `Dev Containers` allows you to develop consistently across any operating system (Windows, macOS, or Linux) without worrying about installing dependencies or configuring your local environment. The containerized development environment ensures that all team members work with identical setups, regardless of their host machine.

### Prerequisites
- [`Docker Desktop`](https://docs.docker.com/get-docker/) - Provides containerization
- [`Visual Studio Code`](https://code.visualstudio.com/) - Code editor with Dev Containers support

### Step-by-Step Setup

1. Install `Docker Desktop` and `VSCode` on your system.

2. Install the [`Dev Containers`](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension in VSCode. This enables VSCode to develop inside containers.

3. Clone and open the repository:
   ```bash
   cd Documents/projects  # or your preferred directory
   git clone https://github.com/adabwana/sp25-cs5200-hw4.git
   cd sp25-cs5200-hw4
   code .
   ```

4. When VSCode opens, look for the \"Open in Dev Container\" popup in the bottom right. Alternatively, press `Ctrl + Shift + P`, type \"Dev Containers: Open Folder in Container\", and select the project folder.

5. Choose your development environment:
   - `Python` container: For developing the `Jupyter Notebook` code (did not do this part)
   - `Clojure` container: For developing the `Clojure` code (choose this one for the bulk of the code)

The Dev Container will automatically:

- Set up the correct versions of `Python` and `Clojure`
- Install all necessary packages and dependencies
- Configure the development environment consistently
- Isolate the project environment from your local system

This approach ensures reproducibility and eliminates \"it works on my machine\" issues when collaborating.

### Using the REPL:
1. Once in the `Clojure` container, open a script file you want to run, for example `src/bfs.clj`, and start a `Clojure` REPL with `Calva` using `Ctrl + Shift + P`, type and select `Jack-in`
2. Select `deps.edn` as the project type
3. To evaluate code at your desired level, we use `Ctrl + Enter` for current form, `Alt + Enter` for top-level form, and `Ctrl + Alt + C` then `Enter` for the whole namespace.
4. To render the visualizations in the browser, we use `Clay` with the hotkey `Ctrl + Shift + Space` then `,` to render current form and `Ctrl + Shift + Space` then `n` to render the whole namespace.

## 3. A report on comparison of uninformed search algorithms. (20 pts.)

Our implementation explores four **Uninformed Search Algorithms** on a *25-node application hierarchy*. Each algorithm offers distinct trade-offs in how it explores the search space:

### **Breadth-First Search (BFS)**
**BFS** explores the tree level by level, using a *FIFO queue* to track nodes to visit. In our application hierarchy, **BFS** guarantees finding the *shortest path* in terms of the number of steps from `Operating System` to any target node. For example, finding `Excel-Child1` visits all nodes at each level before going deeper, ensuring we discover the optimal path but potentially using more memory for nodes in the frontier.

### **Uniform-Cost Search (UCS)**
**UCS** extends **BFS** by considering *edge costs* in the search. While our basic tree structure has uniform costs between nodes, the `ucs_plus.clj` implementation demonstrates how **UCS** behaves differently from **BFS** when edges have varying weights. **UCS** ensures finding the *lowest-cost path*, which becomes important when navigating between applications has different computational or resource costs.

### **Depth-First Search (DFS)**
**DFS** takes a different approach, diving deep into one path before backtracking. Using *recursion*, it explores as far as possible along each branch before trying alternatives. When searching for `PowerPoint-Child3`, **DFS** might find a path quickly if it happens to choose the right branch first, but this path isn't guaranteed to be the shortest. **DFS** uses less memory than **BFS** since it only needs to track the current path.

### **Depth-Limited Search (DLS)**
**DLS** adds a practical constraint to **DFS** by limiting the search depth to 3 levels. This is particularly useful in our application hierarchy where most target nodes are within 3-4 levels of the root. For example, searching for `Word` (depth 2) works well, but **DLS** would fail to find `Word-Child1` (depth 4) with a limit of 3. This trade-off makes **DLS** useful when we know our target's approximate depth and want to avoid excessive exploration.

### **Practical Implications:**

- For finding application shortcuts, **BFS** is most reliable as it finds the quickest navigation path
- **UCS** becomes valuable when we add weights to represent resource costs between applications
- When memory is constrained, **DFS** might be preferable despite not guaranteeing shortest paths
- **DLS** is practical for quick searches within known depth bounds, like finding recently used applications

The choice between these algorithms depends on our priorities: *memory usage*, *path optimality*, or *search depth limitations*. In our application hierarchy, **BFS** often provides the most practical balance for user navigation, while the other algorithms offer valuable alternatives for specific scenarios.
")))