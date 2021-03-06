package assignments

object HeapTest {
  /*
   * In this assignment, 
   * you will work with 
   * the 'ScalaCheck' library for 
   * automated specification-based testing.

  You�re given 
  several implementations of 
  a purely functional 'data structure': 
  a 'heap', 
  which is 
  a
  'priority queue' supporting operations 
  insert, 
  meld, 
  findMin, 
  deleteMin. 
  Here is the interface:*/
  trait HeapImplement {
    type H // type of a heap
    type A // type of an element
    def ord: Ordering[ A ] // ordering on elements

    def empty: H // the empty heap
    def isEmpty( h: H ): Boolean // whether the given heap h is empty

    def insert( x: A, h: H ): H // the heap resulting from inserting x into h
    def meld( h1: H, h2: H ): H // the heap resulting from merging h1 and h2

    def findMin( h: H ): A // a minimum of the heap h
    def deleteMin( h: H ): H // a heap resulting from deleting a minimum of h
  }

  /*
   * All these operations are pure; 
   * they never modify the given heaps, and 
   * may return new heaps. 
   * This purely functional interface is 
   * taken from 
   * Brodal & Okasaki�s paper, Optimal Purely Functional Priority Queues.

  Here is 
  what you need to know about priority queues to 
  complete the assignment. 
  In a nutshell, 
  a priority queue is 
  like a queue, except 
  it's not first-in first-out, but 
  whatever-in
  'minimum-out'. 
  Starting with 
  the  'empty queue', 
  you can 
  construct 
  a new non-empty bigger queue by 
  recursively inserting an element. 
  You can also 
  meld two queues, 
  which results in 
  a new queue that 
  contains 
  all the elements of the first queue and 
  all the elements of the second queue. 
  You can 
  test whether 
  a queue is 
  empty or 
  not with isEmpty. 
  If you have 
  a non-empty queue, 
  you can 
  find its minimum with findMin. 
  You can also 
  get a smaller queue from 
  a non-empty queue by 
  deleting the minimum element with deleteMin.

  Only one of 
  the several implementations you are given is 
  correct. 
  The other ones have bugs. 
  Your goal is 
  to write some properties that 
  will be automatically checked. 
  All the properties you write should be 
  satisfiable by 
  the correct implementation, while 
  at least one of them should 
  fail in each incorrect implementation, thus 
  revealing it�s buggy.

  You should 
  write your properties in 
  the body of the QuickCheckHeap class in 
  the file src/main/scala/quickcheck/QuickCheck.scala. 
  In this class, 
  the heap operates on Int elements with 
  the natural ordering, so 
  findMin finds the least integer in the heap.

  As an example of 
  what you should do, 
  here is 
  a property that 
  ensures that if 
  you insert an element into an empty heap, then 
  find the minimum of the resulting heap, 
  you get the element back:*/
  /** {{{
    * property( "min1" ) = forAll { a: Int =>
    * val h = insert( a, empty )
    * findMin( h ) == a
    * }
    * }}}
    */
  /*
   * We also recommend you write 
   * a generator of heaps, of abstract type H, so that 
   * you can write properties on any random heap, 
   * generated by your procedure. 
   * For example,*/
  /** {{{
    * property( "gen1" ) = forAll { ( h: H ) =>
    * val m = if ( isEmpty( h ) ) 0 else findMin( h )
    * findMin( insert( m, h ) ) == m
    * }
    * }}}
    */
  /*
   * To get you in shape, 
   * here is 
   * an example of 
   * a generator for maps of type Map[Int,Int].*/
  /** {{{
    * lazy val genMap: Gen[ Map[ Int, Int ] ] = for {
    * k <- arbitrary[ Int ]
    * v <- arbitrary[ Int ]
    * m <- oneOf( const( Map.empty[ Int, Int ] ), genMap )
    * } yield m.updated( k, v )
    * }}}
    */
  /*In order to 
   * get full credit, 
   * all tests should pass, 
   * that is 
   * you should correctly identify each buggy implementation while 
   * only writing properties that are 
   * true of heaps. 
   * You are free to 
   * write as many or 
   * as few properties as you want 
   * in order to 
   * achieve a full passing suite.*/

