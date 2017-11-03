* throff runs backwards

Throff programs start at the _bottom_ and are evaluated backwards until they reach the top, where they finish.  Actually, the line breaks are removed and the program becomes one long line, which is evaluated from right-to-left.

* There is no parser, lexxer or tokeniser

Throff programs are split on whitespaces, then fed into the interpreter.  You need spaces between everything!  Do not combine brackets like this ((( ))), or allow an = to 'touch' a variable e.g. a=b


* you can't change variables in your parent scope

It looks like you can, but your changes will be discarded when you exit the current scope.  You kind of can change variables in the current scope (using REBIND), but depending on how I optimise this, will either disable the optimiser entirely or force a new scope for each REBIND (also slow).

In general, you lose the ability to do things like this (C example)

	int j = 0;
	for (int i;i<10;i++) {
		j++;
	}

You can get around this by using THIN functions, but I very strongly recommend that you don't.  Instead, use MAP, FILTER, FOLD, ZIP and UNZIP