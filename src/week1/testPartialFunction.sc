package week1

object testPartialFunction {
  /*
  Partial Matches
  We have seen that
  a 'pattern matching' 'block' like*/
  //*{ case "ping" => "pong" }
  /*can be given*/
  //*type String => String
  val f: String => String = { case "ping" => "pong" }
                                                  //> f  : String => String = <function1>
  /*But
  the function is
  not defined on all its 'domain'!*/
  //*f("pong") // gives a MatchError
  /*Is there
  a way to
  find out
  whether
  the function can be
  applied to a given argument before running it?*/
  /*
  Partial Functions
  Indeed there is:*/
  val f0: PartialFunction[ String, String ] = { case "ping" => "pong" }
                                                  //> f0  : PartialFunction[String,String] = <function1>

  f0( "ping" ) // true                            //> res0: String = pong
  f0.isDefinedAt( "ping" ) // true                //> res1: Boolean = true
  /*f0( "pong" ) causes exception*/
  f0.isDefinedAt( "pong" ) // false               //> res2: Boolean = false
  /*
	The 'partial function' 'trait' is deﬁned as follows:
	trait PartialFunction[-A, +R] extends Function1[-A, +R] {
	def apply(x: A): R
	def isDefinedAt(x: A): Boolean
	}
	*/
  /*Partial Function Objects
	If the expected type is
	a 'PartialFunction',
	the Scala compiler will expand*/
  //{ case "ping" => "pong" }
  //as follows:
  val f1 = new PartialFunction[ String, String ] {
    def apply( x: String ) = x match {
      case "ping" => "pong"
    }

    def isDefinedAt( x: String ) = x match {
      case "ping" => true
      case _      => false
    }
  }                                               //> f1  : PartialFunction[String,String] = <function1>
  
  f1( "ping" ) // true                            //> res3: String = pong
  f1.isDefinedAt( "ping" ) // true                //> res4: Boolean = true
  /*f1( "pong" ) causes exception*/
  f1.isDefinedAt( "pong" ) // false               //> res5: Boolean = false
  
  /*
	Exercise
	Given the function*/
  val f2: PartialFunction[ List[ Int ], String ] = {
    case Nil            => "one"
    case x :: y :: rest => "two"
  }                                               //> f2  : PartialFunction[List[Int],String] = <function1>
  /*
	What do you expect is the result of
	*/
	f2(Nil)                                   //> res6: String = one
	f2(List( 1, 2, 3 ))                       //> res7: String = two
  f2.isDefinedAt( List( 1, 2, 3 ) )               //> res8: Boolean = true
  /*
	O true
	O falseExercise(2)
	*/
  /*
  How about the following variation:*/
  val g: PartialFunction[ List[ Int ], String ] = {
    case Nil => "one"
    case x :: rest =>
      rest match {
        case Nil => "two"
      }
  }                                               //> g  : PartialFunction[List[Int],String] = <function1>
  /*not check inner 'match'
  only one outer level considered
  so no garanty on function defined range of values*/
  g.isDefinedAt( List( 1, 2, 3 ) )                //> res9: Boolean = true
  /*
	gives:
	O true
	O false
	*/
}