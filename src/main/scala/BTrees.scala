/*
 * This file is part of COMP332 Assignment 1.
 *
 * Copyright (C) 2018 Dominic Verity, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * A module implementing the B-tree data structure:
 *
 *     https://en.wikipedia.org/wiki/B-tree
 *
 * In particular we provide the 2-3 tree variant:
 *
 *     https://en.wikipedia.org/wiki/2-3_tree
 *
 * Our implementation is _persistent_ and _purely functional_.
 */

import scala.math.Ordering.Implicits._
import scala.math.{max}
import Utils._

/**
  * Base class for all nodes. These include nodes with 1 and 4 children
  * which cannot occur in a legal B-tree.
  */
abstract sealed class Node[T](implicit val ord : Ordering[T])

/**
  * A simple functional (persistent) implementation of 2-3 trees.
  *
  * The type of values to be stored in the tree must have an ordering.
  * We make use of the assumption that this ordering has the property
  * that for any pair of values `v` and `w` exactly one of `v < w`,
  * `v equiv w` or `w < v` is true.
  */
abstract sealed class BTree[T](implicit ord : Ordering[T])
    extends Node[T] {
  // Import definitions from the companion object.
  import BTree._

  // Number of entries stored in this B-tree.
  def size : Int
  // Depth of this B-tree.
  def depth : Int

  /**
    * Insert a new value into this tree.
    *
    * If the tree already contains an entry whose value is equivalent to `v`
    * is then the tree is returned unchanged.
    */
  def insert(v : T) : BTree[T] =
    insertAux(v, false).map(treeToBTree(_)).getOrElse(this)

  /**
    * Replace a value in this tree.
    *
    * If the tree doesn't already contain an entry whose value is equivalent
    * to `v` in the specified ordering then insert `v` as a new entry, otherwise
    * replace the equivalent entry by `v`.
    *
    * For example this is useful when the values stored in a B-tree are key-value
    * pairs ordered by key alone. Then this method can be used to replace the
    * value associated with a key with another value.
    */
  def replace(v : T) : BTree[T] =
    insertAux(v, true).map(treeToBTree(_)).getOrElse(this)

  /**
    * Delete an element from this tree.
    *
    * If an element equal to `v` is not found in the tree then that tree is
    * returned unchanged.
    */
  def delete(v : T) : BTree[T] =
    deleteAux(v).map(treeToBTree(_)).getOrElse(this)

  /**
    * Insert multiple values into the tree.
    */
  def insertMany(args : T*) = (this /: args)(_ insert _)

  /**
    * Replace multiple values into the tree.
    */
  def replaceMany(args : T*) = (this /: args)(_ replace _)

  /**
    * Delete multiple values from the tree.
    */
  def deleteMany(args : T*) = (this /: args)(_ delete _)


  /**
    * Auxiliary method implementing both variants of the insert method.
    *
    * Parameters:
    *    -   `v` the element to insert (or replace) into the tree.
    *    -   `rep` replacement flag, suppose that  the tree contains an entry
    *         equivalent `v` then if this flag is `true` replace that entry
    *         otherwise leave the tree unchanged.
    *
    * Returns:
    *    -   `None` in the event that the tree isn't changed.
    *    -   `Some` of a tree with the new element inserted and of the same depth
    *        as the original tree; this might an object of type `FourNode` as its
    *        root node.
    *
    */
  private def insertAux(v : T, rep : Boolean) : Option[Node[T]] =
    this match {
      case EmptyNode() => Some(TwoNode(EmptyNode(), v, EmptyNode()))

      case TwoNode(t1, u1, t2) if (u1 equiv v) =>
        if (rep) Some(TwoNode(t1, v, t2)) else None

      case TwoNode(EmptyNode(), u1, EmptyNode()) =>
        if (v < u1)
          Some(ThreeNode(EmptyNode(), v, EmptyNode(), u1, EmptyNode()))
        else /* here u1 < v */
          Some(ThreeNode(EmptyNode(), u1, EmptyNode(), v, EmptyNode()))

      case TwoNode(t1, u1, t2) =>
          if (v < u1)
            t1.insertAux(v, rep).map(buildTwoLeftIns(_, u1, t2))
          else /* here u1 < v */
            t2.insertAux(v,rep).map(buildTwoRightIns(t1, u1, _))

      case ThreeNode(t1, u1, t2, u2, t3) if (u1 equiv v) =>
        if (rep) Some(ThreeNode(t1, v, t2, u2, t3)) else None
      case ThreeNode(t1, u1, t2, u2, t3) if (u2 equiv v) =>
        if (rep) Some(ThreeNode(t1, u1, t2, v, t3)) else None

      case ThreeNode(EmptyNode(), u1, EmptyNode(), u2, EmptyNode()) =>
        if (v < u1)
          Some(FourNode(EmptyNode(), v, EmptyNode(), u1, EmptyNode(), u2, EmptyNode()))
        else if (u2 < v)
          Some(FourNode(EmptyNode(), u1, EmptyNode(), u2, EmptyNode(), v, EmptyNode()))
        else /* here u1 < v && v < u2 */
          Some(FourNode(EmptyNode(), u1, EmptyNode(), v, EmptyNode(), u2, EmptyNode()))

      case ThreeNode(t1, u1, t2, u2, t3) =>
        if (v < u1)
          t1.insertAux(v, rep).map(buildThreeLeftIns(_, u1, t2, u2, t3))
        else if (u2 < v)
          t3.insertAux(v, rep).map(buildThreeRightIns(t1, u1, t2, u2, _))
        else /* here u1 < v && v < u2 */
          t2.insertAux(v, rep).map(buildThreeMidIns(t1, u1, _, u2, t3))

      case _ =>
        assert(false, "Insertion failure, I shouldn't be here!")
        None
    }

  /**
    * Auxiliary method implementing the delete method.
    *
    * Parameters:
    *    -   `v` the value to delete from the tree.
    *
    * Returns:``
    *    -   `None` in the event that an element equivalent to `v` isn't found in
    *         the tree, otherwise
    *    -   `Some` of a tree with the and of depth one more than the original tree;
    *        this might have root node of type `OneNode`.
    *
    */
  private def deleteAux(v : T) : Option[Node[T]] =
    this match {
      case EmptyNode() => None

      case TwoNode(EmptyNode(), u1, EmptyNode()) if (v equiv u1) =>
        Some(OneNode(EmptyNode()))

      case TwoNode(t1, u1, t2) if (v equiv u1) =>
        t2.deleteLeft().map{case (t2, u1) => buildTwoRightDel(t1, u1, t2)}

      case TwoNode(t1, u1, t2) if (v < u1) =>
        t1.deleteAux(v).map(buildTwoLeftDel(_, u1, t2))
      case TwoNode(t1, u1, t2) if (u1 < v) =>
        t2.deleteAux(v).map(buildTwoRightDel(t1, u1, _))

      case ThreeNode(EmptyNode(), u1, EmptyNode(), u2, EmptyNode()) if (v equiv u1) =>
        Some(TwoNode(EmptyNode(), u2, EmptyNode()))
      case ThreeNode(EmptyNode(), u1, EmptyNode(), u2, EmptyNode()) if (v equiv u2) =>
        Some(TwoNode(EmptyNode(), u1, EmptyNode()))

      case ThreeNode(t1, u1, t2, u2, t3) if (v equiv u1) =>
        t2.deleteLeft().map{case (t2, u1) => buildThreeMidDel(t1, u1, t2, u2, t3)}
      case ThreeNode(t1, u1, t2, u2, t3) if (v equiv u2) =>
        t3.deleteLeft().map{case (t3, u2) => buildThreeRightDel(t1, u1, t2, u2, t3)}

      case ThreeNode(t1, u1, t2, u2, t3) if (v < u1) =>
        t1.deleteAux(v).map(buildThreeLeftDel(_, u1, t2, u2, t3))
      case ThreeNode(t1, u1, t2, u2, t3) if (u1 < v  && v < u2) =>
        t2.deleteAux(v).map(buildThreeMidDel(t1, u1, _, u2, t3))
      case ThreeNode(t1, u1, t2, u2, t3) if (u2 < v) =>
        t3.deleteAux(v).map(buildThreeRightDel(t1, u1, t2, u2, _))

      // If the tree we started with satisfies the B-tree invariants then we
      // should never end up here. So for debugging purposes we've added a
      // call to `assert()` here. This raises an exception if it is executed
      // and we can use the resulting stack trace to track down where our code
      // is incorrect.

      // We use `assert()` bacause once we've debugged our code we can set the
      // compiler flag `-Xdisable-assertions` (in the `build.sbt` file) and then
      // the compiler will ignore these calls to `assert()` completely. That is,
      // with that flag set no code whatsoever will be generated for these calls.

      case _ =>
        assert(false, "Deletion failure, I shouldn't be here!")
        None
    }

  // Recurse to left most child and delete that. Preserve the value deleted by
  // passing it up the tree.
  protected def deleteLeft() : Option[(Node[T], T)] =
    this match {
      case EmptyNode() => None

      case TwoNode(EmptyNode(), u1, EmptyNode()) =>
        Some((OneNode(EmptyNode()), u1))
      case TwoNode(t1, u1, t2) =>
        t1.deleteLeft().map{case (t1, v) => (buildTwoLeftDel(t1, u1, t2), v)}

      case ThreeNode(EmptyNode(),u1, EmptyNode(), u2, EmptyNode()) =>
        Some((TwoNode(EmptyNode(), u2, EmptyNode()), u1))
      case ThreeNode(t1, u1, t2, u2, t3) =>
        t1.deleteLeft().map{case (t1, v) => (buildThreeLeftDel(t1, u1, t2, u2, t3), v)}

      case _ =>
        assert(false, "Deletion failure, I shouldn't be here!")
        None
    }

  /**
    * Find an entry in the B-tree. Implemented using a straightforward variant of
    * binary tree searching:
    *
    *     https://en.wikipedia.org/wiki/Binary_search_tree#Searching
    *
    *  Returns:
    *     -   `Some` of an entry found in the tree that is `equiv`
    *         to the parameter value `v`.
    *     -   `None` if no such element is found in the tree.
    *
    *  Note: the element returned and `v` are equivalent values but they may
    *  not be identical. For example, suppose that the values stored in the
    *  tree are key-value pairs `(k, v)` ordered by the ordering on keys alone.
    *  then two such values will be `equiv` if they have the same keys, but they
    *  may have different values. This allows us to use our B-trees to implement
    *  maps which associate keys with values.
    */
  def find(v : T) : Option[T] =
    this match {
      case EmptyNode() => None
      case TwoNode(t1, u1, t2) if (v equiv u1) => Some(u1)
      case TwoNode(t1, u1, t2) if (v < u1) => t1.find(v)
      case TwoNode(t1, u1, t2) if (v > u1) => t2.find(v)
      case ThreeNode(t1, u1, t2, u2, t3) if (v < u1) => t1.find(v)
      case ThreeNode(t1, u1, t2, u2, t3) if (v equiv u1) => Some(u1)
      case ThreeNode(t1, u1, t2, u2, t3) if (v > u1 && v < u2) => t2.find(v)
      case ThreeNode(t1, u1, t2, u2, t3) if (v equiv u2) => Some(u2)
      case ThreeNode(t1, u1, t2, u2, t3) if (v < u2) => t3.find(v)
      case _ => None
    }


  /**
    * Test to see if an element is in this tree.
    *
    * Returns `true` if `v` is `equiv` to an element in the tree and
    * `false` otherwise.
    */
  def member(v : T) : Boolean = find(v).nonEmpty

  /**
    * Render B-trees to strings using the pretty printer method.
    */
  override def toString() : String = pprint(0)

  /**
    * Pretty print this B-tree as string in which each level of the tree is
    * indented relative to the last one.
    *
    * Parameters:
    *    -   `tab` the number of spaces to indent each line by.
    */
  def pprint(tab : Int) : String = {
    this match {
      case EmptyNode() => " " * tab + "EmptyNode()"
      case TwoNode(t1, u1, t2) => (" " * tab +"TwoNode(\n"
                                          + t1.pprint(tab + tab_width) + ",\n"
                                          + " " * (tab_width + tab) + "entry: " + u1 + ",\n"
                                          + t2.pprint(tab + tab_width) + ")")
      case ThreeNode(t1, u1, t2, u2, t3) => (" " * tab + "ThreeNode(\n"
                                          + t1.pprint(tab + tab_width) + ",\n"
                                          + " " * (tab_width + tab) + "entry: " + u1 + ",\n"
                                          + t2.pprint(tab + tab_width) + ",\n"
                                          + " " * (tab_width + tab) + "entry: " + u2 + ",\n"
                                          + t3.pprint(tab + tab_width) + ")")
    }
  }

  /**
    * Convert the tree to an ordered list of values by making a depth first,
    * left to right traversal. Implemented using a straightforward variant of the
    * in order traversal discussed in:
    *
    *    https://en.wikipedia.org/wiki/In-order_traversal#In-order_(LNR)
    *
    * Returns:
    *    -   A list containing all of the entries of the tree sorted in order from
    *        smallest to largest.
    */
  def inOrder() : List[T] = {
    this match {
      case EmptyNode() => List[T]()
      case TwoNode(t1, u1, t2) => (t1.inOrder :+ u1) ::: t2.inOrder
      case ThreeNode(t1, u1, t2, u2, t3) => (((t1.inOrder :+ u1) ::: t2.inOrder) :+ u2) ::: t3.inOrder
    }
  }
}
/**
  * Companion object for the `BTree` class. Contains the case classes
  * implementing different kinds of BTree nodes and some utility
  * functions for building and validating such trees.
  */
