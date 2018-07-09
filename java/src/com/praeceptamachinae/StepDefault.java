package com.praeceptamachinae;

public class StepDefault {

    public ThroffEngine go(ThroffEngine e, Thingy instruction) {
        //Dispatch to the correct handler for the type
        if (instruction.tiipe == "TOKEN") {
            Thingy value = e.environment._hashVal.get(instruction._stringVal);
            if (value != null) {
                e.dataStack.stack.push(value);
                return e;
            } else {
                System.out.print("Variable not defined\n");
            }
        }
        return e;
    }
}


