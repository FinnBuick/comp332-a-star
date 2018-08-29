# Macquarie University, Department of Computing #

## COMP332 Programming Languages 2018 ##

## Assignment 1 ##

Due: 11am Wednesday 5 September (week 6)  
Worth: 10% of unit assessment

Marks breakdown:

* Code: 50% (of which tests are worth 10%)
* Report: 50% (of which test description is worth 10%)

Submit a notice of disruption via [Ask@MQ](https://ask.mq.edu.au) if you are unable to submit on time for medical or other legitimate reasons.

Late penalty without proper justification: 20% of the full marks for the assessment per day or part thereof late.

### Overview ###

The framework project for this assignment implements (most of) a simple [path planner](https://en.wikipedia.org/wiki/Motion_planning) using the [A\*-algorithm](https://en.wikipedia.org/wiki/A*_search_algorithm). This demonstrates the kind of algorithm used to plan the paths of autonomous agents in computer games or of robots in factories.

You should consult the [README file](README.md) of this project for more detail about its implementation and for links to lots of information about the A\* algorithm and the B-trees and binomial heaps we've used to implement it.

### What you have to do ###

You can clone the skeleton sbt project for this assignment from its [BitBucket](https://bitbucket.org) repository at [https://bitbucket.org/dominicverity/COMP332-A-star](https://bitbucket.org/dominicverity/COMP332-A-star). This skeleton contains a semi-complete implementation which is missing the following:

*   Implementations of the search method `find`, the pretty print method `pprint`, and the in order traversal method `inOrder` of the `BTree` class in the [BTrees.scala](src/main/scala/BTrees.scala) module.
*   Code to render a path plan as a [Doodle image](https://github.com/underscoreio/doodle) which is missing from the `PathPlan` class in the [PathPlan.scala](src/main/scala/PathPlan.scala) module.

Search for the `FIXME` comments to find where this code is missing. Your task in this assignment is threefold:

*   Provide implementations to replace this missing code.
*   Construct a suite of automated tests of your implementations of the `find()`, `pprint()` and `inOrder()` methods of the `BTree` class.
*   Write a technical report describing the code and tests you have written.

You should consult the comments in our code for more information about what these methods are expected to do and how they might be implemented. Here is some information about the outputs we expect from them:

*   `BTree.find`: If we were to construct a B-tree containing the words `"when", "too", "much", "sport", "is", "barely", "enough"`, by executing the code

        val t1 = BTree("when", "too", "much", "sport", "is", "barely", "enough")
        
    at the sbt console say, then the code
    
        t1.find("too")
        
    should return the value `Some("too")`, because the value `"too"` is an entry in this tree, whereas the code
    
        t1.find("far")
        
    should return the value `None`, because `"far"` is not an entry in this tree.

*   `BTree.pprint`: If we were to construct a B-tree containing the integers `5,7,1,9,13,11,10,2`, by executing the code 
     
        val t2 = BTree(5,7,1,9,13,11,10,2)     
      
    at the sbt console say, then the code
    
        print(t2.toString)
    
    should display the pretty printed output:
    
        TwoNode(
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
                    EmptyNode())))

*   `BTree.inOrder`: If we reuse the trees from the last two examples and execute the code

        t1.inOrder
        
    at the sbt console then it should return the value
    
        List(barely, enough, is, much, sport, too, when)
        
    this being the list of the words stored in the tree in _alphabetical order_ and if we type
    
        t2.inOrder
        
    it should return the value
    
        List(1, 2, 5, 7, 9, 10, 11, 13)
        
    this being the list of numbers stored in that three in _ascending numerical order_. We might note that if we type the following at the console:
    
        val t3 = BTree(1,2,1,3,5,3,5,1,2,7,2,4,5)
        t3.inOrder
    
    it we should obtain the value
    
        List(1, 2, 3, 4, 5, 7)
        
    because our implementation of B-trees rules out having two entries which values are equivalent.

*   `PathPlan.mapImage`: An image illustrating the kind of image we are looking for, along with an associated description, can be found in the [README](README.md) file of the assignment framework.

**Note 1:** once you have correctly implemented the `BTree.find` method the A\* algorithm implementation in [AStar.scala](src/main/scala/AStar.scala) module should work correctly. At that point the application should no longer hang when you execute the `run` task at the sbt console. That being said, it still won't display anything interesting until you've added code to implement construct a nice image to `PlanPath.mapImage.

**Note 2:** In your implementation of the `BTree.find` method you will have to compare entries stored in the tree with the value you are searching for. To check whether one such value `v` is less than (greater than) another one `w` you can use the usual comparison operator `v < w` (or `v > w`). If you want to check them for equality, however, you should use an expression of the form `v equiv w` (rather than `v == w`). The reason for this is a matter of some Scala arcana - ask a question about it on the forum if you are interested in finding out more.

### Running and testing your code ###

The skeleton for this assignment is designed to be run from within the Scala [simple build tool (`sbt`)](https://www.scala-sbt.org/). To do this from the console run the command `sbt` from the root directory of the assignment framework project. After a few messages you should end up at the following prompt:

    assignment1 0.1 2.12.6>
    
You can use the following _tasks_ to compile, run and test the code:

*   `run` compiles and runs the application. 
*   `compile` compiles any modules that have been updated since the last compile operation.
*   `test` compiles any updated modules and executes the automated tests. The are simply Scala modules which can be found in the [tests directory](src/test/scala).
*   `console` compiles any updated modules and allows you to execute Scala code at a console.

Note that if you try to `run` the framework code "out-of-the-box" things will appear to hang, and eventually an `OutOfMemory` exception will be raised. This is because some vital code is missing from the `BTree.scala` module on which the `AStar.scala` implementation depends. In this situation you can terminate execution by pressing ^C (control-C) and then restart `sbt`.

The file [BTreeTests.scala](src/test/scala/BTreeTests.scala) illustrates the basic infrastructure for writing automated tests using the [ScalaTest](http://www.scalatest.org/) framework. At the moment it simply contains tests of the behaviour of empty `BTree` objects.

### Asking questions ###

We've tried to give you lots of pointers on how to complete this assignment, but we are sure there are plenty of things we have missed. So you are strongly encouraged to ask lots questions about this assignment on the COMP332 forum. So start the assignment early and when you get stuck post a question - all of these will be answered in detail. You can, of course, ask questions about this assignment in your tutorial class, your tutor will be happy to help you.

We will not, however, answer forum questions about this assignment after 6pm on Monday the 3rd of September 2018. So please don't leave your questions to the last moment.

### What you must hand in and how ###

A zip file containing all of the code for your project and a type-written report.

Submit every source and build file that is needed to build your program from source, including files in the skeleton that you have not changed. Do not add any new files or include multiple versions of your files. Do not include any libraries or generated files (run the sbt `clean` command before you zip your project). We will compile all of the files that you submit using sbt, so you should avoid any other build mechanisms.

Your submission should include all of the tests that you have used to make sure that your program is working correctly. Note that just testing one or two simple cases is not enough for many marks. You should test as comprehensively as you can.

Your report should describe how you have achieved the goals of the assignment. Do not neglect the report since it is worth 50% of the marks for the assignment.

Your report should contain the following sections:

* A title page or heading that gives the assignment details, your name and student number.
* A brief introduction that summarises the aim of the assignment and the structure of the rest of the report.
* A description of the design and implementation work that you have done to achieve the goals of the assignment. Listing some code fragments may be useful to illustrate your description, but don't give a long listing. Leaving out obvious stuff is OK, as long as what you have done is clear. A good rule of thumb is to include enough detail to allow a fellow student to understand it if they are at the stage you were at when you started work on the assignment.
* A description of the testing that you carried out. You should demonstrate that you have used a properly representative set of test cases to be confident that you have covered all the bases. Include details of the tests that you used and the rationale behind why they were chosen. Do not just print the tests out without explanation.

Submit your code and report electronically as a single zip file called `ass1.zip` using the appropriate submission link on the COMP332 iLearn website by the due date and time. Your report should be in PDF format.

DO NOT SUBMIT YOUR ASSIGNMENT OR DOCUMENTATION IN ANY OTHER FORMAT THAN ZIP and PDF, RESPECTIVELY. Use of any other format slows down the marking and may result in a mark deduction.

### Marking ###

The assignment will be assessed according to the assessment standards for the unit learning outcomes.

Marks will be allocated equally to the code and to the report. Your code will be assessed for correctness and quality with respect to the assignment description. Marking of the report will assess the clarity and accuracy of your description and the adequacy of your testing. 20% of the marks for the assignment will be allocated to testing.

---
[Dominic Verity](http://orcid.org/0000-0002-4137-6982)  
Last modified: 13 August 2018  
[Copyright (c) 2018 by Dominic Verity. Macquarie University. All rights reserved.](http://mozilla.org/MPL/2.0/)