object BTree {

  // BTree node case classes.

  // We should make the following tree node case classes private so that we can
  // only construct B-trees in the `BTree` object / class. Then if we can show
  // that the methods of the `BTree[T]` class preserve the B-tree invariants we
  // will be sure that any trees constructed in client code must necessarily
  // satisfy those invariants.

  // But we haven't made this private to allow you to easily write tests of your
  // code as you are writing it.

  /**
    * Empty B-tree node.
    */
  final case class EmptyNode[T]()(implicit ord : Ordering[T])
      extends BTree[T] {
    val size : Int = 0
    val depth : Int = 0
  }

  /**
    * A node with one child.
    *
    * These don't occur in trees that satisfy the B-tree invariants but they
    * are useful for keeping track of things in the delete method.
    */
  final case class OneNode[T](t : BTree[T])(implicit ord : Ordering[T])
      extends Node[T] {
    val size : Int = t.size
    val depth : Int = t.depth + 1
  }

  /**
    * A B-tree node with two children.
    */
  final case class TwoNode[T](t1 : BTree[T], u1 : T, t2 : BTree[T])
                          (implicit ord : Ordering[T]) extends BTree[T] {
    val size : Int = t1.size + t2.size + 1
    val depth : Int = max(t1.depth, t2.depth) + 1
  }

