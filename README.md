# throff
the Throff programming language

    go get -v github.com/donomii/throff

Throff is a dynamically typed, late binding, homoiconic, concatenative programming language.  It has all the features of a modern language - closures, lexical scopes, tail call optimisations, and continuations. 

It has an optional type system, and everything is a function, even language constructs like IF and FOR, which can be replaced and extended with your own versions.  It uses immutable semantics wherever possible to provide safe and secure threading and continuations.  There is almost no lexer/tokeniser, and no parser in the traditional sense.  Commands are fed directly into the engine to be executed.  The programs are written //backwards//. 

Throff is still in development.  The basic language is complete and can be used for minor tasks e.g. text processing.  However things like errors. continuations and someprogrammer-friendly features like arity tracking are still in progress.

## Program direction

Throff programs start at the _bottom_ and are evaluated backwards until they reach the top, where they finish.  Internally, the line breaks are removed and the program becomes one long line, which is evaluated strictly from right-to-left.


All Throff functions operate on the result of code to the right of the function.  The only exception is the TOK function, which forces the word to the left to become a TOKEN.

    PRINTLN Hello
    
evaluates Hello first (a string), then PRINTLN (a function).  PRINTLN doesn't care how its argument is made, so you can put any code there.

    PRINTLN ADD 1 2
    
Throff processes 2, then 1, then ADDs them together, then PRINTLNs them.  If a function doesn't consume any arguments or return any values, it effectively is invisible, and so you can place it anywhere.

    PRINTLN .S ADD 1 2
    
will call .S before calling PRINTLN.  .S prints the arguments to its right (i.e. the datastack).  Very handy for debugging!



## Goals

* To create a small, simple and portable interpreter (mostly complete)
* quick and effective access to platform libraries like graphics, databases, etc
* a simple and highly configurable language (good so far)
* the best interactive debugger, with rewind and undo functionality (promising so far)
* a familiar interface available everywhere
* to minimise the use of explicit typing where possible


## Throff datatypes


Throff datatypes are still a work in progress, as I come to understand the most
effective ways to structure them.  At the moment, there are the following
datatypes, with their literal syntax:

* Boolean - TRUE, FALSE
* String -  ->STRING [ This is a string ]
* Token - ANY SINGLE WORD LIKE THIS ,INCLUDING PUNCTUATION [ ] , .
* Array - ->ARRAY [ one two three four ] or A[ one two three four ]A 
* Code - ->FUNC [ PRINTLN Hello ]
* Lambda - [ PRINTLN Hello ]
* Hash - H[ key => value , key => value ]H
* Wrapper - No literal syntax

Note that under the hood,  arrays, lambdas and codes are all the same thing,
just with different flags to tell the interpreter what to do when it
encounters them. 

## String Representations

Throff is homoiconic, which in this case means that all its data structures have explicit string representations.  Every Throff data structure can be used as a string.  So a simple way to compare nested arrays is to compare their string representations:

    EQUAL 	->STRING ARRAY1		->STRING ARRAY2
    
hashes should work in a similar manner.

Native wrappers usually will not work this way, since it is not possible to make a string representation for something like a database handle.  Generally they will have a descriptive string that might be unique for some things (like filehandles).

## Symbol Representations

Symbol representations are similar to string representations, but they contain the exact commands needed to recreate the data structure.  The symbol representation is not necessarily homoiconic, instead it is a sequence of commands that, when run by Throff, will recreate the data structure.  This can potentially contain commands to re-open files and network sockets, or do other complex calls.

## The Datatypes in Detail

### Boolean

Booleans are created with TRUE, FALSE and EQUAL.  They only matter in an IF function.

### Strings and Tokens

Strings and tokens are treated exactly the same, it's just that tokens are created by the parser, usually for
function names etc, while strings are created from TOKENs or directly by reading from a socket or file.

Tokens may be forced explicitly with the TOK command, which is the only command in Throff
that acts on arguments to its _left_.

    Hello! TOK
    
