package main
import "github.com/donomii/throfflib"



import (

	"os"
	"fmt"
	_ "net/http/pprof"
	"log"
	"net/http"
	
)



func main () {
   //do_graphix()
	go func() {
			log.Println(http.ListenAndServe("localhost:6060", nil))
	}()
	t := throfflib.MakeEngine()
	strs := os.Args[1:]
	t = t.RunFile("bootstrapgo.lib")
	//t = t.RunString(throff.BootstrapText())
	//t = t.RunString(`: HELP => [ ITERATE [ PRINTLN ] ->ARRAY [ ->STRING [ Welcome to THROFF ] [ ] [ Throff is a minimal scripting language and an ultra-lightweight interpreter. It provides powerful scripting access to its host. ] [ ] [ Current host: Google's Go Language  ] [ ] [ Supported libraries: None so far ] ] ]`)
	if len(strs) == 0 { 
		t = t.RunString("PRINTLN [ Welcome to the THROFF command shell.  Type HELP for help. ]", "repl")
		throfflib.Repl(t)
	} else {
		//fmt.Printf("Running: %v\n", strs)
		fmt.Printf("")
		tokens := throfflib.StringsToTokens(strs)
		//fmt.Printf("Tokens: %v\n", tokens)
		t.LoadTokens(tokens)
		t=t.Run()
		t.RunString("PRINTLN", "replprint")
	}
}