  /**
    * A B-tree node with three children.
    */
  final case class ThreeNode[T](t1 : BTree[T], u1 : T,
                                t2 : BTree[T], u2 : T, t3 : BTree[T])
                            (implicit ord : Ordering[T]) extends BTree[T] {
    val size : Int = t1.size + t2.size + t3.size + 2
    val depth : Int = max(t1.depth, max(t2.depth, t3.depth)) + 1
  }

  /**
    * A node with four children.
    *
    * These don't occur in trees that satisfy the B-tree invariants but they
    * are useful for keeping track of things in the insert method.
    */
  final case class FourNode[T](t1 : BTree[T], u1 : T,
                               t2 : BTree[T], u2 : T,
                               t3 : BTree[T], u3 : T, t4 : BTree[T])
                           (implicit ord : Ordering[T]) extends Node[T] {
    val size : Int = t1.size + t2.size + t3.size + t4.size + 3
    val depth : Int = max(t1.depth, max(t2.depth, max(t3.depth, t4.depth))) + 1
  }

  /**
    * Make a B-tree from a tree that might contain 1-nodes or 4-nodes.
    *
    * All tree nodes have children of classes that extend `BTree`, this means
    * that `OneNode` and `FourNode` objects may only occur as root nodes. So
    * this function only needs to alter the root node as follows:
    *    -   If it is a `OneNode` then drop it, making the tree one level shallower.
    *    -   If it is a `FourNode` the replace it by a tree of 2-nodes of depth 2,
    *        therefore making the tree one level deeper.
    */
  def treeToBTree[T](t : Node[T])(implicit ord : Ordering[T]) : BTree[T] =
    t match {
      case OneNode(t) => t
      case FourNode(t1, u1, t2, u2, t3, u3, t4) =>
        TwoNode(TwoNode(t1, u1, t2), u2, TwoNode(t3, u3, t4))
      case t : BTree[T] => t
    }


