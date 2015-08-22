# throff
the Throff programming language


Throff is a dynamically typed, late binding, homoiconic, catenative programming language.  It has all the features of a modern language - closures, lexical scopes, tail call optimisations, and continuations. 

It has an optional type system  Everything is a function, even language constructs like IF and FOR, which can be replaced and extended with your own versions.  It uses immutable semantics wherever possible to provide safe and secure threading and continuations.  There is almost no lexer/tokeniser, and no parser in the traditional sense.  Commands are fed directly into the engine to be executed.  The programs are written //backwards. //Sometimes they even work.

===== Goals =====

* To create a small, simple and portable interpreter (mostly complete)
* quick and effective access to platform libraries like graphics, databases, etc
* a simple and highly configurable language (good so far)
* the best interactive debugger, with rewind and undo functionality (promising so far)
* a familiar interface available everywhere
