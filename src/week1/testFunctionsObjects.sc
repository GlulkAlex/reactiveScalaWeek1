package week1

object testFunctionsObjects {
  /*
  Representation of JSON in Scala*/
  //*or 'trait'
  abstract class JSON
  case class JSeq( elems: List[ JSON ] ) extends JSON
  /*using
	type JBinding = (String, JSON)*/
  case class JObj( bindings: Map[ String, JSON ] ) extends JSON
  case class JNum( num: Double ) extends JSON
  case class JStr( str: String ) extends JSON
  case class JBool( b: Boolean ) extends JSON
  case object JNull extends JSON
  /*
  Case Blocks
  Question: What's the 'type' of:*/
  //*{ case (key, value) => key + ": " + value }
  /*Taken by itself,
  the expression is not typable.
  We need to
  prescribe an expected 'type'.
  The 'type' expected
  by 'map' on the last slide is*/
  //*JBinding => String,
  /*the 'type' of 'functions' from
  'pairs' of
  'strings' and 'JSON' data to 'String'.
  where
  'JBinding' is*/
  type JBinding = ( String, JSON )
  /*
  Pattern Matching
  Here's
  a method that
  returns the string representation JSON data:*/
  def show( json: JSON ): String = json match {
    case JSeq( elems ) =>
      "[" + ( elems map show mkString ", " ) + "]"
    case JObj( bindings ) =>
      val assocs = bindings map {
        case ( key, value ) => "\"" + key + "\": " + show( value )
      }
      /*return value*/
      "{" + ( assocs mkString ", " ) + "}"
    case JNum( num ) => num.toString
    case JStr( str ) => '\"' + str + '\"'
    case JBool( b )  => b.toString
    case JNull       => "null"
  }                                               //> show: (json: week1.testFunctionsObjects.JSON)String
  /*
  Functions Are Objects
	In Scala,
	every 'concrete' 'type' is
	the type of some 'class' or 'trait'.
	The 'function' 'type' is no exception.
	A type like*/
  //*JBinding => String
  /*is just
	a shorthand for*/
  //*scala.Function1[JBinding, String]
  /*where 'scala.Function1' is
	a 'trait' and
	'JBinding' and 'String' are
	its 'type' arguments.
	*/
  /*
  The Function1 Trait
  Here's an outline of trait Function1:*/
  //trait Function1[-A, +R] {
  // def apply(x: A): R
  //}
  /*The pattern matching block*/
  //*{ case (key, value) => key + ": " + value }
  /*expands to the Function1 instance*/
  val keyToValueBlock = new Function1[ JBinding, String ] {
    def apply( x: JBinding ) = x match {
      case ( key, value ) => key + ": " + show( value )
    }
  }                                               //> keyToValueBlock  : week1.testFunctionsObjects.JBinding => String = <functio
                                                  //| n1>
  val data1 = JObj( Map(
    "type" -> JStr( "home" ), "number" -> JStr( "212 555-1234" )
  ) )                                             //> data1  : week1.testFunctionsObjects.JObj = JObj(Map(type -> JStr(home), num
                                                  //| ber -> JStr(212 555-1234)))
  
  keyToValueBlock(data1.bindings.head)            //> res0: String = type: "home"
  keyToValueBlock(data1.bindings.last)            //> res1: String = number: "212 555-1234"
  /*
  Subclassing Functions
  One nice aspect of
  functions being traits is
  that we can
  'subclass' the function type.
  For instance,
  maps are
  functions from 'keys' to 'values':*/
  //*trait Map[Key, Value] extends (Key => Value) ...

}