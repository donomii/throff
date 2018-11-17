package main

import "github.com/donomii/throfflib"

import (
	"fmt"
	_ "net/http/pprof"
	"os"
)

func main() {
	go func() {
		//log.Println(http.ListenAndServe("localhost:6060", nil))
	}()
	t := throfflib.MakeEngine()
	t = throfflib.LoadGraphics(t)
	//t = throfflib.LoadAudio(t)
	strs := os.Args[1:]
	//t = t.RunFile("bootstrapgo.lib")
	t = t.RunString(throfflib.BootStrapString(), "Internal Bootstrap")

	if len(strs) == 0 {
		t = t.RunString("PRINTLN [ Welcome to the THROFF command shell v0.1.  Type HELP for help. ]", "repl")
		throfflib.Repl(t)
	} else {

		fmt.Printf("")
		tokens := throfflib.StringsToTokens(strs)
		t.LoadTokens(tokens)
		t = t.Run()
		t.RunString("PRINTLN", "replprint")

	}
}
