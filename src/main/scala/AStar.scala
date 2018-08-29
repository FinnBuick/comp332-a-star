/*
 * This file is part of COMP332 Assignment 1.
 *
 * Copyright (C) 2018 Dominic Verity, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Implementation of a simple A^* path planning algorithm on a 2-D grid
 * of cells, inspired by the following pages:
 *
 *     http://theory.stanford.edu/~amitp/GameProgramming/AStarComparison.html
 *     https://www.redblobgames.com/pathfinding/a-star/introduction.html
 */

import scala.math.Ordering.Implicits._
import scala.math.{abs, min, max, sqrt}
import PathPlanner._

/**
  * A class to represent a path-finding problem, with methods to solve that
  * problem using the A^* algorithm.
  *
  * Each `PathPlanner` object contains a map of the terrain to search, comprising:
  *    -   `width` and `height` strictly positive (>0) integers giving the number
  *        of rows and columns of cells on the map.
  *    -   `obstacles` a set of coordinates (implemented as a B-tree) of those
  *        cells that contain an obstacle which will prevent the character from
  *        traversing that cell.
  *
  * The `planPath()` method takes start and target locations and uses the A^*
  * algorithm to plan an optimal path between them.
  */
class PathPlanner(val width : Int, val height : Int,
                 val obstacles : ObstacleSet) {

  // Check that the supplied `width` and `height` legal values.
  if (width <= 0 || height <= 0)
    throw new Exception("Dimensions must be strictly positive.")

  /**
    * Method to verify that a given coordinate lies within the bounds of
    * the map.
    */
  def inBounds(loc : Loc) : Boolean =
    (1 <= loc._1) && (loc._1 <= width) && (1 <= loc._2) && (loc._2 <= height)

  /**
    * Method to plan an optimal path from a start location to a target location
    * using the A^* algorithm.
    *
    * Parameters:
    *    -   `start` the coordinates of the cell to start the path at.
    *    -   `target` the coordinates of the target cell to end up at.
    *
    * Returns:
    *     A `PathPlan` object, which comprises:

    *     -   A reference to the `PathPlanner` object representing the map that the
    *         planned path navigates.
    *     -   The coordinates of the start and target cells of the planned path.
    *     -   The visit record of the last step of the computed path, which
    *         ends up at the target location. We can follow the sequence of `last`
    *         pointers from there to find the actual path traversed to get there.
    *     -   The set containing visit records for all of the locations visited
    *         in the search.
    *     -   The fringe heap containing the cells left in the fringe at the end
    *         of the search.
    * 
    * This information is just enough to enable us to draw a picture illustrating the
    * path computed, the set of cells visited and the fringe of cells on the boundary
    * of that visited region.
    */
  def planPath(start : Loc, target : Loc) : PathPlan = {

    // Check that we start at a legal location on the map.
    if (!inBounds(start))
      throw new Exception("Search must start within the boundary of the map!")
    if (obstacles.member(start))
      throw new Exception("Search can't start at a cell containing an obstacle!")

    // Check that the target is a legal location on the map.
    if (!inBounds(target))
      throw new Exception("Target must be within the boundary of the map!")
    if (obstacles.member(target))
      throw new Exception("Target cannot be a cell containing an obstacle!")

    // Setup the visit records of the starting point.
    val visitStart : VisitRecord = VisitTerminus(start)
    val prVisitStart : PrioritisedVisitRecord =
      WithPriority(compute_heuristic(visitStart, target), visitStart)

    // This one is used to search the visited set to see if we have reached
    // the target.
    val visitTarget : VisitRecord = VisitTerminus(target)

    /**
      * A tail recursive worker function which actually implements the A^* algorithm.
      *
      * Parameters:
      *    -   `visited` a set of visit records which contains one entry for each
      *        cell visited so far in this search.
      *    -   `fringe` a binomial heap of prioritised visit records containing those
      *        cells that are in the fringe of the set of visited cells and are
      *        waiting to be processed further.
      *
      * Returns:
      *    -   `None` if no path exists from `start` to `target`
      *    -   `Some` of a `PathPlan` object encapsulating the last visit record,
      *        the final visited set, and the final fringe heap of the planned path.
      *
      * This worker function essentailly operates as follows:
      *     -   Delete the minimum cost visit entry from the fringe.
      *     -   Add appropriately prioritised visit records for the neighbours of
      *         that location to the fringe and visited set.
      *     -   If the fringe is now empty then stop - there is no path from start
      *         to target.
      *     -   If the visited set now contains the target cell then stop - we've
      *         found the path we are looking for so return the required information
      *         about the path found, the visited set and the fringe.
      *     -   Otherwise we aren't done so make a tail recursive call to `worker()`
      *         passing it the new visited set and fringe heap.
      */
    def worker(visited : VisitedSet, fringe : FringeHeap) : PathPlan =
      fringe.deleteMin match {
        case (_, None) => // if we get here there is no path from start to target.
          new PathPlan(this, start, target, visitStart, visited, fringe)
        case (fringe1, Some(prRecord1)) =>
          val record1 : VisitRecord = prRecord1.value
          val loc1 : Loc = record1.loc
          val toVisit : Seq[PrioritisedVisitRecord] =
            for {
              // scan through the cells horizontally / vertically adjacent to the
              // current one
              loc2 <- List((loc1._1 - 1, loc1._2),
                           (loc1._1 + 1, loc1._2),
                           (loc1._1, loc1._2 - 1),
                           (loc1._1, loc1._2 + 1))

              // filter out cells that are out-of-bounds or are obstacles
              if inBounds(loc2) && !obstacles.member(loc2)

              // make a new visit record for the cell to visit
              record2 : VisitRecord = VisitStep(loc2, record1)

              // Our heuristic has the property that it is _consistent_ in the sense
              // discussed in
              //     https://en.wikipedia.org/wiki/Consistent_heuristic
              // so we never need revisit a cell. So we can simply filter out any
              // cell that has already been visited:\

              if !visited.member(record2)

              // However if our heuristic only had the weaker property that it was
              // _admissible_ in the sense discussed in
              //     https://en.wikipedia.org/wiki/Admissible_heuristic
              // then we might find that a previously closed cell can be revisited
              // at a lower cost later in. In that case closed cells might need to
              // be reopened and we must replace the last test with the more elaborate:
              //
              // if (visited.find(record2).map(record2.cost < _.cost).getOrElse(true))

              // compute heuristic for the new visit and make a prioritised record.
              prRecord2 : PrioritisedVisitRecord = WithPriority(
                compute_heuristic(record2, target),
                record2)
            } yield prRecord2

          // Add new visits to the fringe heap and visited set.
          val fringe2 : FringeHeap = (fringe1 /: toVisit) (_ insert _)
          val visited2 : VisitedSet = (visited /: toVisit) (_ replace _.value)

          visited2.find(visitTarget) match {
            case Some(record3) =>
              new PathPlan(this, start, target, record3, visited2, fringe2)
            case None => worker(visited2, fringe2)
          }
      }

    // We compute the path by calling the `worker()` function and giving it a
    // visited set and fringe heap that each contain just the visit record of the
    // starting location.
    if (start equiv target)
      new PathPlan(this, start, target, visitStart, BTree(visitStart), BinomialHeap())
    else
      worker(BTree(visitStart), BinomialHeap(prVisitStart))
  }
}

