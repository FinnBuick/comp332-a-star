
# Finneas Buick 44604181

## COMP332 Programming Languages 2018 ##

## Assignment 1 ##

The goal of this assignment was to complete the implementation of an A* path finding visualisation on a 2-D grid of cells using the [Doodle image library](https://github.com/underscoreio/doodle). The assignment required us to complete the implementation of the BTree class in the [BTrees.scala](src/main/scala/BTrees.scala) module, specifically the search method `find`, the pretty print method `pprint`, and the in order traversal method `inOrder`. In addition to this we needed to create test cases for each of these methods. Lastly, we had to implement `PathPlan.mapImage`, the code responsible for rendering the path as a Doodle image found in the [PathPlan.scala](src/main/scala/PathPlan.scala) module.

## `BTrees.find()`

This method uses a simple recursive implementation of BST search, adapted for use in BTrees. The algorithm takes a parameter `v` of type T, which means a generic type, defined when an instance of BTree is created e.g.
>`val tree : BTree[Int] = BTree(1,2,3)`.

Then it uses pattern matching on the current node, starting at the root of the BTree while checking the values/entries in the node to determine which branch to take. For example if the current node is a
>`TwoNode(t1, u1, t2)`

with sub-trees `t1` and `t2` and entry `u1 = 5` and `v = 2`(e.g we are searching for the value 2) then we can use the sorted nature of the BTree to search efficiently by checking that `v < u1` and then using `t1.find(v)` to recurse on the left sub-tree. We know that the left sub-tree either must contain `v` otherwise it does not exist in the BTree. When we reach the node we are looking e.g when `v equiv u1`we return an Option containing u1.
>`Some(u1)`

Using this branching method significantly reduces the amount of nodes that we must visit compared to a typical brute force algorithm where all nodes would visited to find the correct value. Since each node we visit either has the correct value or we need to search only one of its sub-trees, the resulting time complexity is O(log n) instead of O(n) for brute force.

## `BTrees.pprint()`

The `pprint()` method is used to print out the structure of a BTree at the proper indentation with the option to further indent the string using the parameter `tab : Int`. Again here we are using a similar design to `find()`, using pattern matching and recursion as seems common in [Scala](https://www.scala-lang.org/). Starting off with the simple case, if the current node is empty we simply print out the string  "EmptyNode()", prepended with the desired amount of spaces defined by `tab`. Next if we encounter a `TwoNode(t1, u1, t2)` we must print out
>"TwoNode(\n"

 again prepended by `tab` spaces. Then we recurse on the left child `t1` with
 >`t1.pprint(tab + tab_width)`

 here we must pass in the amount of indentation specified by `tab` plus the width of the next level of indentation  given by `tab_width` (defined in `Utils`). Then we append a comma newline with `",\n"` followed by the first entry with
 >`" " * (tab_width + tab) + "entry: " + u1

 prepending the total number of spaces `tab_width + tab` then the string `"entry: "` along with the value `u1`.
 Finally we recurse on the right child `t2`with
 > t2.pprint(tab + tab_width)

 again passing in the correct indentation. Followed by `TwoNode`'s closing parenthesis.

A similar process is followed in the case of a `ThreeNode()`, however ill spare you the details.

## `BTrees.inOrder()`

The `inOrder()` method converts a `BTree` into an ordered list of values. It accomplishes this by performing a standard depth first search on the `BTree`. Starting at the root node, `inOrder()` does a pattern match to determine the sub-type of the current node. Obviously an empty node should be just return an empty list so in this case we return `List[T]()`. In the case of a `TwoNode(t1, u1, t2)` we must append the entry `u1` to the tail end of the list returned by `t1.inOrder()`, this is done using Scala's `:+` operator which appends single elements to a List containing the same type of values. So far we have the left sub-tree in a list with,
> `(t1.inOrder :+ u1)`

but now we must add the right sub-tree's list using the list concatenation operator `:::` to concatenate both lists into a single list that we can then return
>`(t1.inOrder :+ u1) ::: t2.inOrder`

The same process is followed for a `ThreeNode(t1, u1, t2, u2, t3)` however we must append `u2` and concatenate the result with the 3rd sub-tree  `t3` and return the resulting list
>`(((t1.inOrder :+ u1) ::: t2.inOrder) :+ u2) ::: t3.inOrder`
