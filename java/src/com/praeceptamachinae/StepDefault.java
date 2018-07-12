package com.praeceptamachinae;
import java.util.*;
import java.util.HashMap;

public class StepDefault extends Stepper {

    @Override
    public ThroffEngine go(ThroffEngine e, Thingy instruction, Thingy environment) {
        //Dispatch to the correct handler for the type
        //Are we in function-building mode?
        e._prevLevel = e._funcLevel;
        String source = instruction.getSource();
        System.out.print("Considering thingy '"+source+"'\n");
        if (source.equals( "[" )){
            //Finish a (possibly nested) function
            e._funcLevel -=1;
            System.out.print("Decrementing function level");
            //FIXME Move this to a function
            if (e._funcLevel == 0 ) {
                ThingyStack f = new ThingyStack();
                e = this.buildFunc(e, f);
                Thingy newFunc = e.dataStack.pop();
                newFunc.environment = e.environment;
            }
        }
        if (source.equals( "]" )) {
            //Start a new function, or enter a nested function
            e._funcLevel +=1;
            System.out.print("Incrementing function level");
        }
        if (e._funcLevel<0) {
            System.out.print("Error: Unmatched [ at line " + instruction._line);
        }

        if (e._funcLevel == 0 ) {
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
        }
        return e;
    }

    private ThroffEngine buildFunc(ThroffEngine e, ThingyStack f) {
        //FIXME write this
        Thingy v;
        ThroffEngine ne = (ThroffEngine) e.clone();
        v = ne.dataStack.pop();
        if (v.getSource() == "[") {
            ne._funcLevel += 1;
        }
        if (v.getSource() == "]") {
            ne._funcLevel -= 1;
        }

        //fmt.Printf("BUILDFUNC: in function level: %v\n", ne._funcLevel)
        //fmt.Printf("Considering %v\n", v.getSource())
        if (v.getSource().equals("]") && ne._funcLevel == 0) {
            //fmt.Printf("fINISHING FUNCTION\n")
            //This code is called when the newly-built function is activated
            Thingy newFunc = new Thingy("CODE","InterpretedCode", new StepDefault());
            newFunc.tiipe = "LAMBDA";
            newFunc.subType = "INTERPRETED";
            newFunc._arrayVal = f;
            newFunc._line = e._line;
            ne.dataStack.push(newFunc);
            return ne;
        } else {
            //fmt.Printf("Building FUNCTION\n")
            v.environment = e.environment;
            f.push(v);
            ne = buildFunc(ne, f);
        }
        //fmt.Printf("Returning\n")
        return ne;
    }
}