  // Utility methods (smart constructors) to build nodes in a way that propagates
  // `OneNode` instances up the tree. These are used to construct nodes in the delete
  // method.

  private def buildTwoLeftDel[T](t1 : Node[T], u1 : T, t2 : BTree[T])
                             (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2) match {
      case (OneNode(t1), u1, TwoNode(t2, u2, t3)) =>
        OneNode(ThreeNode(t1, u1, t2, u2, t3))
      case (OneNode(t1), u1, ThreeNode(t2, u2, t3, u3, t4)) =>
        TwoNode(TwoNode(t1, u1, t2), u2, TwoNode(t3, u3, t4))
      case (t1 : BTree[T], u1, t2) => TwoNode(t1, u1, t2)
      case _ =>
        assert(false, "Delete failure, I shouldn't be here!")
        EmptyNode()
    }

  private def buildTwoRightDel[T](t1 : BTree[T], u1 : T, t2 : Node[T])
                              (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2) match {
      case (TwoNode(t1, u1, t2), u2, OneNode(t3)) =>
        OneNode(ThreeNode(t1, u1, t2, u2, t3))
      case (ThreeNode(t1, u1, t2, u2, t3), u3, OneNode(t4)) =>
        TwoNode(TwoNode(t1, u1, t2), u2, TwoNode(t3, u3, t4))
      case (t1, u1, t2 : BTree[T]) => TwoNode(t1, u1, t2)
      case _ =>
        assert(false, "Delete failure, I shouldn't be here!")
        EmptyNode()
    }

