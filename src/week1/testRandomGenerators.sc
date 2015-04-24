package week1

object testRandomGenerators {
  import java.util.Random

  val rand = new Random                           //> rand  : java#11.util#49.Random#6769 = java.util.Random@530c12
  /*arbitrary : Int*/
  rand.nextInt()                                  //> res0: Int#1080 = 1766234021

  /*
  Generators
  Let's define
  a 'trait' 'Generator[T]' that
  generates random values of type T:*/
  trait Generator[ +T ] {
    def generate: T
  }
  /*Some instances:*/
  val integers = new Generator[ Int ] {
    val rand = new java.util.Random
    def generate = rand.nextInt()
  }                                               //> integers  : week1#27.testRandomGenerators#36016.Generator#3578415[Int#1080]{
                                                  //| val rand#3578754: java#11.util#49.Random#6769} = week1.testRandomGenerators$
                                                  //| $anonfun$main$1$$anon$1@1464ce8
  val booleans = new Generator[ Boolean ] {
    def generate = integers.generate > 0
  }                                               //> booleans  : week1#27.testRandomGenerators#36016.Generator#3578415[Boolean#25
                                                  //| 02] = week1.testRandomGenerators$$anonfun$main$1$$anon$2@1829d67
  val pairs = new Generator[ ( Int, Int ) ] {
    def generate = ( integers.generate, integers.generate )
  }                                               //> pairs  : week1#27.testRandomGenerators#36016.Generator#3578415[(Int#1080, In
                                                  //| t#1080)] = week1.testRandomGenerators$$anonfun$main$1$$anon$3@17f6480
  /*
  Streamlining It
  Can we avoid
  the 'new Generator ...' boilerplate ?
  Ideally,
  we would like to write:*/
  /** {{{
    * /*value 'map' is
    * not a member of
    * week1.testRandomGenerators.Generator[Int]{val rand: java.util.Random}*/
    * val booleans1 = for ( x <- integers ) yield x > 0
    *
    * def pairs1[ T, U ]( t: Generator[ T ], u: Generator[ U ] ) = for {
    * /*value 'flatMap' is
    * not a member of
    * week1.testRandomGenerators.Generator[T]*/
    * x <- t
    * y <- u
    * } yield ( x, y )
    * }}}
    */
  /*What does this expand to ?
  (with compiler
  accoding to Translation Rules
  to for-expression)*/
  /** {{{
    * val booleans2 = integers map (x => x > 0)
    *
    * def pairs2[T, U](t: Generator[T],
    * u: Generator[U]) =
    * t flatMap (x => u map (y => (x, y)))
    * }}}
    */
  /*Need 'map' and 'flatMap' for that!*/
  /*
	Generator with
  'map' and 'flatMap'
  Here's
  a more convenient version of 'Generator':*/
  trait Generator1[ +T ] {
    self => // an alias for "this".
    /*abstract*/
    def generate: T
    def map[ S ]( f: T => S ): Generator1[ S ] =
      new Generator1[ S ] {
        /*overloaded ?
        concrete implementation*/
        def generate = f( self.generate )
      }
    def flatMap[ S ]( f: T => Generator1[ S ] ): Generator1[ S ] =
      new Generator1[ S ] {
        /*
        just 'generate' translates to 'this.generate'
        where
        'this' is context for 'flatMap' not 'Generator1' itself
        but refer to current method as host / owner
        so, wrong level of hierarchy
        */
        def generate = f( self.generate ).generate
        /*or*/
        //*def generate = f( Generator1.generate ).generate
      }
  }

  /*
  The booleans Generator
  What does this definition resolve to?*/
  /*Some instances:*/
  val integers2 = new Generator1[ Int ] {
    val rand = new java.util.Random
    def generate = rand.nextInt()
  }                                               //> integers2  : week1#27.testRandomGenerators#36016.Generator1#3578423[Int#108
                                                  //| 0]{val rand#3578826: java#11.util#49.Random#6769} = week1.testRandomGenerat
                                                  //| ors$$anonfun$main$1$$anon$6@16e8792
  integers2.generate                              //> res1: Int#1080 = 1829514496

  val booleans3 = for ( x <- integers2 ) yield x > 0
                                                  //> booleans3  : week1#27.testRandomGenerators#36016.Generator1#3578423[Boolean
                                                  //| #2502] = week1.testRandomGenerators$$anonfun$main$1$Generator1$1$$anon$4@11
                                                  //| d970
  booleans3.generate                              //> res2: Boolean#2502 = false
  
  /*detailed substitution model of
  compiler actual work*/
  val booleans4 = integers2 map { x => x > 0 }    //> booleans4  : week1#27.testRandomGenerators#36016.Generator1#3578423[Boolean
                                                  //| #2502] = week1.testRandomGenerators$$anonfun$main$1$Generator1$1$$anon$4@42
                                                  //| b0a6
  booleans4.generate                              //> res3: Boolean#2502 = true
  val booleans5 = new Generator1[ Boolean ] {
    def generate = ( ( x: Int ) => x > 0 )( integers2.generate )
  }                                               //> booleans5  : week1#27.testRandomGenerators#36016.Generator1#3578423[Boolean
                                                  //| #2502] = week1.testRandomGenerators$$anonfun$main$1$$anon$7@f82f98
  booleans5.generate                              //> res4: Boolean#2502 = true

  val booleans6 = new Generator1[ Boolean ] {
    def generate = integers2.generate > 0
  }                                               //> booleans6  : week1#27.testRandomGenerators#36016.Generator1#3578423[Boolean
                                                  //| #2502] = week1.testRandomGenerators$$anonfun$main$1$$anon$8@1f983a6
  booleans6.generate                              //> res5: Boolean#2502 = false

  /*
  The pairs Generator*/
  def pairs3[ T, U ]( t: Generator1[ T ],
                      u: Generator1[ U ] ) = t flatMap {
    x => u map { y => ( x, y ) }
  }                                               //> pairs3: [T#3578436, U#3578437](t#3578926: week1#27.testRandomGenerators#360
                                                  //| 16.Generator1#3578423[T#3578436], u#3578927: week1#27.testRandomGenerators#
                                                  //| 36016.Generator1#3578423[U#3578437])week1#27.testRandomGenerators#36016.Gen
                                                  //| erator1#3578423[(T#3578436, U#3578437)]
  pairs3( integers2, booleans5 ).generate         //> res6: (Int#1080, Boolean#2502) = (-778738356,true)
  def pairs4[ T, U ]( t: Generator1[ T ],
                      u: Generator1[ U ] ) = t flatMap {
    x =>
      new Generator1[ ( T, U ) ] {
        def generate = ( x, u.generate )
      }
  }                                               //> pairs4: [T#3578441, U#3578442](t#3578965: week1#27.testRandomGenerators#360
                                                  //| 16.Generator1#3578423[T#3578441], u#3578966: week1#27.testRandomGenerators#
                                                  //| 36016.Generator1#3578423[U#3578442])week1#27.testRandomGenerators#36016.Gen
                                                  //| erator1#3578423[(T#3578441, U#3578442)]
  pairs4( integers2, booleans5 ).generate         //> res7: (Int#1080, Boolean#2502) = (-1223887307,false)
  def pairs5[ T, U ]( t: Generator1[ T ],
                      u: Generator1[ U ] ) = new Generator1[ ( T, U ) ] {
    def generate = ( new Generator1[ ( T, U ) ] {
      def generate = ( t.generate, u.generate )
    } ).generate
  }                                               //> pairs5: [T#3578446, U#3578447](t#3579006: week1#27.testRandomGenerators#360
                                                  //| 16.Generator1#3578423[T#3578446], u#3579007: week1#27.testRandomGenerators#
                                                  //| 36016.Generator1#3578423[U#3578447])week1#27.testRandomGenerators#36016.Gen
                                                  //| erator1#3578423[(T#3578446, U#3578447)]
  pairs5( integers2, booleans5 ).generate         //> res8: (Int#1080, Boolean#2502) = (1827595781,true)
  def pairs6[ T, U ]( t: Generator1[ T ],
                      u: Generator1[ U ] ) = new Generator1[ ( T, U ) ] {
    def generate = ( t.generate, u.generate )
  }                                               //> pairs6: [T#3578451, U#3578452](t#3579043: week1#27.testRandomGenerators#360
                                                  //| 16.Generator1#3578423[T#3578451], u#3579044: week1#27.testRandomGenerators#
                                                  //| 36016.Generator1#3578423[U#3578452])week1#27.testRandomGenerators#36016.Gen
                                                  //| erator1#3578423[(T#3578451, U#3578452)]
  pairs6( integers2, booleans5 ).generate         //> res9: (Int#1080, Boolean#2502) = (-1864736768,true)
  /*
  Generator Examples*/
  /*return just input itself */
  def single[ T ]( x: T ): Generator1[ T ] = new Generator1[ T ] {
    def generate = x
  }                                               //> single: [T#3578456](x#3579057: T#3578456)week1#27.testRandomGenerators#3601
                                                  //| 6.Generator1#3578423[T#3578456]
  single( 1 ).generate                            //> res10: Int#1080 = 1
  single( true ).generate                         //> res11: Boolean#2502 = true
  /*value from interval*/
  def choose( lo: Int,
              hi: Int ): Generator1[ Int ] =
    for ( x <- integers2 ) yield //ArithmeticException: / by zero
    //lo + x % ( hi - Math.abs(lo) )
    /*  max difference
      ( hi - lo ) */ /* from 0 to max difference
      x % ( hi - lo )*/
      /*normalization for
      generated 'x'*/
      lo + x % ( hi - lo )                        //> choose: (lo#3578879: Int#1080, hi#3578880: Int#1080)week1#27.testRandomGene
                                                  //| rators#36016.Generator1#3578423[Int#1080]
  /*??? res12: Int#1080 = -17 ???*/
  val choose7toMinus7 = choose( -7, 7 ).generate  //> choose7toMinus7  : Int#1080 = -11

  ( -7 ) + 14                                     //> res12: Int#1080(7) = 7
  ( -7 ) + 7                                      //> res13: Int#1080(0) = 0
  ( -7 ) - 7                                      //> res14: Int#1080(-14) = -14
  ( 7 ) + 7                                       //> res15: Int#1080(14) = 14

  ( -7 ) + ( 0 ) % ( 7 - ( -7 ) )                 //> res16: Int#1080 = -7
  ( -7 ) + ( 1 ) % ( 7 - ( -7 ) )                 //> res17: Int#1080 = -6
  ( -7 ) + ( 3 ) % ( 7 - ( -7 ) )                 //> res18: Int#1080 = -4
  ( -7 ) + ( 7 ) % ( 7 - ( -7 ) )                 //> res19: Int#1080 = 0
  ( -7 ) + ( -1 ) % ( 7 - ( -7 ) )                //> res20: Int#1080 = -8
  ( -7 ) + ( -3 ) % ( 7 - ( -7 ) )                //> res21: Int#1080 = -10
  /*max difference*/
  ( -7 ) + ( -7 ) % ( 7 - ( -7 ) )                //> res22: Int#1080 = -14
  0 + 0 % ( 7 - 0 )                               //> res23: Int#1080 = 0
  0 + 1 % ( 7 - 0 )                               //> res24: Int#1080 = 1
  0 + 3 % ( 7 - 0 )                               //> res25: Int#1080 = 3
  0 + 7 % ( 7 - 0 )                               //> res26: Int#1080 = 0
  //choose(true, false).generate

  /*one value from input sequence with arbitrary length*/
  def oneOf[ T ]( xs: T* ): Generator1[ T ] =
    /*high bound is 'xs.length - 1'
    but low is '0'*/
    for (
      idx <- choose( 0, xs.length )
    /*value 'filter' is
    not a member of
    Generator1*/
    /*if xs.length > 0 && xs.length != idx*/ ) yield //*xs( idx )
    xs( Math.abs( idx ) )                         //> oneOf: [T#3578462](xs#3579121: T#3578462*)week1#27.testRandomGenerators#360
                                                  //| 16.Generator1#3578423[T#3578462]
  //*if (idx == xs.length) xs( 0 ) else xs( idx )

  def oneOfManySize[ T ]( xs: T* ): Int = xs.length
                                                  //> oneOfManySize: [T#3578465](xs#3579177: T#3578465*)Int#1080
  def lastOneOfMany[ T ]( xs: T* ): T = xs( xs.length - 1 )
                                                  //> lastOneOfMany: [T#3578468](xs#3579179: T#3578468*)T#3578468
  def firstOneOfMany[ T ]( xs: T* ): T = xs( 0 )  //> firstOneOfMany: [T#3578471](xs#3579210: T#3578471*)T#3578471

  oneOfManySize[ Int ]( 1, 2, 3, 4, 5, 6, 7 )     //> res27: Int#1080 = 7
  firstOneOfMany[ Int ]( 1, 2, 3, 4, 5, 6, 7 )    //> res28: Int#1080 = 1
  lastOneOfMany[ Int ]( 1, 2, 3, 4, 5, 6, 7 )     //> res29: Int#1080 = 7

  /*why negative values ?
  ArrayIndexOutOfBoundsException: -2*/
  oneOf[ Int ]( 1, 2, 3, 4, 5, 6, 7 ).generate    //> res30: Int#1080 = 5
  oneOf[ Int ]( 1, 2, 3, 4, 5, 6, 7 ).generate    //> res31: Int#1080 = 4
  oneOf[ Int ]( 1, 2, 3, 4, 5, 6, 7 ).generate    //> res32: Int#1080 = 5
  
  oneOf[ Boolean ]( true, false ).generate        //> res33: Boolean#2502 = true
  
  oneOf[ String ]( "red", "blue", "green" ).generate
                                                  //> res34: String#230 = green
  oneOf[ String ]( "red", "blue", "green" ).generate
                                                  //> res35: String#230 = blue
  oneOf[ String ]( "red", "blue", "green" ).generate
                                                  //> res36: String#230 = green

  /*
  A List Generator
  A list is
  either
  an empty list or
  a non-empty list.*/
  def lists: Generator1[ List[ Int ] ] = for {
    /*? pick / inherit implisit 'generator' for Booleans ?*/
    /*flip a coin*/
    isEmpty <- booleans6
    /*work with 'generators'*/
    list <- if ( isEmpty ) emptyLists else nonEmptyLists
  } yield list                                    //> lists: => week1#27.testRandomGenerators#36016.Generator1#3578423[List#26140
                                                  //| 26[Int#1080]]

	/*work with 'generator' 'single'*/
  def emptyLists = single( Nil )                  //> emptyLists: => week1#27.testRandomGenerators#36016.Generator1#3578423[scala
                                                  //| #25.collection#2731.immutable#5818.Nil#7807.type]

  def nonEmptyLists = for {
	  /*work with 'generator' 'integers2'*/
    head <- integers2
	  /*work with 'generator' 'lists'*/
    tail <- lists
  } yield head :: tail                            //> nonEmptyLists: => week1#27.testRandomGenerators#36016.Generator1#3578423[Li
                                                  //| st#8589[Int#1080]]
  lists.generate                                  //> res37: List#2614026[Int#1080] = List(1866288081)

  /*
  A Tree Generator
  Can you implement
  a generator that
  creates random Tree objects?*/
  trait Tree
  case class Inner( left: Tree, right: Tree ) extends Tree
  /*? like for encoding ?
  only 'Leafs' hold actual values*/
  case class Leaf( x: Int ) extends Tree
  /*
  from: "https://github.com/rickynils/scalacheck/wiki/User-Guide"
  may be*/
  /**{{{
  case class Node(left: Tree, right: Tree, v: Int) extends Tree
  case object Leaf extends Tree
  }}}  */
  /*Hint:
	a tree is
	either
	a leaf or
	an inner node.*/

  def trees: Generator1[ Tree ] = for {
    /*? pick / inherit implisit generator for Booleans ?*/
    isEmpty <- booleans6
    subTree <- if ( isEmpty ) leafNode else innerNode
    //*subTree <- leafNode
    //*subTree <- innerNode
  } yield subTree                                 //> trees: => week1#27.testRandomGenerators#36016.Generator1#3578423[week1#27.t
                                                  //| estRandomGenerators#36016.Tree#3578476]

  def leafNode: Generator1[ Leaf ] =
    //single( Leaf ).generate
    //Leaf(integers2.generate)
    //single( integers2.generate )
    single( Leaf(integers2.generate) )            //> leafNode: => week1#27.testRandomGenerators#36016.Generator1#3578423[week1#2
                                                  //| 7.testRandomGenerators#36016.Leaf#3578480]

  def innerNode: Generator1[Inner] =
    //Inner( trees.generate, trees.generate )
    //Inner( trees, trees )
    //pairs6( trees.generate, trees.generate )
    //pairs6( trees, trees ).generate
    for {
      leftSubTree <- trees
      rightSubTree <- trees
    } yield Inner( leftSubTree, rightSubTree )    //> innerNode: => week1#27.testRandomGenerators#36016.Generator1#3578423[week1#
                                                  //| 27.testRandomGenerators#36016.Inner#3578477]
  
  trees.generate                                  //> res38: week1#27.testRandomGenerators#36016.Tree#3578476 = Inner(Inner(Inner
                                                  //| (Leaf(-1868196138),Inner(Inner(Leaf(1486989935),Inner(Inner(Inner(Leaf(2759
                                                  //| 93525),Leaf(470227210)),Inner(Leaf(-704993171),Leaf(975781971))),Inner(Inne
                                                  //| r(Leaf(-559198742),Leaf(-1964533852)),Leaf(1282557697)))),Leaf(-1545111726)
                                                  //| )),Leaf(624652974)),Leaf(1176174789))
  trees.generate                                  //> res39: week1#27.testRandomGenerators#36016.Tree#3578476 = Inner(Leaf(268607
                                                  //| 271),Inner(Leaf(1540517233),Inner(Inner(Leaf(2121752151),Inner(Inner(Inner(
                                                  //| Leaf(554601897),Leaf(-540636349)),Leaf(1613504789)),Leaf(1022677351))),Inne
                                                  //| r(Leaf(-1184225135),Leaf(1908523996)))))
  trees.generate                                  //> res40: week1#27.testRandomGenerators#36016.Tree#3578476 = Inner(Leaf(119078
                                                  //| 8263),Inner(Leaf(-1220984583),Inner(Inner(Leaf(479253084),Inner(Leaf(-68054
                                                  //| 6142),Inner(Leaf(420426072),Inner(Inner(Inner(Inner(Leaf(1700396438),Inner(
                                                  //| Inner(Leaf(-2054293852),Inner(Leaf(1569752160),Inner(Inner(Leaf(1502570627)
                                                  //| ,Inner(Inner(Leaf(810298421),Leaf(2092024091)),Inner(Leaf(229684656),Leaf(-
                                                  //| 379509937)))),Leaf(980656058)))),Inner(Leaf(-937596462),Inner(Leaf(-1397891
                                                  //| 295),Leaf
                                                  //| Output exceeds cutoff limit.

}