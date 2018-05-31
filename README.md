[![Build Status](https://travis-ci.org/donomii/throff.svg?branch=master)](https://travis-ci.org/donomii/throff)
[![GoDoc](https://godoc.org/github.com/donomii/throff?status.svg)](https://godoc.org/github.com/donomii/throff)

# throff

## Get it

the Throff programming language

    go get -v github.com/donomii/throff
    go build ...throff

 Or download a [precompiled binary](https://github.com/donomii/throff/releases)

## Use it

### Run-and-quit

	throff ADD 1 1
	2

### Interactive shell

	> throff
	Welcome to the THROFF command shell v0.1. Type HELP for help.
	Throff »

### Run a program

	throff -f properdiv.thr

## Throff is

Throff is a dynamically typed, late binding, homoiconic, concatenative programming language, taking inspiration from Forth and Joy.  It has all the features of a modern language - [closures, lexical scopes](http://praeceptamachinae.com/post/throff_variables.html), [tail call optimisations](http://praeceptamachinae.com/post/throff_tail_call_optimisation.html), currying, and continuations.

It supports actors, and everything is a function, even language constructs like IF and FOR, which can be replaced and extended with your own versions.  It uses immutable semantics wherever possible to provide safe and secure threading and continuations.  There is almost no lexer/tokeniser, and no parser in the traditional sense.  Commands are fed directly into the engine to be executed.  The programs are written _backwards_. 

Throff is still in development.  The basic language is complete and can be used for minor tasks e.g. text processing.  However things like errors and some programmer friendly features like arity tracking are still in progress.

## Program flow

Throff programs start at the _bottom_ and are evaluated backwards until they reach the top, where they finish.  Actually, the line breaks are removed and the program becomes one long line, which is evaluated from right-to-left.


All Throff functions (bar one) operate on the result of code to the right of the function.  A program is one long chain of function calls, each one transforming the output of the function to the right.

    PRINTLN Hello

evaluates Hello (a string), then PRINTLN (a function).  PRINTLN prints the result of the function to its right, so you can replace Hello with anything, like ADD.

    PRINTLN ADD 1 2

Throff processes 2, then 1, then ADDs them together, then PRINTLNs the result.  If a function doesn't consume any arguments or return any values, it effectively is invisible, and so you can place it anywhere.

    PRINTLN ADD .S 1 2

will call .S before calling ADD.  .S prints the arguments to its right (i.e. the datastack).  Very handy for debugging!

## Goals

* To create a small, simple and portable interpreter (mostly complete)
* To design a language that builds itself from basic principles to advanced language constructs (going nicely)
* quick and effective access to platform libraries like graphics, databases, etc (not so good)
* a simple and highly configurable language (good so far)
* the best interactive debugger, with rewind and undo functionality (possible but not implemented well)
* Support advanced language features like first-class continuations (mostly complete)
* a familiar interface available everywhere
* to minimise the use of explicit typing where possible (while still providing useful typing)


## Throff datatypes

Throff datatypes are still a work in progress, as I come to understand the most effective ways to structure them.  At the moment, there are the following datatypes, with their literal syntax:

### Throff literals

* Boolean -     TRUE, FALSE
* String -      ->STRING [ This is a string ]
* Token -       ANY SINGLE WORD LIKE THIS, INCLUDING PUNCTUATION [ ] , .
* Array -       ->ARRAY [ one two three four ] or A[ one two three four ]A
* Code -        ->FUNC [ PRINTLN Hello ]
* Lambda -      [ PRINTLN Hello ]
* Hash -        H[ key => value  key => value ]H
* Wrapper -     No literal syntax
* Bytes -       No literal syntax

Note that under the hood,  arrays, lambdas and codes are almost the same thing, just with different flags to tell the interpreter what to do when it encounters them.

## String Representations

Throff is homoiconic, which in this case also means that all its data structures have explicit string representations.  Every Throff data structure can be used as a string.  So a simple way to compare nested arrays is to compare their string representations:

    EQUAL 	->STRING ARRAY1		->STRING ARRAY2

hashes work in a similar manner.

Native wrappers usually will not work this way, since it is not possible to make a string representation for something like a database handle.  They have a descriptive string that might be meaningful for some things (like filehandles), but usually not.

## Symbol Representations

Symbol representation is used for manipulating source code.  Requesting symbol output of data returns the commands needed to recreate the data, rather than their string representation.  Symbol output is most useful for EVAL, and can be used to send program code over network sockets.

FIXME rename "symbol representation" to something less confusing.

## The Datatypes in Detail

### Boolean

Booleans are created with TRUE, FALSE and EQUAL.  They are only used by the IF function, and the usual logical functions.

### Strings and Tokens

Strings and tokens are treated exactly the same, except that strings require quotes around them, and tokens are printed raw.  This matters when trying to print out a data structure (or code) to be evaluated later. Tokens are usually created by the parser, usually for function names etc, while strings are created from TOKENs or directly by reading from a socket or file.

Almost everything in throff has a string representation, and wherever possible,
throff acts on strings and strings alone.  Every datatype except WRAPPER may be
coerced into a STRING with ->STRING, or by using a function that expects a string, like
PRINT or STRING-JOIN.

### Variable lookup

Variable names in throff are actually tokens.  When throff finds a token, it tries to look it up in the local symbol table to find the value.  If it has a value (if it is a variable), throff replaces the variable name with the value, then continues processing.

Because a variable name is automatically replaced with its value, it is impossible to grab the variable name (the token) itself, which is necessary for writing macros.  Using the _TOK_ function prevents variable lookup and gives you the token to its _left_.

The TOK command, is the only command in Throff
that acts on arguments to its _left_.

    Hello! TOK

will force a token containing Hello! onto the datastack.

### Numbers

All numbers are kept as strings right up to the moment they are used in
a numeric operation, then the native string to number converter is called.  As a
result, arithmatic will be very slow, right up to the point where I add JIT/PIC,
and then it will be very fast.

If at any point you find yourself wondering "Is this variable a number or a
string?", the answer is:  it's a string.

### Arrays

Arrays are kept as native arrays, and are created with NEWARRAY.  Under the
hood, they are the same native structure as CODE and LAMBDA.  All arrays that
don't contain a WRAPPER have a string representation.  This string is usually
calculated on-the-fly, so you don't have to worry wasting memory on the string
component.

Arrays may be converted to LAMBDAs with ->LAMBDA, and to CODEs with ->CODE.  If you do
this, the newly formed function will run in the same namespace that it was
defined in.  You can change the environment it runs in with SETENVIRONMENT.

### Lambdas

A lambda is a function that can be passed around like data.  Lambdas are created
with [ ].  Lambdas are activated by CALL.

    CALL [ PRINTLN Hello ]

will result in

    Hello

LAMBDAs are the fundamental component of Throff.  Using the [ ] brackets always creates
a LAMBDA, no matter the context.  For instance, there is nothing special about the IF function - 
it is just a function that takes 3 LAMBDA arguments.

LAMBDAs can be converted to arrays

    BIND my_array => ARRAY some_lambda

LAMBDAs can be converted to strings like this:


    BIND my_string => STRING a_lambda
    
    or for CODE
    
    BIND my_string => STRING GETFUNCTION some_function TOK


### Code

Functions are kept as native arrays, and are created with 

    FUNC [ ]

Throff keeps track of executable code by marking the data itself, not the variable.  Because CODE is a datatype, any variable holding a a CODE immediately becomes a function, like this:

    a
    BIND a => GETFUNCTION say_hello TOK
    DEFINE say_hello => FUNC [ PRINTLN Hello ]

will output

    Hello

This means that any attempt to use functions as arguments to other functions will explode in your face.  For instance

    MAP PRINT [ 1 2 3 4 ]

will print out

    [ 1 2 3 4 ]

instead of 1234.


If you want to pass functions around for higher-order programming, you will need
LAMBDAs.  CODEs can be converted to LAMBDAs like this:

    BIND my_lambda => LAMBDA GETFUNCTION PRINT TOK

but by far the easiest way to do this is to wrap the function

    MAP my_lambda [ 1 2 3 4 ]
    BIND my_lambda = [ PRINT ]
    

This will get the PRINT function and store it in my_lambda

You can call a CODE or a LAMBDA with CALL.

    CALL GETFUNCTION PRINT TOK [ Hello ]
    
    CALL my_lambda


CODEs can be converted to arrays:

    BIND my_array => ARRAY GETFUNCTION some_function TOK

CODEs can be converted to strings:

    BIND my_string => STRING GETFUNCTION some_function TOK

### Hashes

Hashes are kept as native hashes, and are created with NEWHASH.  You can get
values with GETHASH/HASHGET and set values with SETHASH/HASHSET.

You can get a list of the keys with KEYS.

The string representation of a throff hash is generated on the fly and not
stored.  The string representing the hash will be a series of throff commands
that will rebuild the hash when EVALed.  Key order is not guaranteed unless the
underlying implementation provides for it.

Handling nested hashes is currently too difficult, I will be redesigning this soon.

### Wrappers

Wrappers manage native datastructures.  There are no guarantees or guidelines
for access to wrappers.  Wrappers are typically returned by automatically
generated code that provides access to lower-level functionality.

Wrappers usually won't have a string representation.  If a wrapper is used as a
string, throff will usually attempt to print the code that was used to create the wrapper, or just throw an error.

Since throff uses the string representation of data as a hash key,
wrappers should not be used as hash keys, unless you are very sure that the
string representation exists and is meaningful.

### Bytes

Bytes are a special type of wrapper, because Throff has some extra in functions for manipulating them.  Throff also provides some basic guarantees for working with bytes.  Throff will not move the bytes, so pointers into the bytes will remain valid.  However, you will need to make sure the bytes are not freed by the garbage collector by keeping a Throff binding to the bytes.

You can convert a throff value to bytes with ->BYTES, or make one with MMAPFILE.  You can get the length (in bytes) with LENGTH, read parts of the BYTES with GETBYTE, or set them with SETBYTE.

## Function Reference

#### THIN function

THIN converts a function into a THIN function, which has no private lexical environment - it shares its parents' lexical scope.

#### MACRO function

MACRO converts a function into a MACRO.  MACROs have no lexical environment at all - they use the same environment as the caller (dynamic scope).

#### WITH array FROM hash

Inserts hashkeys into the current namespace

	WITH [ a b c ] FROM H[ a 1 b 2 c 3 ]H

Loads the requested keys from the hash and puts them in the current environment.  

Updating the variables will not update the hash nor vice versa.  HASHes, like most other data types, are immutable.

##### Parameters:

-	array 	- A list of hash keys that will become variable names
-	hash	- Some data that you want to access as variables

	Example:

	WITH [ ips dates paths ] FROM http_requests

is equivalent to

	DEFINE ips 	=> GETHASH ips http_requests
	DEFINE dates => GETHASH dates http_requests
	DEFINE paths => GETHASH paths http_requests

#### REPEAT n function

Calls **function** n times.  

##### Parameters:

-	function	- The function must take no arguments and return no values (i.e. it is called for its side effects)

##### Example:

	REPEAT 10 [ p Hello World ; ]

##### See Also:

	MAP, FOLD, RANGE

#### THREAD function

Starts a new thread to run function.  A clone of the current interpreter is used for the the new thread.  Due to Throff's immutable semantics, the new thread will not be able to update values in the old thread.  However this protection does not work for anything external to the interpreter, like sockets, files or databases.  If both the old and new threads attempt to write to the same file handle, or read from the same network socket, corruption will occur.

Threads cannot communicate directly with each other, you need to use something like a QUEUE, or a third party library.

The ACTOR functions provide a convenient way to run a thread and communicate with other threads.

##### Parameters:

-	function 	- The function to run in the new thread.  It must take no arguments and return no values

##### Example:

	THREAD [ p Hello World ; ]



#### DEFINE name => value

Defines a function **name** with body **value**


##### Parameters:

- name	- A variable name that will be bound in the current namespace.
- value	- Any value or function

##### Description:

DEFINE creates a function.  **name** does not need to be quoted, so long as you remember to put the => operator afterwards.  

**value**  must be a function definition (LAMBDA or CODE), and **name** will become a function.  Any time **name** appears in the program, it will automatically activate.  This can cause some nasty bugs, for example:

	DEFINE print_code => [
		p Code is aFunc ;

		ARG aFunc =>
	]

if you use print_code on a function, you will get, at best, a crash

	print_code ->FUNC [ ADD ]

	> ERROR: read on empty stack

Instead of printing out ADD, the program crashes because the variable aFunc became a function that tries to add the next two arguments, in this case, the letter ';' and the top of the stack.  To avoid this mishap, you will need to typecast the arguments to your function:

	ARG aFunc => ->LAMBDA
	
or

	ARG aFunc => ->STRING

or

	ARG aFunc => ->ARRAY

or you can use GETFUNCTION to safely get the value of aFunc

	p Code is GETFUNCTION aFunc TOK ;

##### Returns
	
	nothing

##### See Also

	BIND, REBIND, TOK, GETFUNCTION, ARG, CALL, ->LAMBDA


#### ARG name =>

	ARG binds a function argument to **name**.  It is currently an alias to BIND.

##### Example:

	DEFINE greet => [
		p Hello NAME ;

		ARG NAME =>
	]

	greet [ Bob ]

	> Hello Bob

	See Also:

	DEFINE, BIND, REBIND

#### BIND name => value

	Variables are created with the BIND command.


	PRINTLN x
	BIND x => 10

    > 10

##### Description

	Bindings are almost immutable - you can only modify bindings in your current scope.  Read the Scoping section for more details.

#### REBIND name => new value

	Overwrites an existing binding

##### Example

	REBIND x => 20

##### Description

	The variable must have been created already with BIND, before it can be rebound

	Note that no matter what happens, the change is only visible inside the current (and lower) scope.


#### TOK

	TOK quotes the word to its left

	PRINTLN TOK

	Out of all the functions in Throff, TOK is the only function that can affect anything to the left.  TOK is used to quote function and variable names, preventing them from being resolved to their values. => and = are aliased to TOK, and thus quote the variable name to their left.  This is how you can assign to variable names, without the variable resolving to its stored value.

##### Example:

	PRINTLN TOK

	Stack top: PRINTLN

	Instead of printing TOK, TOK pushes the TOKEN "PRINTLN" onto the stack.

##### Description:

	While Throff is bootstrapping, TOK is used to quote function names when they are being defined.

	DEFINE TRUE TOK [ EQUAL 1 1 ]

	TOK is useful when you want to retrieve a function value.  If you just use the function name, it will activate.  So if you try to rename PRINTLN

	DEFINE printline => PRINTLN

	> ERROR: read past end of stack

	it will fail because PRINTLN will activate.  To get the function behind PRINTLN

	DEFINE printline => GETFUNCTION PRINTLN TOK
	
	Note that => is just syntactic sugar for TOK, as are several other symbols I can't recall right now.

##### See Also:

	GETFUNCTION

#### CALL function

	Calls a function or a lambda

	Call activates a lambda function, which are built with [ ] or fetched with GETFUNCTION

	Example:

	CALL [ PRINTLN [ Hello World ] ]
	CALL GETFUNCTION PRINTLN TOK [ Hello World ]

	DEFINE myHello => [ p Hello my world ; ]

	CALL myHello

	See Also:

	GETFUNCTION

#### ->LAMBDA

	Converts a CODE into a LAMBDA


FIXME move this discussion to another page

	All Throff functions are of type CODE.  Any variable containing a CODE becomes a function, and will activate any time the variable name appears.  This makes function arguments difficult to deal with, because if someone passes your function a CODE, each time you refer to that CODE, you will need to write

	GETFUNCTION argname TOK

	this is too much typing and too easy to forget, so instead you should convert the CODEs to LAMBDAs.  Lambdas are identical to CODE in every way, except that to activate them, you need to use CALL

	Internally throff examines each word of the program in turn.  If the word is of type CODE, throff activates it immediately.  If the word is of type LAMBDA, throff pushes it onto the stack.

	Example:

	CALL myHello

	See Also:

	GETFUNCTION

#### ->STRING

	Converts anything into a STRING.

	In Throff, almost everything has a string representation.  The only exceptions are WRAPPERs around internal data structures, and where possible, these will have some kind of descriptive string.  ->STRING will build the string representation of a data structure recursively, so calling it on a HASH or ARRAY might result in a very large string.

#### ->ARRAY

	Converts a CODE or LAMBDA into an ARRAY

	CODEs, LAMBDAs and ARRAYs use the same internal data structure, and so can be used almost interchangably.  The only difference is how the interpreter treats them at certain times, and CODE/LAMBDAs have a lexical environment.  

##### Example

	MAP [ PRINTLN ] ->ARRAY [ one two three four ]

  > one
  > two
  > three
  > four


### Math Functions

#### FLOOR number

	Discards everything to the right of the decimal point
	

#### ADD number number

	Adds two numbers

	Example

	ADD 2 3

#### SUB number number

	Subtract two numbers

	Example

	SUB 5 3

#### MULT number number

	Multiplies two numbers

	Example

	MULT 3 4

#### DIVIDE number number

	Divides two numbers

	Example

	DIVIDE 10 9

#### MODULO number number

	Returns a modulo b

	Example

	MODULO 100000 3

#### LN number

	Natural logarithm
	
#### SIN number

	Sine (radiians)

### String functions

#### GETSTRING n astring

Gets the *n*th letter of *astring*

#### SETSTRING n letter string

Sets the *n*th *letter* of *string*

#### SLICE-STRING start end string

Extracts and returns a substring of *string*, from *start* to *end*

#### STRING-CONCATENTE s1 s2

Returns a new string, which is s1 with s2 appended

#### STRING-JOIN separator array

Combines *array* into a single string, with *separator* placed between each element of *array*.

#### STRING-CONCATENTE* array

Returns a new string, which is all the elements of **array** concatenated together.

#### STRING-JOIN string array

Returns a new string, which is made of all the elements of **array** with **string** in between each element

Example

    STRING-JOIN , A[ HELLO WORLD ]A

    > HELLO,WORLD

#### PLURAL number string

Pluralises **string** by adding an s to the end if **number** is not equal to one.

Example

    PRINLN A[ 99 PLURAL 99 bottle of beer ]A

    > 99 bottles of beer

#### RUNSTRING string environment

Evaluates *string* inside *environment*.  You can get an environment from almost any throff type (except macros) using ENVIRONMENTOF.

You can get the current environment with

    ENVIRONMENTOF [ THIS ]

### Byte functions

Bytes provide direct memory access.  Unlike everything else in Throff, they are mutable by default.  They are also a good way to to corrupt memory and cause throff to crash.

#### ->BYTES

Converts any throff data into a BYTES.  If the input is a WRAPPER, then it will be used unchanged (and without being copied into a new memory location, which is what the other type converters do).  Any data other than WRAPPERs will be converted to a STRING, and then that STRING will be used as BYTES.

#### GETBYTE position bytes

Gets the byte at **position** from **bytes**.

##### Returns

A string, containing the ASCII representation of the byte.

FIXME should this be returning a string?

#### SETBYTE position value bytes

FIXME implement this

#### BYTE-LENGTH bytes

FIXME implement this



### Array functions

#### NEWARRAY

	Create an empty new array
	
	Note: You can also use the literal

	A[ ]A

#### ARRAYPUSH array item

	Pushes item onto the end of array.  Returns the new array

	Example

	REBIND myArray => ARRAYPUSH myArray [ hello ]

#### POPARRAY array

	Pops an item off the end of the array

	Returns
		item 	- the popped item
		array	- the new array, missing the final item

	Example

	REBIND myArray => REBIND dataItem => POPARRAY myArray

	Description

	POPARRAY returns two values: the item from the end of the array, and a new array, missing the final item. The original array is not affected!

#### SHIFTARRAY array

	As for POPARRAY, but the other end

#### UNSHIFTARRAY item array

	As for pusharray, but the other end

#### GETARRAY index array

	Returns the item in array at position index

	Example

	BIND 3rdItem => GETARRAY 2 myArray

#### EMPTY? array

Returns true if **array** has no elements.

#### REVERSE array

    Returns a reversed copy of **array**

#### CAR array

Returns the first element of **array**

#### CDR array

Returns a copy of **array**, with the first element removed

#### APPEND array1 array2

Returns a new array which is array1 with array2 appended to the end.


### HASHes (dictionaries)

#### NEWHASH

	Create a new hash
	
	Note: you can use the literal

	H[ ]H

#### HASHSET hash key value -> hash

	Sets **key** to **value**

##### Returns
		hash	- a new hash.  The old hash is unmodified

#### SETHASH key value hash -> hash

	Sets **key** to **value**

##### Returns
		hash	- a new hash.  The old hash is unmodified

#### KEYS hash -> array

##### Returns
		array	- the keys of the hash as an array

#### VALUES hash

##### Returns
		array	- The values of the hash, as an array

#### KEYVALS hash -> array

##### Returns
		array	- The keys and values "flattened" into an array

	Example

	KEYVALS H[ A 1 B 2 C 3 ]H

	-> A[ A 1 B 2 C 3 ]A

#### KEYS/VALS -> array, array

	Returns
		array	KEYS hash
		array	VALUES hash

#### HASHDELETE hash key

	Removes *key* from the hash

### Queues

Queues are thread safe FIFO queues, most useful for sending messages between threads.  All throff data types can be send through a queue, and since this will simply send a pointer, it is quick and efficient.

#### NEWQUEUE -> queue

	Create a new thread-safe FIFO queue

#### WRITEQ queue value

	Sends value to queue

#### READQ queue -> value

	Reads a value from queue

##### Returns
		value	- a single element from the queue

### Network


#### GETWWW url

	Fetch webpage

	Returns
		string	- webpage
		

#### DNS.HOST hostname

	Lookup hostname in the DNS system

	Returns
		array	- IP addresses
		

#### DNS.CNAME hostname

	Lookup cname in the DNS system

	Returns
		string	- canonical DNS name
		

#### DNS.TXT hostname

	Lookup text records for given hostname in the DNS system

	Returns
		array - list of text records
		

#### DNS.REVERSE hostname

	Lookup IP in the DNS system

	Returns
		array	- list of hostnames for given IP address
		
		

### I/O

#### EMIT value

Prints **value** to STDOUT

#### PRINTLN value

Prints **value** to STDOUT, followed by a newline.

### Control flow

#### IF [ condition ] [ true ] [ false ]

The **condition** value must be TRUE or FALSE, which are values returned by LESSTHAN, NOT, etc.  See the CONDITIONALS section for more

##### Example
	
	IF [ LESSTHAN X 0 ]
		[ EMIT [ X is less than 0 ] ]
		[ EMIT [ X is greater than or equal to 0 ] ]
		
or rewriting in more Throffy style:

	EMIT   IF  LESSTHAN X 0
				[ X is less than 0 ]
				[ X is greater than or equal to 0 ]
				
all flow control is based on IF, such as

#### WHEN [ condition ] [ true ]

Just like IF, but only has a true branch

### Conditionals & logic operations

Throff IF functions only accept TRUE and FALSE values, as returned by the TRUE and FALSE functions, or other built-ins listed below.  Anything else is an error.

In order to match the convenience of other, more popular languages, Throff offers the TRUTHY function, which attempts to guess whether any value you give it is TRUE or FALSE.

#### EQUAL x y

Returns true if the string value of x is equal to the string value of y.

Warning: Don't use EQUAL on WRAPPERs like file handles, network sockets, database handles, etc.  These things usually do not have string representations, so they will all "be equal", when in fact they are different.

#### LESSTHAN x y

Returns true if **x** is less than **y**

#### NOT x

#### AND x y

#### OR x y

#### EMPTY? array

Returns TRUE if **array** has no elements.

#### TRUTHY x

IF only accepts TRUE or FALSE values, but other languages provide more convenient IF statements that accept any value.  TRUTHY provides this service for THROFF.  Positive numbers are true, zero and negatives are false, and strings with length greater than 0 are true.

#### IFFY [ value ] [ true ] [ false ]

Exactly like IF, except it uses TRUTHY to decide whether **value** is TRUE or FALSE
	
### Advanced control flow

#### CASE array

Much neater than multiple if statements, CASE provides a compact way to do multiple tests, in order.

##### Example

    CASE A[
         LESSTHAN 0 X       ... [ PRINTLN [ X IS GREATER THAN 0 ] ]
         LESSTHAN X 0       ... [ PRINTLN [ X IS LESS THAN 0 ] ]
         DEFAULT            ... [ PRINTLN [ X IS EQUAL TO 0 ] ]
     ]A

Case tests each condition (on the left).  If that condition is true, it calls the function on the right.  CASE is an expression, the result of the function becomes the result of the CASE.

    REBIND COUNT => ADD COUNT CASE A[
                                         LESSTHAN 0 X       ... -1
                                         LESSTHAN X 0       ... 1
                                         DEFAULT            ... 0
                                     ]A

You can provide a function or a value, CASE will use CALL to resolve everything.

    REBIND COUNT ADD COUNT CASE A[
         [ LESSTHAN 0 X ]       ... [ -1 ]
         [ LESSTHAN X 0 ]       ... [  1 ]
         [ DEFAULT      ]       ... [  0 ]
     ]A

#### CATCH [ error handler ] [ function ]

CATCH calls **function**.  If **function** THROWs an error, thene the **error handler** will be run.  **error hander** must take one argument, which will be the THROWn error message

Returns

The result of **function**, or the result of the **error handler**

See Also

THROW

#### THROW message

THROW causes an error condition, which will be caught by the previously declared **error handler**, as declared by **CATCH**

**message** can be any value

##### Returns

THROW does not return

See Also

CATCH

#### CALL/CC lambda

Call **lambda** with the Current Continuation.  **lambda** must take one argument

Returns

- Nothing - CALL/CC never returns

#### ACTIVATE/CC continuation value

Activate **continuation** with **value**.  Control will jump to the place where the continuation was defined.

Returns

- Nothing.  ACTIVATE/CC never returns

#### PROMISE lambda -> promise

A PROMISE is a function that delays its execution until needed.  It's a way to get some of the benefits of a lazy language without actually having a lazy language.

**lambda** must return one value, and take no inputs.

When a promise is created, it delays the execution of **lambda** until the first time that the promise is accessed - usually via its variable name.  e.g.

    PRINTLN GREETING
    BIND GREETING => PROMISE [ HELLO ]

Promises are most useful when they are used on code that is expensive to run, like database or network calls.  So for instance, instead of loading data from the database for all employees and putting it into an array, you can fill the array with PROMISES which will delay fetching the data until accessed.

After the **lambda** runs, its return value is cached, and the **lambda** is not called again.  **lambda** is only ever called once.

Example

    MAP [ PROMISE [ database-fetch USER ] ARG USER => ] [ BOB MARY SUE DAVE ]

Notes about "forcing" promises


Promises can be triggered accidentally by passing them to a function that accesses them.  For instance, calling MAP on an array of PROMISEs will activate every PROMISE in the array, because MAP takes each element from the input array and "accesses" it.  Other array functions like FOLD, FILTER, etc will do the same.

While promises are difficult to handle without accidentally triggering the code, sometimes it is possible to forget to trigger the code, resulting in printing out the promise, rather than its result.  The easiest way to handle promises is to use ->LAMBDA to convert it to a normal LAMBDA, and then use CALL to fetch the result when you know you want it.  You can also safely handle promises by putting them in ARRAYs or HASHes.

Because a promise is just a function, it has to be used like a function.  If you never assign it to a variable and then use it, or use CALL on it, then the function won't run.  e.g.

   PRINTLN PROMISE [ HELLO ]

does not print HELLO, it prints [ FUNCTION DEFINITION ].  You actually need

   PRINTLN CALL PROMISE [ HELLO ]


##### Returns
- A PROMISE.

### Debugging

#### .S

Prints out the current data stack in an (almost) human readable format.

#### TRON

Trace on.  Prints out every function as it is executed

#### TROFF

Trace off.  Stops printing out program flow.

Note to self: having a trace level setting would be very useful

#### DUMP value -> string

Converts the internal data structure into a string.  Not useful unless you are looking at the interpreter.

### Actors

Actors are objects that run in their own thread.  Actors receive commands via input queues, and return results over an output queue.  Actors are asynchronous, and are best when used for slow-running code that can run in the background.

A good use of actors is networking code, e.g. fetching webpages, or communicating with a database.  They can also be used as mutexes, because they will only process one command at a time.  This makes them ideal for managing updates to databases and network services.  Each command will run in the background, one after the other.

Actors should not be used for small, fast code e.g. numerical code.  Do not, for instance, make an actor that squares a number and returns it.

Each message to an actor requires several rounds of locking and hash accesses, so it works better when wrapping slower code.

#### ACTOR lambda -> actor

Create a new actor. **lambda** will be called for each message sent to the actor.  **lambda** must take one argument and return one value, which will be written to the output queue.

Actors run in their own thread so they are a great place to put code that will block or run slowly.

Returns

- An actor.  You can send messages to it with CALLA

See Also

CALLA

#### CALLA actor value -> resultFunction

CALLA sends a **value** to an **actor**.  Value can be anything e.g. array, hash, wrapper, etc.

CALLA returns a **resultFunction** that takes no args but will return the correct value when it has been calculated.  See the section on PROMISEs for more details.

Calling the **resultFunction** will try to fetch the return value from the actor.  If the actor has not finished yet, then the current thread will block until the actor finishes.

The return value is cached and further accesses will be instant.

##### Example

    PRINTLN VALUE
    BIND VALUE => CALLA DOUBLE 2
    BIND DOUBLE => ACTOR [ MULT 2 ]

##### Returns

- a PROMISE.  This is (or will be), the return value from the actor

See Also

ACTOR, PROMISE

### Sub processes

Throff supports starting subprocesses (other programs), and the ability to wait for or kill them.  Functions to capture the STDOUT/STDERR coming soon.

#### STARTPROCESS path A[ arg0 arg1 arg2 ... ]A

Start another program at **path**.  The **args** list will be passed to the program as its argument list.  Note that the first element of args will be the $0 arg for the program, which is usually set to the **path** by other languages.  You can set anything you want there, but some programs might rely on it being the path to the program.  You must provide something for the 0th arg, as all programs start reading the arg list from the 1st place.  e.g.

    STARTPROCESS /bin/echo [ /bin/echo Hello world ]

If you omit the second /bin/echo, echo will only print "world"

##### Returns

- ProcHandle  A native wrapper around a process handle.  Used by KILLPROC and WAITPROC.

#### KILLPROC ProcHandle -> resultCode

Immediately kill the process represented by **ProcHandle**.

##### Returns
- code  A success code.

#### WAITPROC ProcHandle -> Summary

Wait for the subprocess **ProcHandle** to finish before continuing.

##### Returns

- Summary   A summary of the process' execution, e.g. time, exit code, etc

#### SUBSHELL [ command line ] -> output

Runs **command line** in the default shell, and returns the output.

This command works on Mac, Linux and windows, but you get whatever shell is installed, so your commands will not be cross platform.



#### CMDSTDOUTSTDERR [ executable arg1 arg2 arg3 ...  ] -> Output

Starts a subprocess and waits for it to finish, then returns all the programs output as a string.  Output from STDOUT and STDERR will
be mixed together in the same way it would be printed to a screen.

**executable** must be the full path to a program.  The rest of the array will be used as program arguments

##### Returns

- Output All the program output

#### OS -> name

OS returns the name of the operating system that throff was compiled for.

#### MAP function array -> array

FIXME

#### FOLD function start array -> a value

Fold works like map, it applies a function to every element of the array.  But instead of building a new array, FOLD passes the result of your function to the next
call of **function**

The function must work like this:  FUNC leaf accumulator

FIXME moar

#### FOLDTREE

As for fold, but works on any data structure.  It recursively visits every element of every subtree until it has called **function** on every element in the tree.

#### TREEWALK

Calls **function** on every leaf of the tree in turn

### NAMESPACES and SCOPES

Namespaces and scopes are still a bit wobbly in Throff, but are at the point where they function well enough to be called 'feature complete'.

Manipulating them directly is usually safe, because they are _immutable_.  Or _mostly_ immutable.

Scopes are throff hashes, and if you can get a reference to them, you can use ordinary throff hash functions on them.

Scopes are all nested, with the root scope being the initial scope that the program starts with.  Variables can be looked up with GETLEX, which will search every scope from the current up to the root.

The current scope is mutable but all other scopes (especially parent) are immutable, although it is not hard to find a way to modify the memory of another scope.  Doing this will not work the way you want it to, since scopes are often copied or optimised away.

Almost every [ ] introduces a new scope (MACRO and THIN functions are exceptions).  This means you can't update variables inside a MAP or FOLD or FOR loop, because all the variables inside the [ ] are discarded.

BIND creates a new variable in the current scope, and fails if the variable already exists.  REBIND either replaces a BIND, or shadows a variable in the parent scope.  Throff will throw an error if you attempt to bind a variable name that is already bound in a parent scope.

#### Scoping rules

There are two special types of scoping rules:  THIN and MACRO.

MACROs have no scope when they are defined, they temporarily gain one when they are run, enabling them to create and destroy variables in other scopes.  MACROs are heavily used to implement language features, and should also be quite fast (since we don't have to create a scope when they run).

THIN functions are probably a bad idea.  THIN functions use their parent scope, and not their own, when they run.  This does allow you to update variables from inside a FOR loop, but it is a very bad idea in general, since, among other things, it prevents any optimisation of that scope.

SETLEX name value

Sets the variable *name* to *value*.  *name* must already exist as a variable.  Better to use BIND (TODO delete SETLEX?)

SCRUBLEX name

Deletes variable *name*.  *name* must already exist.

GETLEX name

Looks up the value of *name* using the current scope.

: name value

Creates binding *name* and sets its value to *value*.  Fails if *name* alredy exists.

(Also known as BIND)
