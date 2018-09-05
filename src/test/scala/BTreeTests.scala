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

  //pprint() tests

  it should """pretty print the correct string""" in {
    assertResult(
"TwoNode(\n    TwoNode(\n        ThreeNode(\n            EmptyNode(),\n            entry: 1,\n            EmptyNode(),\n            entry: 2,\n            EmptyNode()),\n        entry: 5,\n        TwoNode(\n            EmptyNode(),\n            entry: 7,\n            EmptyNode())),\n    entry: 9,\n    TwoNode(\n        TwoNode(\n            EmptyNode(),\n            entry: 10,\n            EmptyNode()),\n        entry: 11,\n        TwoNode(\n            EmptyNode(),\n            entry: 13,\n            EmptyNode())))") { BTree(5,7,1,9,13,11,10,2).toString }
  }

  it should """pretty print to the string
  "TwoNode(
      EmptyNode(),
      entry: 1,
      EmptyNode())"""" in {
          assertResult("TwoNode(\n    EmptyNode(),\n    entry: 1,\n    EmptyNode())"){
            BTree[Int](1).toString
          }
  }

  //find() tests

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

  //inOrder() Tests

  it should "return List(barely, enough, is, much, sport, too, when) when inOrder is invoked" in {
    assertResult(List("barely", "enough", "is", "much", "sport", "too", "when")) {
      BTree("when", "too", "much", "sport", "is", "barely", "enough").inOrder
    }
  }

  it should "return List(1, 2, 5, 7, 9, 10, 11, 13) when inOrder is invoked" in {
    assertResult(List(1, 2, 5, 7, 9, 10, 11, 13)) {
      BTree(5,7,1,9,13,11,10,2).inOrder
    }
  }

}