/**
  * Companion object of the `PathPlanner` class.
  */
object PathPlanner {

  /**
    * Locations on the map are identified by 2-dimensional integer coordinates.
    */
  type Loc = (Int, Int)

  /**
    * A visit record contains:
    *    -   `loc` the coordinate of a location visited,
    *    -   `last` a pointer to the visit that preceeded it if there is one, and
    *    -   `cost` the cost incurred to get to this location.
    */
  abstract class VisitRecord {
    def loc : Loc
    def cost : Double

    /**
      * Method to extract the list of locations (coordinates) of the cells visited
      * upto this point in the path.
      *
      * This has been written in a tail recursive manner, using an auxiliary function,
      * to avoid stack overflow when processing long paths.
      */
    def toPath() : List[Loc] = toPathAux(this, Nil)

  }

  case class VisitTerminus(val loc : Loc) extends VisitRecord {
    val cost = 0
  }

  case class VisitStep(val loc : Loc, val last : VisitRecord)
      extends VisitRecord {
    val cost = last.cost + metric(loc, last.loc)
  }

  private def toPathAux(visit : VisitRecord, result : List[Loc]) : List[Loc] =
    visit match {
      case VisitTerminus(loc) => loc :: result
      case VisitStep(loc, last) => toPathAux(last, loc :: result)
    }

  // Some type aliases.

  /**
    * Visit records are prioritised by a distance measure given as a double
    * numeric value.
    */
  type PrioritisedVisitRecord = WithPriority[Double, VisitRecord]

  /**
    * We keep track of the fringe of cells whose neighbourhoods are yet to be
    * visited using a binomial heap of prioritised visit records.
    */
  type FringeHeap = BinomialHeap[PrioritisedVisitRecord]

  /**
    * We keep track of the cells already visited using a B-tree based set of
    * visit records.
    */
  type VisitedSet = BTree[VisitRecord]

  /**
    * Obstacles on the map, that can't be walked through, are stored as a
    * B-tree set of locations.
    */
  type ObstacleSet = BTree[Loc]

  /**
    * Visit records are ordered by their locations for the purposes of
    * insertion into a B-tree.
    */
  implicit val visitRecordOrdering : Ordering[VisitRecord] =
    new Ordering[VisitRecord] {
      def compare(visit1 : VisitRecord, visit2 : VisitRecord) =
        implicitly[Ordering[Loc]].compare(visit1.loc, visit2.loc)
    }

  /**
    * The distance function used in estimating the heuristic cost of a route.
    *
    * Parameters:
    *    -   `loc1` the start coordinate
    *    -   `loc2` the end coordinate
    * Returns:
    *    -   length of the shortest path from `loc1` to `loc2` in the case
    *        where paths are made up of moves of a single cell either
    *        vertically or horizontally (Manhattan distance)
    */
  def metric(loc1 : Loc, loc2 : Loc) : Double = 
    abs(loc1._1 - loc2._1) + abs(loc1._2 - loc2._2)

  /**
    * Compute a heuristic estimate of the cost of taking a particular step.
    *
    * This is given as a combination of:
    *    -   the cost of the path traversed to this point, and
    *    -   a rough estimate of the cost of travelling from this point to
    *        the target.
    */
  def compute_heuristic(visit : VisitRecord, target : Loc) : Double =
    visit.cost + metric(visit.loc, target)
}

// A few example path finder objects, largely used when debugging things
// from the console in SBT.
object EgPathFind {
  lazy val w1 : List[Loc] = (5 to 20).map((_,5)).toList
  lazy val w2 : List[Loc] = (5 to 20).map((_,20)).toList
  lazy val w3 : List[Loc] = (6 to 19).map((20,_)).toList
  lazy val p1 = new PathPlanner(24, 24, BTree(w1 ++ w2 ++ w3))
  lazy val pp1 = p1.planPath((9,9),(21,11))
}