  private def buildThreeLeftDel[T](t1 : Node[T], u1 : T,
                                   t2 : BTree[T], u2 : T, t3 : BTree[T])
                               (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2, u2, t3) match {
      case (OneNode(t1), u1, TwoNode(t2, u2, t3), u3, t4) =>
        TwoNode(ThreeNode(t1, u1, t2, u2, t3), u3, t4)
      case (OneNode(t1), u1, ThreeNode(t2, u2, t3, u3, t4), u4, t5) =>
        ThreeNode(TwoNode(t1, u1, t2), u2, TwoNode(t3, u3, t4), u4, t5)
      case (t1 : BTree[T], u1, t2, u2, t3) => ThreeNode(t1, u1, t2, u2, t3)
      case _ =>
        assert(false, "Delete failure, I shouldn't be here!")
        EmptyNode()
    }

  private def buildThreeMidDel[T](t1 : BTree[T], u1 : T,
                                  t2 : Node[T], u2 : T, t3 : BTree[T])
                              (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2, u2, t3) match {
      case (t1, u1, OneNode(t2), u2, TwoNode(t3, u3, t4)) =>
        TwoNode(t1, u1, ThreeNode(t2, u2, t3, u3, t4))
      case (t1, u1, OneNode(t2), u2, ThreeNode(t3, u3, t4, u4, t5)) =>
        ThreeNode(t1, u1, TwoNode(t2, u2, t3), u3, TwoNode(t4, u4, t5))
      case (t1, u1, t2 : BTree[T], u2, t3) => ThreeNode(t1, u1, t2, u2, t3)
      case _ =>
        assert(false, "Delete failure, I shouldn't be here!")
        EmptyNode()
    }

  private def buildThreeRightDel[T](t1 : BTree[T], u1 : T,
                                    t2 : BTree[T], u2 : T, t3 : Node[T])
                                (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2, u2, t3) match {
      case (t1, u1, TwoNode(t2, u2, t3), u3, OneNode(t4)) =>
        TwoNode(t1, u1, ThreeNode(t2, u2, t3, u3, t4))
      case (t1, u1, ThreeNode(t2, u2, t3, u3, t4), u4, OneNode(t5)) =>
        ThreeNode(t1, u1, TwoNode(t2, u2, t3), u3, TwoNode(t4, u4, t5))
      case (t1, u1, t2, u2, t3 : BTree[T]) => ThreeNode(t1, u1, t2, u2, t3)
      case _ =>
        assert(false, "Delete failure, I shouldn't be here!")
        EmptyNode()
    }

  // Utility methods (smart constructors) to build nodes in a way that propagates
  // `FourNode` instances up the tree. These are used to construct nodes in the
  // insert method.

  private def buildTwoLeftIns[T](t1 : Node[T], u1 : T, t2 : BTree[T])
                             (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2) match {
      case (FourNode(t1, u1, t2, u2, t3, u3, t4), u4, t5) =>
        ThreeNode(TwoNode(t1, u1, t2), u2, TwoNode(t3, u3, t4), u4, t5)
      case (t1 : BTree[T], u1, t2) => TwoNode(t1, u1, t2)
      case _ =>
        assert(false, "Insert failure, I shouldn't be here!")
        EmptyNode()
    }

  private def buildTwoRightIns[T](t1 : BTree[T], u1 : T, t2 : Node[T])
                              (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2) match {
      case (t1, u1, FourNode(t2, u2, t3, u3, t4, u4, t5)) =>
        ThreeNode(t1, u1, TwoNode(t2, u2, t3), u3, TwoNode(t4, u4, t5))
      case (t1, u1, t2 : BTree[T]) => TwoNode(t1, u1, t2)
      case _ =>
        assert(false, "Insert failure, I shouldn't be here!")
        EmptyNode()
    }

