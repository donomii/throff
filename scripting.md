# Scripting interface for golang

Throff is a useful scripting language for golang.  Like most embedded scripting languages, the challenge is in passing data to throff from golang in a format that it can use.

Passing basic types into throff is easy.  Throff works mainly on strings, and has some limited support for things that aren't strings.  Strings and numbers can passed in directly.  Arrays and hashes can be passed with the use of some helper functions, and everything else gets a lot more difficult.

First, the easy bits:

## CallArgs

``` golang
	t1 := t.CallArgs("ADD", 4, "5")
```

The first arg is the "program" to run, everything else is an argument.  Unlike normal throff, arguments can contain spaces and special characters:

``` golang
	t1 := t.CallArgs("PRINTLN", "Hello World!")
```

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

## t, t1, t2 and engine steps

One of the more interesting things about throff is that the engine is completely immutable.  Rather than updating itself as the program runs, throff creates a new copy of the engine for each step.  So when we run

``` golang
	result, t1 := t.CallArgs1("ADD", 4, "5")
```

t does not change.  Throff creates a new engine with the result of the function "ADD", and puts the result in t1.

This means you can "save" the throff engine, and "load" it from the same point over and over.  This is handy for things like webservers, which can prepare the throff interpreter, then run the same engine over and over to handle web requests.


