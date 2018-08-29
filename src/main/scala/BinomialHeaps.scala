/*
 * This file is part of COMP332 Assignment 1.
 *
 * Copyright (C) 2018 Dominic Verity, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * This module implements binomial heaps:
 *
 *     https://en.wikipedia.org/wiki/Binomial_heap
 *
 * Our implementation is _persistent_ and _purely functional_.
 */

import scala.math.Ordering.Implicits._
import Utils._

/**
  * A simple functional implementation of binomial trees.
  *
  * We make the primary constructor of this class `private`, so clients
  * must use one of `apply` methods in the companion object to build heaps
  * that are guaranteed to be legal.
  *
  * The type of values to be stored in the heap must have an ordering.
  * We make use of the assumption that this ordering has the property
  * that for any pair of values `v` and `w` exactly one of `v < w`,
  * `v equiv w` or `w < v` is true.
  *
  * This just wraps a list of binomial tree objects satisfying the invariants:
  *     -   no two trees in the list have the same order, and
  *     -   the trees are sorted from highest to lowest order.
  */
class BinomialHeap[T] private (val entries : List[BinomialTree[T]])
                              (implicit val ord : Ordering[T]) {

  // Field recording the number of entries in this heap.
  val size : Int = entries.map(1 << _.order).sum

  // Import definitions from the companion object.
  import BinomialHeap._

  /**
    * Method to merge this heap with another one. This is essentially a
    * matter of binary addition. Most of the other heap methods are defined
    * in terms of this one.
    *
    * The work is done by an auxiliary method `mergeHeapLists()` in the
    * companion object.
    */
  def merge(other : BinomialHeap[T]) : BinomialHeap[T] =
    new BinomialHeap(mergeHeapLists(entries, other.entries))

  /**
    * Insert a new entry into this heap.
    */
  def insert(v : T) : BinomialHeap[T] =
    new BinomialHeap(mergeHeapLists(entries, List(BinomialTree(v))))

  /**
    * Insert a sequence of new entries into this heap.
    */
  def insertMany(args : T*) : BinomialHeap[T] = (this /: args) (_ insert _)

  /**
    * Returns `true` if the heap is empty and `false` otherwise.
    */
  def isEmpty() = (size == 0)

  /**
    * Find the tree with minimum root value in the heap.
    *
    * If there is more than one tree that with that minimum root value then
    * the one with lowest order is returned.
    */
  def findMinTree() : Option[BinomialTree[T]] =
    (entries :\ (None : Option[BinomialTree[T]])) {
      case (t1, None) => Some(t1)
      case (t1, st2 @ Some(t2)) => if (t1.minimum < t2.minimum) Some(t1) else st2
    }

  /**
    * Remove the tree with a specified order from the heap.
    *
    * Returns the modified heap paired with
    *     -   `None` if no tree was removed or
    *     -   `Some` of the tree that was removed.
    */
  def removeTree(order : Int) : (BinomialHeap[T], Option[BinomialTree[T]]) =
    (entries :\ ((Nil, None) : (List[BinomialTree[T]], Option[BinomialTree[T]]))) {
      case (t1, (trees, None)) if (t1.order == order) => (trees, Some(t1))
      case (t1, (trees, ot2)) => (t1 :: trees, ot2)
    } match {
      case (trees, ot) => (new BinomialHeap(trees), ot)
    }

  /**
    * Delete the minimum entry from the heap.
    *
    * Returns:
    *     -   The empty heap paired with `None` if the original heap was empty.
    *     -   The heap with the minimum entry deleted paired with `Some` of the
    *         value removed.
    */
  def deleteMin() : (BinomialHeap[T], Option[T]) =
    findMinTree().map((t : BinomialTree[T]) => removeTree(t.order)) match {
      case Some((heap, Some(tree))) =>
        (new BinomialHeap(mergeHeapLists(heap.entries, tree.children)),
         Some(tree.minimum))
      case _ => (this, None)
    }

  /**
    * Construct a list containing the entries of a heap by repeatedly
    * deleting the minimum element.
    */
  def inOrder() : List[T] = {
    def worker (heap : BinomialHeap[T], result : List[T]) : List[T] =
      heap.deleteMin() match {
        case (heap, Some(v)) => worker(heap, v :: result)
        case _ => result.reverse
      }
    worker(this, Nil)
  }

  /**
    * Pretty print this binomial heap as a nicely formatted string.
    */
  override def toString() : String = pprint(0)

  def pprint(tab : Int) : String = {
    " " * tab ++ "BinomialHeap(\n" ++
      entries.map(_.pprint(tab + tab_width)).mkString(",\n") ++ ")"
  }
}

/**
  * Companion object for the `BinomialHeap` class. Contains `apply`
  * methods to construct legal heaps.
  */
object BinomialHeap {
  /**
    * Apply method for constructing binomial heaps from a specified list of values
    * by successive insertion.
    *
    * Allows us to write things like:
    *    val t : BinomialHeap[Int] = BinomialHeap()
    * to make an empty binomial heap and
    *    val t : BinomialHeap[Int] = BinomialHeap(5, 2, 9, 6, 10, -1)
    * to make a legal binomial heap containing entries -1, 2, 5, 6, 9, 10.
    */
  def apply[T](args : T*)(implicit ord : Ordering[T]) : BinomialHeap[T] =
    (new BinomialHeap[T](Nil) /: args)(_ insert _)