will force a token containing Hello! onto the datastack.

Almost everything in throff has a string representation, and wherever possible,
throff acts on strings and strings alone.  Every datatype except WRAPPER may be
coerced into a STRING, just by using a function that expects a string, like
PRINT or STRING-JOIN.

### Numbers

All numbers are kept as strings right up to the moment they are used in
a numeric operation, then the native string to number converter is called.  As a
result, arithmatic will be very slow, right up to the point where I add JIT/PIC,
and then it will be very fast.

If at any point you find yourself wondering "Is this variable a number or a
string?", the answer is very simple:  it's a string.

### Arrays

Arrays are kept as native arrays, and are created with NEWARRAY.  Under the
hood, they are the same native structure as CODE and LAMBDA.  All arrays that
don't contain a WRAPPER have a string representation.  This string is usually
calculated on-the-fly, so you don't have to worry wasting memory on the string
component. 

Arrays may be converted to LAMBDAs with ->LAMBDA or UNFUNC, and to CODEs with ->CODE.  If you do
this, the newly formed function will run in the same namespace that it was
defined in.  You can change the environment it runs in with SETENVIRONMENT.

### Lambdas

A lambda is a function that can be passed around like data.  Lambdas are created
with LAMBDA [ ].  Lambdas are activated by CALL.

    CALL LAMBDA [ PRINTLN Hello ]

will result in

    Hello


### Code

Functions are kept as native arrays, and are created with the [ ] characters.
Note that because CODE is a datatype, any variable holding a function (that is,
a CODE) immediately becomes a function, like this:

    DEFINE say_hello => [ PRINTLN Hello ]
    SET a = say_hello
    a

will output

    Hello

This means that any attempt to use functions as arguments to other functions will explode in your face.  For instance

	MAP PRINT [ 1 2 3 4 ]

will print out 
	[ 1 2 3 4 ]
instead of 1234.


If you want to pass functions around for higher-order programming, you will need
LAMBDAs.  CODEs can be converted to LAMBDAs like this:

    SET my_lambda = LAMBDA GETFUNCTION PRINT TOK

This will get the print function and store it in my_lambda

You can call a CODE or a LAMBDA with CALL.

    CALL my_lambda


CODEs can be converted to arrays like this:

    BIND my_array => ARRAY GETFUNCTION some_function TOK

CODEs can be converted to strings like this:

    BIND my_string => STRING GETFUNCTION some_function TOK

### Hashes

Hashes are kept as native hashes, and are created with NEWHASH.  You can get
values with GETHASH/HASHGET and set values with SETHASH/HASHSET.

You can get a list of the keys with KEYS.

The string representation of a throff hash is generated on the fly and not
stored.  The string representing the hash will be a series of throff commands
that will rebuild the hash when EVALed.  Key order is not guaranteed unless the
underlieing implementation provides for it.

### Wrappers

Wrappers manage native datastructures.  There are no guarantees or guidelines
for access to wrappers.  Wrappers are typically returned by automatically
generated code that provides access to lower-level functionality.

Wrappers usually won't have a string representation.  If a wrapper is used as a
string, throff will attempt to print the code that was used to create the  native structure, or just throw an error.

Since throff uses the string representation of anything as the hash key,
wrappers should not be used as hash keys, unless you are very sure that the
string representation will behave correctly.


## Function Reference

THIN function
	THIN converts a function into a THIN function, which has no lexical environment - it shares its parents' lexical scope

MACRO
	MACRO converts a function into a MACRO.  MACROs have no lexical environment - they use the same environment as the caller (dynamic scope).

	WITH array FROM hash

	Inserts hashkeys into the current namespace
	
	WITH [ a b c ] FROM H[ a 1 b 2 c 3 ]H

	Loads the requested keys from the hash and puts them in the current environment.  
	
	Updating the variables will not update the hash nor vice versa.

	Parameters: 
	
	array 	- A list of hash keys that will become variable names
	hash		- Some data that you want to access as variables

	Example: WITH [ ips dates paths ] FROM http_requests
	
	is equivalent to 
	
	DEFINE ips 	=> GETHASH ips http_requests
	DEFINE dates => GETHASH dates http_requests
	DEFINE paths => GETHASH paths http_requests
	
