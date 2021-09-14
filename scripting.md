# Scripting interface for golang

Throff is a useful scripting language for golang.  Like most embedded scripting languages, the challenge is in passing data to throff from golang in a format that it can use.

Throff works mainly on strings, and has some limited support for things that aren't strings.  Strings and numbers can passed in directly.  Arrays and hashes can be passed with the use of some helper functions, and everything else gets a lot more difficult.

First, the easy bits:

## CallArgs

``` golang
	t1 := t.CallArgs("ADD", 4, "5")
```

The first arg is the "program" to run, everything else is an argument.  Unlike normal throff, arguments can contain spaces and special characters:

``` golang
	t1 := t.CallArgs("PRINTLN", "Hello World!")
```

Callargs does not return any data from the calculation.

## CallArgs1, CallArgs2, CallArgs3

You're going to want to access the results of your calculations in golang.

``` golang
	result, t1 := t.CallArgs1("ADD", 4, "5")
	fmt.Println("Calculation result: ", result)
```

``` golang
	result, t1 := t.CallArgs1("ADD", 4, "5")
	fmt.Println("Calculation result: ", result)
```

## CallArray

If you have more data to move in and out of throff, you can use string arrays.


``` golang
	results, _ := t.CallArray("REVERSE", []string{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"})
	fmt.Println("Reversed week: ", results)
```



## FIXME Add wrapper calls

You can also retrieve data directly from the engine, or add it directly to the engine.  Throff is a stack language, so you can fetch data by removing the top of the stack, and add data by pushing onto the stack.  All Throff functions take the arguments from the stack, so pushing data onto the stack is equivalent to passing arguments to a Throff function



### func PopData(e *Engine) (*Thingy, *Engine)

Takes the top elemenet from the data stack.  This will usually be the result of the last function that ran.  You can user .GetString() to get the data written into a string.


### PushData(e *Engine, t *Thingy) *Engine 

Pushes t onto the data stack.  This places t as being the first argument to the next function that runs in the engine.

Some helper functions are provided to build t.

#### func NewString(aString string, env *Thingy) *Thingy 

Takes a go string and wraps it correctly for the throff engine.  Needs an environment as return by Environment()

#### func NewBytes(bytes []byte, env *Thingy) *Thingy 

Takes a go []byte and wraps it correctly for the throff engine.  Needs an environment as return by Environment()

#### func NewArray(a Stack) *Thingy

Takes a stack ([]*Thingy) and wraps it for the throff engine.  

#### func NewWrapper(s interface{}) *Thingy

Takes any go value and wraps it for the throff engine.  You will not be able to access this value from Throff, but you can pass it to other functions.


### func Environment(e *Engine) *Thingy 

Fetches the current lexical environment from the engine.  Needed to create data wrappers, because every piece of data must have a source location.

## Example

Here is a complete example of adding a function to Throff.  The numbers 0,1,1 are the total stack change(0), the amount we take from the stack (1), and the amount we push back onto the stack (1).  If these numbers do not match what the code does, throff will throw an error.

```golang
	engine = throfflib.Add(engine, "FormatObject", throfflib.NewCode("FormatObject", 0, 1, 1, func(ne *throfflib.Engine, c *throfflib.Thingy) *throfflib.Engine {
		//Fetch data from throff
		obj, ne := throfflib.PopData(ne)

		//do something with it
		out := FormatObject(obj.GetString())
		
		//Push the result into the engine
		o := throfflib.NewString(out, throfflib.Environment(e))
		ne = throfflib.PushData(ne, o)
		return ne
	}))
```

## engine steps

Rather than updating itself as the program runs, throff creates a new copy of the engine for each step.  So when we run

``` golang
	result, t1 := t.CallArgs1("ADD", 4, "5")
```

t does not change.  Throff creates a new engine with the result of the function "ADD", and puts the result in t1.

This means you can "save" the throff engine, and "load" it from the same point over and over.  This is handy for things like webservers, which can prepare the throff interpreter, then run the same engine over and over to handle web requests.


