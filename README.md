# Macquarie University, Department of Computing #

## COMP332 Programming Languages 2018 ##

## A-Star: Path planning using the A\* algorithm in Scala ##

### Introduction ###

This project implements a version of the A\* algorithm for path finding on a 2-D grid of _cells_, written in a functional style in Scala. Our implementation is inspired by the presentation of this algorithm given in the following pages:

*   <http://theory.stanford.edu/~amitp/GameProgramming/AStarComparison.html>
*   <https://www.redblobgames.com/pathfinding/a-star/introduction.html>

The algorithm takes a 2-D map with _obstacles_ placed in specified cells, along with specified _start_ and _target_ cells. It computes a minimum length path from start to target, each step of which is either a horizontal or a vertical move into an adjacent cell that does not contain an obstacle.

The A\* algorithm maintains various collections of cells proceeds from one search step to the next:

*   The _visited_ cells, these are the cells which have been considered (or visited) at some point so far.
*   The _open_ cells, these are visited cells which are queued for further investigation by visiting their neighbours.
*   The _closed_ cells, these are the visited cells that are not currently under further investigation. 
*   The _fringe_ which is the set of all open visited cells.

As the A\* algorithm visits cells it computes a _heuristic_ value for that cell which is expressed as a sum of two values:

*    The _actual cost_ of travelling along the path from start to this cell, and
*    An _estimated cost_ of travelling from this cell to the target. Here the estimate we use is the [_Manhattan or taxicab distance_](https://en.wikipedia.org/wiki/Manhattan_distance) from the cell to the target.

To determine the order in which open cells are analysed, we compute their heuristic values and consider those with the lowest heuristics first. To ensure that A\* actually finds a minimal length path the function to compute these heuristics must satisfy an [_admissibility condition_](https://en.wikipedia.org/wiki/Admissible_heuristic).

It is possible that closed cells should be re-opened if a shorter path is found to that cell in the process of investigating other cells. In the simple scenario implemented here, however, our heuristic function satisfies a stronger [_consistency condition_](https://en.wikipedia.org/wiki/Consistent_heuristic) which ensures that we need never re-open a cell once it has been closed.

The following image, which was generated using the [Doodle image library](https://github.com/underscoreio/doodle), illustrates the state of the A\* algorithm at the end of a process of planning a path.

![planned path](PlannedPath.png "A planned path.")

Here the cells containing obstacles are displayed in *dark slate blue*. The optimal path found is illustrated in *gold*, and it proceeds from the highlighted *red* cell inside the box to the highlighted cell outside of the box. The cells displayed in *light green* are those that had been visited and closed in the process of finding that path. Those in *dark green* are the cells that remain in the fringe heap when the algorithm terminated.

We use the following abstract data-structures to keep track of these sets of cells:

*   [sets](https://en.wikipedia.org/wiki/Set_(abstract_data_type)) to keep track of obstacles and visited cells
*   a [priority queue](https://en.wikipedia.org/wiki/Priority_queue) containing the cells that are open for further analysis in an order prioritised by a _heuristic value_ which is computed as a combination of the distance travelled to get to that cell and an estimate of the distance to travel to the target.

We provide functional implementations of these abstract data structures based upon [2-3 trees](https://en.wikipedia.org/wiki/2-3_tree) and [binomial heaps](https://en.wikipedia.org/wiki/Binomial_heap). These data-structures are [_immutable_](https://en.wikipedia.org/wiki/Immutable_object) and [_persistent_](https://en.wikipedia.org/wiki/Persistent_data_structure).

### Source modules.

*   [BTrees.scala](src/main/scala/BTrees.scala) a functional implementation of [B-trees](https://en.wikipedia.org/wiki/B-tree) or more specifically [2-3 trees](https://en.wikipedia.org/wiki/2-3_tree). This implements different kinds of B-tree nodes using [case classes](https://en.wikipedia.org/wiki/Scala_(programming_language)#Case_classes_and_pattern_matching) and makes extensive use of pattern matching.

*   [BinomialHeaps.scala](src/main/scala/BinomialHeaps.scala) a functional implementation of [Binomial Heaps](https://en.wikipedia.org/wiki/Binomial_heap). The key section of this implementation is the _heap merge_ operation provided by the `BinomialHeap.mergeAux()`. The process of [merging two binomial heaps](https://en.wikipedia.org/wiki/Binomial_heap#Merge) is really just a disguised version of the [addition of binary numbers](https://en.wikipedia.org/wiki/Binary_number#Addition). All of the other binomial heap operations are defined in terms of the merge operation.

*   [AStar.scala](src/main/scala/AStar.scala) this is the core module implementing the [A\* algorithm](https://en.wikipedia.org/wiki/A*_search_algorithm). It largely follows the problem and implementation structure discussed on the following [Red Blob Games page](https://www.redblobgames.com/pathfinding/a-star/introduction.html).

*   [PathPlan.scala](src/main/scala/PathPlan.scala) provides a class to encapsulate the data generated by executing the A\* path planning method. This comprises references to the obstacle map, start and target cells, the planned path as a linked list of _cell visit objects_, the visited set and the fringe set. It also uses this data to generate a [Doodle](https://github.com/underscoreio/doodle) object which renders this data as an image of the kind shown above.

*   [Main.scala](src/main/scala/Main.scala) a driver which creates a path planning problem and solves it using the A\* algorithm provided by [AStar.scala](src/main/scala/AStar.scala). The planned path, open and closed sets are displayed graphically in a window as a Doodle image.

*   [Values.scala](src/main/scala/Values.scala) basic container classes representing [key-value pairs](https://en.wikipedia.org/wiki/Key-value_association) and _prioritised values_.

*   [Utils.scala](src/main/scala/Utils.scala) some miscellaneous utility functions.

To compile an execute this code run the Scala [simple built tool (`sbt`)](https://www.scala-sbt.org/) from the root directory of this project and execute the `run` action to compile the project and run its `main()` method.

---
[Dominic Verity](http://orcid.org/0000-0002-4137-6982)  
Last modified: 13 August 2017  
[Copyright (c) 2018 by Dominic Verity. Macquarie University. All rights reserved.](http://mozilla.org/MPL/2.0/)

