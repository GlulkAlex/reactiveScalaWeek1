package week1

object testMonad {
  /*
  What is a Monad?
  A 'monad' 'M' is
  a 'parametric type' 'M[T]' with
  two operations,
  'flatMap' and
  'unit', that have to
  satisfy some laws.*/
  /*from:
  http://en.wikipedia.org/wiki/Monad_(functional_programming)
  Formally,
  a 'monad' consists of
  a 'type constructor' 'M' and
  two 'operations',
  'bind' and
  'return'
  (where 'return' is
  often also called 'unit'):
  */
  trait MonadM[ T ] {
    def flatMap[ U ]( f: T => MonadM[ U ] ): MonadM[ U ]
    def unit[ T ]( x: T ): MonadM[ T ]
  }
  /*
  In the literature,
  'flatMap' is more commonly called 'bind'.*/
  /*
  Examples of Monads
	>>List is
	 a monad with
	   unit(x) = List(x)
	>>Set is
		monad with
		  unit(x) = Set(x)
	>>Option is
		a monad with
		unit(x) = Some(x)
	>>Generator is
		a monad with
		  unit(x) = single(x)
	'flatMap' is
	an operation on each of these types,
	whereas unit in Scala is
	different for each monad.*/
  /*
	'Monads' and 'map'
    'map' can be defied
    for every monad as
    a combination of 'flatMap' and 'unit':*/
    //*where, 'm' stands for 'monad'
  /** {{{
    * m map f == m flatMap (x => unit(f(x)))
    * == m flatMap (f andThen unit)
    * }}}
    */
  /*
	Monad Laws
  To qualify as a 'monad',
  a 'type' has to
  satisfy three laws:
    >>Associativity:*/
  /** {{{
    * (m flatMap f) flatMap g == m flatMap (x => f(x) flatMap g)
    * }}}
    */
  /*>>>Left unit*/
  /** {{{
    * unit(x) flatMap f == f(x)
    * }}}
    */
  /*>>>Right unit*/
  /** {{{
    * m flatMap unit == m
    * }}}
    */
    //*Another 'domain' where
    //*also 'Associativity' law holds
    //*for simplear construction (form of 'monad')
    //*that does not 'bind' anithing as / is
    //*'Monoid', for example 'integers'
  /*
	 Checking Monad Laws
  Let us check the monad laws for Option.
  Here is 'flatMap' for Option:*/
  abstract class OptionM[ +T ] {
    def flatMap[ U ]( f: T => OptionM[ U ] ): OptionM[ U ] = this match {
      //constructor cannot be instantiated to expected type;
      //found   : Some[A]
      //required: week1.testMonad.OptionM[T]
      case Some( x ) => f( x )
      case None      => None
    }
  }

  case class Some[ +A ]( x: A ) extends OptionM[ A ] {
    def isEmpty = false
    def get = x
  }

  case object None extends OptionM[ Nothing ] {
    def isEmpty = true
    def get = throw new NoSuchElementException( "None.get" )
  }
  /*
  Checking the 'Left Unit Law'
  Need to show: */
  /** {{{
    * Some(x) flatMap f == f(x)
    * //expanding 'flatMap' definition
    * Some(x) flatMap f == Some(x) match {
    * case Some(x) => f(x)
    * case None => None
    * } == f(x)
    * }}}
    */
  /*
  Checking the 'Right Unit Law'
  Need to show: opt flatMap Some == opt
  where:
  'opt' as 'Option' value
  'Some' as 'unit' constructor
  */
  /** {{{
    * opt flatMap Some
    * == opt match {
    * case Some(x) => Some(x)
    * case None => None
    * }
    * == opt
    * }}}
    */
  /*
  Checking the Associative Law
  Need to show:
  (opt flatMap f) flatMap g == opt flatMap (x => f(x) flatMap g)*/
  /** {{{
    * ( opt flatMap f ) flatMap g
    * == ( opt match {
    * case Some(x) => f(x) case None => None } ) match {
    * case Some(y) => g(y) case None => None }
    * //nested 'match' representation
    * == opt match {
    * case Some(x) =>
    * f(x) match { case Some(y) => g(y) case None => None }
    * case None =>
    * None match { case Some(y) => g(y) case None => None }
    * }
    * == opt match {
    * case Some(x) =>
    * f(x) match { case Some(y) => g(y) case None => None }
    * case None => None
    * }
    * == opt match {
    * case Some(x) => f(x) flatMap g
    * case None => None
    * }
    * == opt flatMap (x => f(x) flatMap g) //QED - quod erat demonstrandum
    * }}}
    */
  /*
  Signiﬁcance of the 'Laws' for 'For-Expressions'
  We have seen that
  'monad-typed' expressions are
  typically written as
  'for expressions'.
  What is
  the significance of
  the laws with respect to this?
  1. Associativity says
  essentially that
  one can "inline" nested for expressions:*/
  /** {{{
    * for (
    * y <- (for (
    * x <- m
    * y <- f(x) ) yield y )
    * z <- g(y) ) yield z
    *
    * == for (x <- m
    * y <- f(x)
    * z <- g(y)) yield z
    * }}}
    */
  /*
  2. Right unit says:*/
  /** {{{
    * for (x <- m) yield x
    * == m
    * }}}
    */
  /*
  3. 'Left unit' does not have an analogue for 'for-expressions'.*/
  //*monad laws give
  //*a justification for
  //*refactoring for-expressions
  /*
  Conclusion
  We have seen that
  for-expressions are
  useful not only for collections.
  Many other types also define
  'map',
  'flatMap', and
  'withFilter' operations and
  with them
  'for-expressions'.
  Examples:
    'Generator',
    'Option',
    'Try'.
  Many of the types defining 'flatMap' are
  'monads'.
  (If they also define 'withFilter',
  they are called
  "monads with zero").
  //more rich category then 'monads'
  The three 'monad laws' give
  useful guidance in
  the design of library APIs.
  */

  println( "Welcome to the Scala worksheet" )     //> Welcome to the Scala worksheet

}