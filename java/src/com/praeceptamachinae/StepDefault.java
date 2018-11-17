package com.praeceptamachinae;
import java.util.*;
import java.util.HashMap;

public class StepDefault extends Stepper {

    @Override
    public ThroffEngine go(ThroffEngine e, Thingy instruction, Thingy environment) {
        System.out.print("Starting step with function level: "+ Integer.toString(e._funcLevel)+"\n");
        //Dispatch to the correct handler for the type
        //Are we in function-building mode?
        e._prevLevel = e._funcLevel;
        String source = instruction.getSource();
        System.out.print("Considering thingy '"+source+"'\n");
        if (source.equals( "[" )){
            //Finish a (possibly nested) function
            e._funcLevel = e._funcLevel - 1;
            System.out.print("Decrementing function level\n");
            System.out.print("function level: "+ Integer.toString(e._funcLevel)+"\n");
            //FIXME Move this to a function
            if (e._funcLevel == 0 ) {
                ThingyStack f = new ThingyStack();
		    e._funcLevel = e._funcLevel + 1;
                e = this.buildFunc(e, f);
                Thingy newFunc = e.dataStack.pop();
                newFunc.environment = e.environment;
		e.dataStack.push(newFunc);
            }
        }
        if (source.equals( "]" )) {
            //Start a new function, or enter a nested function
            e._funcLevel = e._funcLevel + 1;
            System.out.print("Incrementing function level\n");
            System.out.print("function level: "+ Integer.toString(e._funcLevel)+"\n");
        }
        if (e._funcLevel<0) {
            System.out.print("Error: Unmatched [ at line " + instruction._line+"\n");
        }

        if (e._funcLevel == 0 && ! source.equals("[") ) {
            if (instruction.tiipe == "TOKEN") {
                System.out.print("Looking up " + instruction._stringVal + "\n");
                Thingy value = e.environment._hashVal.get(instruction._stringVal);
                if (value != null) {
                    if (value.tiipe == "CODE") {
                        System.out.print("Found variable value:" + value + ", calling go method\n");
                        value._stub.go(e, value, environment);
                    } else {
                        System.out.print("Found variable value:" + value + ", pushing onto stack\n");
                        e.dataStack.push(value);
                        e.lexStack.push(environment); //FIXME
                        return e;
                    }
                } else {
                    System.out.print("Variable not defined, assuming it is an unquoted string\n");
                    e.dataStack.push(instruction);
                    e.lexStack.push(environment);
                }
            }
        } else {

		e.dataStack.push(instruction);
	}
        return e;
    }

    private ThroffEngine buildFunc(ThroffEngine e, ThingyStack f) {
        System.out.print("=== Starting buildfunc with level "+ e._funcLevel +" ===\n");
        //FIXME write this
        Thingy v;
        ThroffEngine ne = (ThroffEngine) e.clone();
        v = ne.dataStack.pop();
        if (v.getSource().equals("[")) {
            ne._funcLevel += 1;
		System.out.print("building with function level: "+ Integer.toString(ne._funcLevel)+"\n");
        }
        if (v.getSource().equals("]")) {
            ne._funcLevel -= 1;
		System.out.print("building with function level: "+ Integer.toString(ne._funcLevel)+"\n");
        }

        //fmt.Printf("BUILDFUNC: in function level: %v\n", ne._funcLevel)
        //fmt.Printf("Considering %v\n", v.getSource())
        if (v.getSource().equals("]") && ne._funcLevel == 0) {
	System.out.print("Finishing function\n");
            //fmt.Printf("fINISHING FUNCTION\n")
            //This code is called when the newly-built function is activated
            StepDefault sd = new StepDefault();
            Thingy newFunc = new Thingy("LAMBDA", sd);
            newFunc.subType = "INTERPRETED";
            newFunc._arrayVal = f;
            newFunc._line = e._line;
            ne.dataStack.push(newFunc);
            return ne;
        } else {
            //fmt.Printf("Building FUNCTION\n")
        System.out.print("Pushing "+ v.getSource() +"into function\n");
            v.environment = e.environment;
            f.push(v);
            ne = buildFunc(ne, f);
        }
        //fmt.Printf("Returning\n")
        return ne;
    }
}


