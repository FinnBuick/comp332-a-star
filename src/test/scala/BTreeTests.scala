/*
* This file is part of COMP332 Assignment 1.
*
* Copyright (C) 2018 Dominic Verity, Macquarie University.
*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/.
*
* Tests of the B-tree implementation. Uses the ScalaTest `FlatSpec` style
* for writing tests. See
*
*      http://www.scalatest.org/user_guide
*
* For more info on writing ScalaTest tests.
*/

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class BTreeSpec extends FlatSpec with Matchers {

  import BTree._

  "An empty B-tree:" should "have size 0" in {
    assert (BTree[Int]().size == 0)
  }

  it should "have depth 0" in {
    assertResult(0) {
      BTree[Int]().depth
    }
  }

  it should "return `None` when `find` is invoked" in {
    assertResult(None) {
      BTree[String]().find("Hello")
    }
  }

  it should "return the empty B-tree when `delete` is invoked" in {
    assertResult(EmptyNode[Int]()) {
      BTree[Int]().delete(10)
    }
  }

  it should "return a singleton B-tree when `insert` is invoked" in {
    assertResult(TwoNode(EmptyNode[String](), "Hello", EmptyNode[String]())) {
      BTree[String]().insert("Hello")
    }
  }

  it should "return the empty list when `inOrder` is invoked" in {
    assertResult(Nil) {
      BTree[String]().inOrder
    }
  }

  it should "pretty print to the string \"EmptyNode()\"" in {
    assertResult("EmptyNode()") {
      BTree[(Int, Int)]().toString
    }
  }

  // My Tests

  it should """pretty print to the string "TwoNode(
    TwoNode(
        ThreeNode(
            EmptyNode(),
            entry: 1,
            EmptyNode(),
            entry: 2,
            EmptyNode()),
        entry: 5,
        TwoNode(
            EmptyNode(),
            entry: 7,
            EmptyNode())),
    entry: 9,
    TwoNode(
        TwoNode(
            EmptyNode(),
            entry: 10,
            EmptyNode()),
        entry: 11,
        TwoNode(
            EmptyNode(),
            entry: 13,
            EmptyNode())))\"""" in {
    assertResult("""TwoNode(
    TwoNode(
        ThreeNode(
            EmptyNode(),
            entry: 1,
            EmptyNode(),
            entry: 2,
            EmptyNode()),
        entry: 5,
        TwoNode(
            EmptyNode(),
            entry: 7,
            EmptyNode())),
    entry: 9,
    TwoNode(
        TwoNode(
            EmptyNode(),
            entry: 10,
            EmptyNode()),
        entry: 11,
        TwoNode(
            EmptyNode(),
            entry: 13,
            EmptyNode())))""") {
      BTree(5,7,1,9,13,11,10,2).toString
    }
  }

  it should "return Some(\"Hello\") when `find` is invoked" in {
    assertResult(Some("Hello")) {
      BTree[String]("Hello", "world!").find("Hello")
    }
  }

  it should "return Some(\"trops\") when `find` is invoked" in {
    assertResult(Some("trops")) {
      BTree[String]("when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "trops", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough",
                    "when", "too", "much", "sport", "is", "barely", "enough")
      .find("trops")
    }
  }

}
