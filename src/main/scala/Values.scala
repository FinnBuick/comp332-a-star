/*
 * This file is part of COMP332 Assignment 1.
 *
 * Copyright (C) 2018 Dominic Verity, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Classes to represent values tagged with priorities or keys.
 */

/**
  * A class to represent values that are tagged with a priority.
  *
  * Parameters:
  *     -   `priority` a priority tag, whose type `P` has an ordering given as
  *         an implicitly supplied object of type `Ordering[P]`, and
  *     -   `value` the value to be tagged with a priority.
  */
case class WithPriority[P, V](val priority : P, val value : V)
                             (implicit val ord : Ordering[P])

/**
  * Companion object for the `WithPriority` class.
  */
object WithPriority {
  /**
    * This implicit provides an ordering under which `WithPriority` objects
    * are ordered by their priorities.
    */
  implicit def withPriorityOrdering[P,V] (implicit ord : Ordering[P]) :
      Ordering[WithPriority[P,V]] =
    new Ordering[WithPriority[P,V]] {
      def compare(x : WithPriority[P,V], y : WithPriority[P,V]) =
        ord.compare(x.priority, y.priority)
    }
}

/**
  * Classes to represent key-value pairs. These can be used to construct
  * key-value maps using B-trees.
  */
abstract class MapEntry[K, V](val ord : Ordering[K]) {
  def key : K
}

case class Key[K, V](val key : K)
              (implicit ord : Ordering[K]) extends MapEntry[K,V](ord)

case class KeyValue[K, V](val key : K, val value : V)
                   (implicit ord : Ordering[K]) extends MapEntry[K,V](ord)

/**
  * Companion object for the `MapEntry` class.
  */
object MapEntry {
  /**
    * This implicit provides an ordering under which `MapEntry` objects
    * are ordered by their keys alone.
    */
  implicit def withKeyOrdering[K,V](implicit ord : Ordering[K]) :
      Ordering[MapEntry[K,V]] =
    new Ordering[MapEntry[K,V]] {
      def compare(x : MapEntry[K,V], y : MapEntry[K,V]) = ord.compare(x.key, y.key)
    }
}