  /*Hints
  Here are some possible properties we suggest you write.

  >>If you insert 
  any two elements into an empty heap, 
  finding the minimum of 
  the resulting heap should get 
  the smallest of the two elements back.

  >>If you insert 
  an element into an empty heap, then 
  delete the minimum, 
  the resulting heap should be empty.

  >>Given any heap, 
  you should get 
  a sorted sequence of elements when 
  continually 
  finding and 
  deleting minima. 
  (Hint: 
  recursion and 
  helper functions are your friends.)

  >>Finding 
  a minimum of the melding of 
  any two heaps should return 
  a minimum of one or 
  the other.*/

  //package quickcheck

  //import common._

  // http://www.brics.dk/RS/96/37/BRICS-RS-96-37.pdf

  // Figure 1, page 3
  trait HeapPQ {
    type H // type of a heap
    type A // type of an element
    def ord: Ordering[ A ] // ordering on elements

    def empty: H // the empty heap
    def isEmpty( h: H ): Boolean // whether the given heap h is empty

    def insert( x: A, h: H ): H // the heap resulting from inserting x into h
    def meld( h1: H, h2: H ): H // the heap resulting from merging h1 and h2

    def findMin( h: H ): A // a minimum of the heap h
    def deleteMin( h: H ): H // a heap resulting from deleting a minimum of h
  }

  trait IntHeap extends HeapPQ {
    override type A = Int
    override def ord = scala.math.Ordering.Int
  }

  // Figure 3, page 7
  trait BinomialHeap extends HeapPQ {

    type Rank = Int
    case class Node(
      x: A,
      r: Rank,
      c: List[ Node ] )
    override type H = List[ Node ]

    protected def root( t: Node ) = t.x
    protected def rank( t: Node ) = t.r
    protected def link(
      t1: Node,
      t2: Node ): Node = // t1.r==t2.r
      if ( ord.lteq( t1.x, t2.x ) ) {
        Node( t1.x, t1.r + 1, t2 :: t1.c )
      } else {
        Node( t2.x, t2.r + 1, t1 :: t2.c )
      }

    protected def ins(
      t: Node,
      ts: H ): H = ts match {
      case Nil => List( t )
      case tp :: ts => // t.r<=tp.r
        if ( t.r < tp.r ) {
          t :: tp :: ts
        } else {
          ins( link( t, tp ), ts )
        }
    }

    override def empty = Nil
    override def isEmpty( ts: H ) = ts.isEmpty

    override def insert(
      x: A,
      ts: H ) = ins( Node( x, 0, Nil ), ts )
    override def meld(
      ts1: H,
      ts2: H ) = ( ts1, ts2 ) match {
      case ( Nil, ts ) => ts
      case ( ts, Nil ) => ts
      case ( t1 :: ts1, t2 :: ts2 ) =>
        if ( t1.r < t2.r ) {
          t1 :: meld( ts1, t2 :: ts2 )
        } else if ( t2.r < t1.r ) {
          t2 :: meld( t1 :: ts1, ts2 )
        } else {
          ins( link( t1, t2 ), meld( ts1, ts2 ) )
        }
    }

    override def findMin( ts: H ) = ts match {
      case Nil      => throw new NoSuchElementException( "min of empty heap" )
      case t :: Nil => root( t )
      case t :: ts =>
        val x = findMin( ts )
        if ( ord.lteq( root( t ), x ) ) {
          root( t )
        } else { x }
    }
    override def deleteMin( ts: H ) = ts match {
      case Nil => throw new NoSuchElementException( "delete min of empty heap" )
      case t :: ts =>
        def getMin(
            t: Node,
            ts: H ): ( Node, H ) = ts match {
            case Nil => ( t, Nil )
            case tp :: tsp =>
              /*pattern matching &
               * recursion magic*/
              val ( tq, tsq ) = getMin( tp, tsp )

              if ( ord.lteq( root( t ), root( tq ) ) ) {
                ( t, ts )
              } else {
                ( tq, t :: tsq )
              }
          }
        /*still inside 'case' ?*/    
        val ( Node( _, _, c ), tsq ) = getMin( t, ts )

        meld( c.reverse, tsq )
    }
  }

  trait Bogus1BinomialHeap extends BinomialHeap {
    override def findMin( ts: H ) = ts match {
      case Nil     => throw new NoSuchElementException( "min of empty heap" )
      case t :: ts => root( t )
    }
  }

  trait Bogus2BinomialHeap extends BinomialHeap {
    override protected def link(
      t1: Node,
      t2: Node ): Node = // t1.r==t2.r
      if ( !ord.lteq( t1.x, t2.x ) ) {
        Node( t1.x, t1.r + 1, t2 :: t1.c )
      } else {
        Node( t2.x, t2.r + 1, t1 :: t2.c )
      }
  }