REPEAT n function

	Calls the function n times.  
	
	Parameters:
	
	function	- The function must take no arguments and return no values (i.e. it is called for its side effects)
	
	Example: REPEAT 10 [ p Hello World ; ]
	
	See Also:
	
	<MAP>, <FOLD>, <RANGE>
	
THREAD function

	Starts a new thread to run function.  A clone of the current interpreter is used for the the new thread.  Due to Throff's immutable semantics, the new thread will not be able to update values in the old thread.  However this protection does not work for outside connections or libraries.  If both the old and new threads attempt to write to the same file handle, or read from the same network socket, corruption will occur.
	
	Parameters:
	
	function 	- The function to run in the new thread.  It must take no arguments and return no values
	
	Example: THREAD [ p Hello World ; ]



DEFINE name => value

	Sets the variable name to value
	
	
	Parameters:
	
	name	- A variable name that will be bound in the current namespace.
	value	- Any value or function 
	
	Description:
	
	DEFINE sets a variable.  name does not need to be quoted, so long as you remember to put the => operator afterwards.  
	
	value  function definition must be a function definition (LAMBDA or CODE), and name will become a function.  Any time name appears in the program, it will automatically activate.  This can cause some nasty bugs, for example:
	
	DEFINE print_code => [
		p Code is aFunc ;
		
		ARG aFunc =>
	]
	
	if you pass a function, you will get, at best, a crash
	
	print_code ->FUNC [ ADD ]
	
	> ERROR: read on empty stack
	
	Instead of printing out ADD, the program crashes because the variable aFunc became a function that tries to add the next two arguments, in this case, the letter ';' and the top of the stack.  To avoid this mishap, you will need to typecast the arguments to your function:
	
	ARG aFunc => ->LAMBDA
	ARG aFunc => ->STRING
	ARG aFunc => ->ARRAY
	
	or you can use GETFUNCTION to safely get the value of aFunc
	
	GETFUNCTION aFunc TOK

	Returns: nothing
	
	See Also:
	
	<BIND>,<REBIND>,<TOK>, <GETFUNCTION>, <ARG>, <CALL>->LAMBDA


ARG name =>

	ARG binds an argument to a name.  It is currently an alias to BIND.  Please read the notes on BIND to understand how to use ARG.
	
	Example:
	
	DEFINE greet => [ 
		p Hello NAME ;
		
		ARG NAME =>
	]
	
	greet [ Bob ]
	
	> Hello Bob
	
	See Also:
	
	<DEFINE>,<BIND>,<REBIND>

BIND name => value
	
	Variables are created with the BIND command. 


	PRINTLN x
	BIND x => 10

	Description
	 
	Bindings are almost immutable - you can only modify bindings in your current scope.  Read the Scoping section for more details.

REBIND name => new value

	Overwrites an existing binding
	
	Example
	
	REBIND x => 20
	
	Description
	
	The variable must have been created already with BIND, before it can be rebound
	
	Note that no matter what happens, the change is only visible inside the current scope.


