package com.praeceptamachinae;

abstract class Stepper {
    public ThroffEngine go(ThroffEngine e, Thingy instruction, Thingy environment) {
        System.out.print("Error: called virtual method\n");
        return e;
    }

}