  trait Bogus3BinomialHeap extends BinomialHeap {
    override protected def link(
      t1: Node,
      t2: Node ): Node = // t1.r==t2.r
      /** Return true if `x` <= `y` in the ordering. */
      if ( ord.lteq( t1.x, t2.x ) ) {
        Node( t1.x, t1.r + 1, t1 :: t1.c )
      } else {
        Node( t2.x, t2.r + 1, t2 :: t2.c )
      }
    /*was
     * protected def link(
      t1: Node,
      t2: Node ): Node = // t1.r==t2.r
      if ( ord.lteq( t1.x, t2.x ) ) {
        Node( t1.x, t1.r + 1, t2 :: t1.c )
      } else {
        Node( t2.x, t2.r + 1, t1 :: t2.c )
      }*/
  }

  trait Bogus4BinomialHeap extends BinomialHeap {
    /*so,
     * 'deleteMin' must be incorrect
     * in the way how it is defined / calculate 
     * List[ Node ] & rest of the Heap: H 
     * one or both values fail*/
    override def deleteMin( ts: H ) = ts match {
      case Nil =>
        throw new NoSuchElementException( "delete min of empty heap" )
      case t :: ts => meld( t.c.reverse, ts )
    }
    /*was
     override def deleteMin( ts: H ) = ts match {
      case Nil => throw new NoSuchElementException( "delete min of empty heap" )
      case t :: ts =>
        def getMin(
            t: Node,
            ts: H ): ( Node, H ) = ts match {
            case Nil => ( t, Nil )
            case tp :: tsp =>
              val ( tq, tsq ) = getMin( tp, tsp )
              if ( ord.lteq( root( t ), root( tq ) ) ) {
                ( t, ts )
              } else {
                ( tq, t :: tsq )
              }
          }
        val ( Node( _, _, c/*: List[ Node ]*/ ), tsq ) = getMin( t, ts )
        meld( c.reverse, tsq )*/
  }

  trait Bogus5BinomialHeap extends BinomialHeap {
    override def meld(
      ts1: H,
      ts2: H ) = ts1 match {
      case Nil       => ts2
      case t1 :: ts1 => List( Node( t1.x, t1.r, ts1 ++ ts2 ) )
    }
  }

  /*unit tests*/
  def main( args: Array[ String ] ) = {
    /*combining traits*/
    object QuickCheckBinomialHeap extends IntHeap /*QuickCheckHeap*/ with BinomialHeap
    object Bogus3Heap extends IntHeap with Bogus3BinomialHeap
    object Bogus4Heap extends IntHeap with Bogus4BinomialHeap
    object Bogus5Heap extends IntHeap /*BinomialHeap*/ with Bogus5BinomialHeap

    val heap1 = QuickCheckBinomialHeap
      /*override type H = List[ Node ]*/
      //.insert( 9, QuickCheckBinomialHeap.H )/*: H*/ 
      .insert( 9, QuickCheckBinomialHeap.empty ) /*: H*/
    /* the heap 
    resulting from inserting 'x' into 'h' */
    val heap2 = QuickCheckBinomialHeap
      .insert( 3, heap1 )
    val heap3 = QuickCheckBinomialHeap
      .insert( 5, heap2 )

    type H = QuickCheckBinomialHeap.Node //new BinomialHeap.Node 
      def heapLoop(
        heapRemains: List[ H ],
        growingSeq: Seq[ Int ] ): Seq[ Int ] =
        //if (heapRemains == Nil) {
        //if (heapRemains == empty) {
        //if (heapRemains.isEmpty) {
        if ( QuickCheckBinomialHeap.isEmpty( heapRemains ) ) {
          growingSeq
        } else {
          val min = QuickCheckBinomialHeap.findMin( heapRemains )

          heapLoop(
            QuickCheckBinomialHeap
              .deleteMin( heapRemains ),
            //*min +: growingSeq )
            growingSeq :+ min )
        }

    val sequence = heapLoop(
      heap3,
      Seq.empty[ Int ] )
    val sortedSeq = sequence.sorted
    val reversedSeq = sequence.reverse

    /*property( "minOfTwo1" ) = forAll { ( x: Int, y: Int ) =>
      val h = insert( y, insert( x, empty ) )

      findMin( h ) == Math.min( x, y )
    }*/
    val minOfTwoHeap0 = QuickCheckBinomialHeap
      .insert(
        2,
        QuickCheckBinomialHeap
          .insert(
            1,
            QuickCheckBinomialHeap.empty ) )
    val minOfTwoHeap3 =
      Bogus3Heap.insert(
        2,
        Bogus3Heap
          .insert(
            1,
            Bogus3Heap.empty ) )
    val minOfTwoHeap4 =
      //new Bogus5BinomialHeap.insert(
      Bogus4Heap.insert(
        2,
        Bogus4Heap
          .insert(
            1,
            Bogus4Heap.empty ) )
    val minOfTwoHeap5 = Bogus5Heap
      .insert(
        2,
        Bogus5Heap
          .insert(
            1,
            Bogus5Heap.empty ) )

    println( "`minOfTwoHeap0` is: " + ( minOfTwoHeap0 ) )
    println( "`minOfTwoHeap0 min` is: " +
      QuickCheckBinomialHeap.findMin( minOfTwoHeap0 ) )
    println( "`minOfTwoHeap0 max of two` is: " +
      QuickCheckBinomialHeap
      .findMin(
        QuickCheckBinomialHeap
          .deleteMin( minOfTwoHeap0 ) ) )
    println( "`minOfTwoHeap3` is: " + ( minOfTwoHeap3 ) )
    println( "`minOfTwoHeap3 min` is: " +
      Bogus3Heap.findMin( minOfTwoHeap3 ) )
    println( "`minOfTwoHeap3 max of two` is: " +
      Bogus3Heap
      .findMin(
        Bogus3Heap
          .deleteMin( minOfTwoHeap3 ) ) )
    println( "`minOfTwoHeap4` is: " + ( minOfTwoHeap4 ) )
    println( "`minOfTwoHeap4 min` is: " +
      Bogus4Heap.findMin( minOfTwoHeap4 ) )
    println( "`min(1, 2)` is: " + Math.min( 1, 2 ) )
    println( "`sequence == sortedSeq` is: " + ( sequence == sortedSeq ) )
    println( "`sequence == reversedSeq` is: " + ( sequence == reversedSeq ) )
    println( "`sortedSeq == reversedSeq` is: " + ( sortedSeq == reversedSeq ) )
    println( "`sequence` is: " + sequence )
    println( "`sequence.sorted` is: " + sequence.sorted )
    println( "`sequence.reverse` is: " + sequence.reverse )
    println( "`sequence == sequence.sorted` is: " )
    println( sequence == sequence.sorted )
    println( "`sequence eq sequence.sorted` is: " )
    //comparing values of types 
    //String and Seq[Int] using `eq' will always yield `false`
    println( sequence eq sequence.sorted )
    println( "`sequence.reverse == sequence.sorted` is: " )
    println( sequence.reverse == sequence.sorted )
    println( "`sequence.mkString` is: " )
    println( sequence.mkString )
    println( "`sequence.mkString(", ")` is: " )
    println( sequence.mkString( "," ) )
    println( "`sequence.mkString eq sequence.sorted.mkString` is: " )
    println( sequence.mkString( "," ) == sequence.sorted.mkString( "," ) )
    /*println( "`sequence.reverse.mkString == sequence.sorted.mkString` is: " +
      sequence.reverse.mkString == sequence.sorted.mkString )*/
    //QuickCheckBinomialHeap.isEmpty(ts: QuickCheckBinomialHeap.H)
    //QuickCheckBinomialHeap.H
    println( "QuickCheckBinomialHeap.empty is: " + QuickCheckBinomialHeap.empty )
    println( "QuickCheckBinomialHeap.empty == Nil is: " +
      QuickCheckBinomialHeap.empty == Nil )
    println( "QuickCheckBinomialHeap.empty == List() is: " +
      QuickCheckBinomialHeap.empty == List() )
    println( "QuickCheckBinomialHeap.empty == List.empty is: " +
      QuickCheckBinomialHeap.empty == List.empty )
    println( "QuickCheckBinomialHeap.empty.isEmpty is: " +
      QuickCheckBinomialHeap.empty.isEmpty )
    println( "heap3 is: " +
      heap3 )
    println( "QuickCheckBinomialHeap.findMin( heap3 ) is: " +
      QuickCheckBinomialHeap.findMin( heap3 ) )
    println( "QuickCheckBinomialHeap.isEmpty( heap3 ) is: " +
      QuickCheckBinomialHeap.isEmpty( heap3 ) )

  }
}