  private def buildThreeLeftIns[T](t1 : Node[T], u1 : T,
                                   t2 : BTree[T], u2 : T, t3 : BTree[T])
                               (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2, u2, t3) match {
      case (FourNode(t1, u1, t2, u2, t3, u3, t4), u4, t5, u5, t6) =>
        FourNode(TwoNode(t1, u1, t2), u2, TwoNode(t3, u3, t4), u4, t5, u5, t6)
      case (t1 : BTree[T], u1, t2, u2, t3) => ThreeNode(t1, u1, t2, u2, t3)
      case _ =>
        assert(false, "Insert failure, I shouldn't be here!")
        EmptyNode()
    }

  private def buildThreeMidIns[T](t1 : BTree[T], u1 : T,
                                  t2 : Node[T], u2 : T, t3 : BTree[T])
                              (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2, u2, t3) match {
      case (t1, u1, FourNode(t2, u2, t3, u3, t4, u4, t5), u5, t6) =>
        FourNode(t1, u1, TwoNode(t2, u2, t3), u3, TwoNode(t4, u4, t5), u5, t6)
      case (t1, u1, t2 : BTree[T], u2, t3) => ThreeNode(t1, u1, t2, u2, t3)
      case _ =>
        assert(false, "Insert failure, I shouldn't be here!")
        EmptyNode()
    }

  private def buildThreeRightIns[T](t1 : BTree[T], u1 : T,
                                    t2 : BTree[T], u2 : T, t3 : Node[T])
                                (implicit ord : Ordering[T]) : Node[T] =
    (t1, u1, t2, u2, t3) match {
      case (t1, u1, t2, u2, FourNode(t3, u3, t4, u4, t5, u5, t6)) =>
        FourNode(t1, u1, t2, u2, TwoNode(t3, u3, t4), u4, TwoNode(t5, u5, t6))
      case (t1, u1, t2, u2, t3 : BTree[T]) => ThreeNode(t1, u1, t2, u2, t3)
      case _ =>
        assert(false, "Insert failure, I shouldn't be here!")
        EmptyNode()
    }

  /**
    * Apply method for constructing BTrees from a specified list of values
    * by successive insertion.
    *
    * Allows us to write things like:
    *    val t : BTree[Int] = BTree()
    * to make an empty B-tree and
    *    val t : BTree[Int] = BTree(5, 2, 9, 6, 10, -1)
    * to make a legal B-tree containing entries -1, 2, 5, 6, 9, 10 in order.
    */
  def apply[T](args : T*)(implicit ord : Ordering[T]) : BTree[T] =
    ((EmptyNode[T]() : BTree[T])  /: args)(_ insert _)

  def apply[T](list : List[T])(implicit ord : Ordering[T]) : BTree[T] =
    ((EmptyNode[T]() : BTree[T])  /: list)(_ insert _)

  // The following methods test to see whether a tree satisfies the B-tree
  // invariants. Only used for testing purposes, since these invariants should be
  // preserved by all tree operations.

  /**
    *  Test to make sure that all branches of a B-tree have the same depth.
    */
  def depthOk[T](t : BTree[T]) : Boolean =
    t match {
      case EmptyNode() => true
      case TwoNode(t1, _, t2) =>
        (t1.depth == t2.depth) && depthOk(t1) && depthOk(t2)
      case ThreeNode(t1, _, t2, _, t3) =>
        (t1.depth == t2.depth) && (t2.depth == t3.depth) &&
          depthOk(t1) && depthOk(t2) && depthOk(t3)
    }
}

// A few example B-trees, largely used when debugging things from the
// console in SBT.

object EgBTrees {
  lazy val t1 : BTree[Int] =
    BTree(9,6,23,5,9,6,2,1,11,15,14,3,7,12,-1,13)
  lazy val t2 : BTree[String] =
    BTree("COMP332", "a", "unit", "in", "which", "too", "many",
          "trees", "are", "barely", "enough!")
  lazy val t3 : BTree[Int] =
    t1.insertMany(21,43,-5,28,-2,12,29,22,31,30)
  lazy val t4 : BTree[Int] =
    t3.deleteMany(4,2,7,9,28,-2,-5,31,30)
  lazy val t5 : BTree[Int] = (t4 /: (1 to 43)) (_ delete _)
}
