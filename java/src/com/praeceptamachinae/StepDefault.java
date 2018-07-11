package com.praeceptamachinae;

public class StepDefault extends Stepper {

    @Override
    public ThroffEngine go(ThroffEngine e, Thingy instruction, Thingy environment) {
        //Dispatch to the correct handler for the type

        if (instruction.tiipe == "TOKEN") {
            System.out.print("Looking up "+instruction._stringVal+"\n");
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
        return e;
    }
}