  /**
    * Merge function, this does the work of the `merge()` method of the
    * companion class.
    *
    * We actually reverse the heaps to be merged so that we can process them
    * from lowest to highest order, just as we would do with the digits of two
    * binary numbers. The result is built up by pushing each constructed tree
    * into a stack (well it is appended to the head of a list) to give a
    * merged heap which is correctly sorted from highest to lowest order.
    *
    * Parameters:
    *     -   `first` and `second` binomial heap lists to merge.
    * Returns:
    *     -   the heap list obtained by correctly merging the `first` and
    *         `second` binomial heap lists.
    */
  private def mergeHeapLists[T] (first : List[BinomialTree[T]],
                                 second : List[BinomialTree[T]])
                            (implicit Order : Ordering[T]) : List[BinomialTree[T]] =
    mergeAux(first.reverse, second.reverse, Nil)

  /**
    * Merge auxiliary method. 
    *
    * Parameters:
    *     -   `first` and `second` are reversed binomial heap lists, that is
    *         they are of binomial trees containing no two trees of the same order
    *         and sorted from lowest to highest order.
    *     -   `result` the merge result built so far, which is a binomial heap list.
    *
    * The `first` and `second` parameters contain the lists of trees to be
    * merged into the `result` list built so far.
    *
    * Invariants:
    *     -   `first.head.order >= result.head.order` (when these lists are
    *         non-empty).
    *     -   `second.head.order >= result.head.order` (when these lists are
    *         non-empty).
    */
  private def mergeAux[T] (first : List[BinomialTree[T]],
                           second : List[BinomialTree[T]],
                           result : List[BinomialTree[T]])
                      (implicit Order : Ordering[T]) : List[BinomialTree[T]] = {

    /**
      * Inner function to add a single tree to the result so far. Its parameter
      * `tree` is a binomial tree to merge into the result-so-far `result`.
      *
      * Invariant `tree.order >= result.head.order` (when `result` is non-empty).
      *
      * We take account of the case `tree.order == result.head.order`,
      * in which these trees must be combined and the resulting tree of the next
      * higher order added as the head of `result.tail`.
      */
    def mergeTree(tree : BinomialTree[T]) : List[BinomialTree[T]] =
      result match {
        case (t :: ts) if (tree.order == t.order) => (BinomialTree(tree, t) :: ts)
        case _ => tree :: result
      }

    (first, second) match {
      case (Nil, Nil) => result
      case (t :: ts, Nil) => mergeAux(ts, Nil, mergeTree(t))
      case (Nil, t :: ts) => mergeAux(Nil, ts, mergeTree(t))
      case (t1 :: ts1, t2 :: ts2) =>
          if (t1.order == t2.order) mergeAux(ts1, ts2, mergeTree(BinomialTree(t1,t2)))
          else if (t1.order < t2.order) mergeAux(ts1, second, mergeTree(t1))
          else /* here t2.order < t1.order */ mergeAux(first, ts2, mergeTree(t2))
    }
  }

  /**
    * Heapsort.
    */
  def heapSort[T](list : List[T])(implicit Order : Ordering[T]) : List[T] =
    (BinomialHeap[T]() /: list)(_ insert _).inOrder
}

/**
  * A simple functional implementation of binomial trees.
  *
  * We make the primary constructor of this class `private`, so clients
  * must use one of `apply` methods in the companion object to build trees
  * that are guaranteed to be legal.
  */
class BinomialTree[T] private (val minimum : T, val order : Int,
                               val children : List[BinomialTree[T]])
                              (implicit val ord : Ordering[T]) {
  /**
    * Pretty print this binomial tree as a nicely formatted string.
    */
  override def toString() : String = pprint(0)

  def pprint(tab : Int) : String =
    " " * tab ++ "BinomialTree(entry: " ++ minimum.toString() ++ (
      if (order != 0)
        children.map(_.pprint(tab + tab_width)).mkString(",\n", ",\n", ")")
      else ")")
}

/**
  * Companion object for the `BinomialTree` class. Contains `apply`
  * methods to construct legal trees from single values and pairs of
  * trees of the same order.
  */
object BinomialTree {
  /**
    * Apply method to make binomial trees containing a single value.
    */
  def apply[T](v : T)(implicit ord : Ordering[T]) : BinomialTree[T] =
    new BinomialTree(v, 0, Nil)

  /**
    * Apply method to make a binomial tree from two binomial trees of the same
    * order.
    *
    * Throws an exception if the trees aren't of the same order.
    */
  def apply[T](t1 : BinomialTree[T], t2 : BinomialTree[T])
           (implicit ord : Ordering[T]) : BinomialTree[T] = {
    if (t1.order != t2.order)
      throw new Exception("To combine binomial trees they must be of the same order!")

    if (t1.minimum <= t2.minimum)
      new BinomialTree(t1.minimum, t1.order + 1, t2 :: t1.children)
    else
      new BinomialTree(t2.minimum, t2.order + 1, t1 :: t2.children)
  }

  /**
    * Unapply method to pattern match a binomial tree of order `n + 1` into
    * two subtrees trees of order `n`.
    */
  def unapply[T](tree : BinomialTree[T])(implicit ord : Ordering[T]) :
      Option[(BinomialTree[T], BinomialTree[T])] =
    tree.children match {
      case (t :: ts) =>
        Some((t, new BinomialTree(tree.minimum, tree.order - 1, ts)))
      case _ => None
    }
}

// A few example binomial heaps / trees, largely used when debugging things
// from the console in SBT.
object EgBinHeaps {
  lazy val t1 : BinomialTree[Int] = BinomialTree(1)
  lazy val t2 : BinomialTree[Int] =
    BinomialTree(BinomialTree(3), BinomialTree(7))
  lazy val t3 : BinomialTree[Int] = BinomialTree(t1, t2)
  lazy val t4 = BinomialTree(t2, BinomialTree(t1, BinomialTree(2)))
  lazy val BinomialTree(t5, t6) = t4

  lazy val h1 : BinomialHeap[Int] =
    BinomialHeap(4, 2, 15, 12, 1, 10, 31, 12, 5, 3, 3, 12, 1, 9, 4)
}
