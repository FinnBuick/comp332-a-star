/*
 * This file is part of COMP332 Assignment 1.
 *
 * Copyright (C) 2018 Dominic Verity, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import doodle.core._
import doodle.syntax._
import doodle.jvm.Java2DFrame._
import doodle.backend.StandardInterpreter._

object Main {

  import PathPlanner._

  // Example - plan our way out of a box
  val pathPlan : PathPlan = {
    // Lists containing coordinates of cells making up four walls of a box.
    val w1 : List[Loc] = (5 to 20).map((_,5)).toList
    val w2 : List[Loc] = (5 to 20).map((_,20)).toList
    val w3 : List[Loc] = (6 to 19).map((20,_)).toList
    val w4 : List[Loc] = (6 to 19).map((5,_)).toList
    // Make a B-tree set from those wall cell lists.
    val t1 : BTree[Loc] =  BTree(w1 ++ w2 ++ w3 ++ w4)
    // Delete a single obstacle, so that we can escape the box.
    val t2 = t1 delete (15,20)
    // Make a path planner object containing these obstacles.
    val p1 = new PathPlanner(24, 24, t2)
    // Plan a path from from a cell inside the box to a cell outside the box. 
    p1.planPath((12,12),(21,18))
  }

  def main(args: Array[String]): Unit = {
    pathPlan.mapImage.draw
  }
}
