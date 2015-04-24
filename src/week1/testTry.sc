package week1

object testTry {
  /*
  Another type: 'Try'
  In the later parts of this course
  we will need
  a type named 'Try'.
  'Try' resembles 'Option', but
  instead of
  'Some/None' there is
  a 'Success' case
  with a 'value' and
  a 'Failure' case
  that contains an 'exception':*/
  abstract class TryM[ +T ] {
    def flatMap[ U ]( f: T => TryM[ U ] ): TryM[ U ] = this match {
      case SuccessM( x ) =>
        //*uses Java 'try' from JVM
        //*this warper allows to prevent leaking non fatal exception
        //*outside of try computation
        try f( x )
        catch {
          //type mismatch;
          //found   : Throwable
          //required: Exception
          case NonFatalM( ex ) => FailureM( ex )
        }
      case fail: FailureM => fail
    }
    def map[ U ]( f: T => U ): TryM[ U ] = this match {
      case SuccessM( x )  => TryM( f( x ) )
      case fail: FailureM => fail
    }
  }
  case class SuccessM[ T ]( x: T ) extends TryM[ T ]
  //* 'Nothing' may indicates that
  //* computation ends ubnormally
  //* for example 'infinite loop'
  case class FailureM( ex: Throwable/*Exception*/ ) extends TryM[ Nothing ]
  /*
	'Try' is used to
	pass results of computations that can
	fail with
	an 'exception'
	between
	'threads' and computers.*/
  /*
  Creating a 'Try'
  You can
  wrap up an arbitrary computation in a 'Try'.*/
  /** {{{
    * Try(expr) // gives Success(someValue) or Failure(someException)
    * }}}
    */
  /*Here’s
  an implementation of Try:*/
  //*? companion object ?
  object TryM {
    //*using by name for parameter
    //*because no certanty on result
    //*else why use 'try' ?
    def apply[ T ]( expr: => T ): TryM[ T ] =
      try SuccessM( expr )
      catch {
        //* most exception are non fatal
        //* & have reason to propagete computation falue result outside
        case NonFatalM( ex ) => FailureM( ex )
      }
  }

  object NonFatalM {
    /** Returns true if
      * the provided `Throwable` is
      * to be considered 'non-fatal', or
      * 'false' if
      * it is
      * to be considered 'fatal'
      */
    def apply( t: /*Exception*/Throwable ): Boolean = t match {
      // VirtualMachineError includes
      //OutOfMemoryError and
      //other fatal errors
      /* ? multiple choises ? */
      /*'|' Binary 'OR' Operator
	     copies a bit if
	     it exists in eather operand.*/
      case _: VirtualMachineError |
        _: ThreadDeath |
        _: InterruptedException |
        _: LinkageError /*_: ControlThrowable*/ => false
      case _ => true
    }
    /** Returns Some(t) if
      * NonFatal(t) == true,
      * otherwise None
      */
    def unapply( t: Throwable ): Option[ Throwable ] =
      if ( apply( t ) ) Some( t ) else None
  }
  /*
  Composing 'Try'
  Just like with
  'Option',
  Try-valued computations can be
  composed in
  'for expresssions'.*/
  /** {{{
    * for {
    * x <- computeX
    * y <- computeY
    * } yield f(x, y)
    * }}}
    */
  /*
	If 'computeX' and 'computeY' succeed
	with results
	'Success(x)' and 'Success(y)',
	this will return
	'Success(f(x, y))'.
	If
	either computation fails
	with an exception 'ex',
	this will return
	'Failure(ex)'.
  */
  /*So,
  for a 'Try' value 't'*/
  /** {{{
  t map f == t flatMap (x => Try(f(x)))
  == t flatMap (f andThen Try)
  }}}*/
  /*
  It turns out
  the 'left unit law' for monad fails.*/
  //Try(expr) flatMap f != f(expr)
  /*
  Indeed
  the left-hand side will never raise
  a 'non-fatal' exception whereas
  the right-hand side will raise
  any 'exception' thrown by
  'expr' or 'f'.
  Hence,
  'Try' trades one 'monad' law for
  another law which is
  more useful in
  this context:
    An expression composed from
    ‘Try‘,
    ‘map‘,
    ‘flatMap‘ will never
    throw a 'non-fatal exception'.
  Call this
  the "bullet-proof" principle.*/
  
  println( "Welcome to the Scala worksheet" )
}