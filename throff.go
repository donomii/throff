package main

import (
"log"
	"os"
	"strings"

	"github.com/abadojack/whatlanggo"
	"github.com/donomii/throfflib"
)

//_ "net/http/pprof"

var lang = "en"

func main() {
	go func() {
		//log.Println(http.ListenAndServe("localhost:6060", nil))
	}()
	t := throfflib.MakeEngine()
	//t = throfflib.LoadGraphics(t)  //Add this to throfflib so it can be called from Throff as a word?
	//t = throfflib.LoadAudio(t)
	strs := os.Args[1:]
	log.Printf("Evaluating %v\n", strs)
	if len(strs) == 1 {
		for _, arg := range strs {
			if whatlanggo.Jpn == whatlanggo.Detect(arg).Lang {
				lang = "jp"
				//log.Println("Japanese detected")
			}
		}
	}
	//t = t.RunFile("bootstrapgo.lib")
	t = t.RunString(throfflib.BootStrapString(), "Internal Bootstrap")

	if len(strs) == 0 {
		t = t.RunString("PRINTLN [ Welcome to THROFF  v0.1.  Type HELP for help. ]", "repl")
		//t = t.RunString(" [ \nTHROFF v0.1.  Type HELP for help. ]", "repl")
		throfflib.Repl(t)
	} else {
		throfflib.PrintWarnings = false
		if lang == "jp" {
			throfflib.BraceMode = "forth"
			strs = strings.Split(strs[0], "　")
		}

		t = t.RunString("SPACE", "Command line evaluator") //We print the top of the stack after the calculation.  If the calculation returns nothing, we don't want to print "Stack empty"

		tokens := throfflib.StringsToTokens(strs)
		t.LoadTokens(tokens)

		t = t.Run()
		t.RunString("PRINTLN", "replprint")
	}
}
