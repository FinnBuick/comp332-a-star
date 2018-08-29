/*
 * This file is part of COMP332 Assignment 1.
 *
 * Copyright (C) 2018 Dominic Verity, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/**
  * Some utility functions used in various places.
  */
object Utils {
  /**
    * Various parameters controlling things like tab width in pretty printers.
    */
  val tab_width = 4

  /**
    * Enables us to use the ideom:
    *     `myBool.toOption(someResult)`
    * which returns `None` if `myBool` is `false` and `Some(someResult)` if
    * it is `true`. Taken from the following discussion:
    *     https://stackoverflow.com/questions/19690531/scala-boolean-to-option
    */
  implicit class BoolToOption(val self: Boolean) extends AnyVal {
    def toOption[A](value: => A): Option[A] = if (self) Some(value) else None
  }

  /**
    * Reverse one list onto the front of another.
    */
  def shunt[T](ls1 : List[T], ls2 : List[T]) : List[T] =
    ls1 match {
      case Nil => ls2
      case (v1 :: ls1) => shunt(ls1, v1 :: ls2)
    }
}