TOK

	TOK quotes the word to its left
	
	PRINTLN TOK
	
	Out of all the functions in Throff, TOK is the only function that can affect anything to the left.  TOK is used to quote function and variable names, preventing them from being resolved to their values. => and = are aliased to TOK, and thus quote the variable name to their left.  This is how you can assign to variable names, without the variable resolving to its stored value.
	
	Example:
	
	PRINTLN TOK
	
	Stack top: PRINTLN
	
	Instead of printing TOK, TOK pushes the TOKEN "PRINTLN" onto the stack.
	
	Description:
	
	While Throff is bootstrapping, TOK is used to quote function names when they are being defined.
	
	DEFINE TRUE TOK [ EQUAL 1 1 ]
	
	TOK is useful when you want to retrieve a function value.  If you just use the function name, it will activate.  So if you try to rename PRINTLN
	
	DEFINE printline => PRINTLN
	
	> ERROR: read past end of stack
	
	it will fail because PRINTLN will activate.  To get the function behind PRINTLN
	
	DEFINE printline => GETFUNCTION PRINTLN TOK
	
	See Also:
	
	<GETFUNCTION>

CALL function

	Calls a function or a lambda
	
	Call activates a function, which you would typically get from GETFUNCTION

	Example:
	
	CALL [ p Hello World ; ]
	CALL GETFUNCTION PRINTLN TOK
	
	DEFINE myHello => ->LAMBDA [ p Hello my world ; ]
	
	CALL myHello
	
	See Also:
	
	<GETFUNCTION>
	
->LAMBDA
	
	Converts a CODE or ARRAY into a LAMBDA
	
	All Throff functions are of type CODE.  Any variable containing a CODE becomes a function, and will activate any time the variable name appears.  This makes function arguments difficult to deal with, because if someone passes your function a CODE, each time you refer to that CODE, you will need to write
	
	GETFUNCTION argname TOK
	
	this is too much typing and too easy to forget, so instead you should convert the CODEs to LAMBDAs.  Lambdas are identical to CODE in every way, except that to activate them, you need to use CALL

	Internally throff examines each word of the program in turn.  If the word is of type CODE, throff activates it immediately.  If the word is of type LAMBDA, throff pushes it onto the stack.

	Example:
	
	CALL myHello
	
	See Also:
	
	<GETFUNCTION>

->STRING

	Converts anything into a STRING.
	
	In Throff, almost everything has a string representation.  The only exceptions are WRAPPERs around internal data structures, and where possible, these will have some kind of descriptive string.  ->STRING will extract the string representation of any native data structure recursively, so calling it on a HASH or ARRAY might result in a very large string.
	
->ARRAY

	Converts a CODE or LAMBDA into an ARRAY
	
	CODEs, LAMBDAs and ARRAYs use the same internal data structure, and so can be used interchangably.  The only difference is how the interpreter treats them at certain times.  The bootstrap code uses this feature heavily, and is full of lines like 
	
	Example:
	
	PRINTLN ->ARRAY [ Starting bootstrap ]
	

### Math Functions

ADD number number

	Adds two numbers

	Example
	
	ADD 2 3
	
SUB number number
	Subtract two numbers
	
	Example
	
	SUB 5 3
	
MULT number number
	Multiplies two numbers
	
	Example
	
	MULT 3 4
	
DIVIDE number number
	Divides two numbers
	
	Example
	
	DIVIDE 10 9
	
MODULO number number
	Returns a modulo b
	
	Example
	
	MODULO 100000 3
	
LN number
	Natural logarithm
	
	
	

### Array functions

NEWARRAY
	Create a new array
	
ARRAYPUSH array item
	
	Pushes item onto the end of array.  Returns the new array
	
	Example
	
	REBIND myArray => ARRAYPUSH myArray [ hello ]
	
POPARRAY array
	Pops an item off the end of the array
	
	Returns
		item 	- the popped item
		array	- the new array, missing the final item
	
	Example
	
	REBIND myArray => REBIND dataItem => POPARRAY myArray
	
	Description
	
	POPARRAY returns two values: the item from the end of the array, and a new array, missing the final item. The original array is not affected!
	
SHIFTARRAY array
	As for POPARRAY, but the other end
	
UNSHIFTARRAY item array
	As for pusharray, but the other end
	
GETARRAY index array
	Returns the item in array at position index
	
	Example
	
	BIND 3rdItem => GETARRAY 2 myArray
	
	
	
