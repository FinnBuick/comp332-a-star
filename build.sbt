/*
 * This file is part of COMP332 Assignment 1.
 *
 * Copyright (C) 2018 Dominic Verity, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

ThisBuild/organization := "comp.mq.edu.au"
ThisBuild/scalaVersion := "2.12.6"
ThisBuild/scalacOptions :=
    Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-Xcheckinit",
//        "-Xfatal-warnings",
//        "-Xlint:-stars-align,_"
    )
ThisBuild/resolvers += Resolver.bintrayRepo("underscoreio", "training")

// Settings for the assignment 1 project.
lazy val assignment1 = (project in file("."))
  .settings(
    // Project information
    name := "Comp332 Assignment1",
    version := "0.1",

    // Fork the JVM to run the doodle application. Then when the application
    // exits it won't kill the JVM that is running SBT.
    fork in run := true,

    // Dependencies
    libraryDependencies ++=
      Seq (
        "junit" % "junit" % "4.12" % "test",
        "org.scalatest" %% "scalatest" % "3.0.5" % "test",
        "underscoreio" %% "doodle" % "0.8.2"
      )
  )

// Interactive settings

logLevel := Level.Info

shellPrompt := {
    state =>
        Project.extract(state).currentRef.project + " " + version.value +
            " " + scalaVersion.value + "> "
}


