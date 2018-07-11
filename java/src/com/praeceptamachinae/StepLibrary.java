package com.praeceptamachinae;

public class StepLibrary extends Stepper {

    @Override
    public ThroffEngine go(ThroffEngine e, Thingy instruction, Thingy environment) {
        System.out.print("Code library:"+instruction._stringVal);
        if (instruction.tiipe != "CODE") {
            System.out.print("Error: Instruction that is not CODE attempted to call a CODE handler");
        }
        if (instruction._stringVal=="EMIT"){
            System.out.print("Emitting...\n");
            Thingy lex = e.lexStack.pop();
            Thingy data = e.dataStack.pop();
            System.out.print("Emitting string\n");
            System.out.print(data._stringVal);
        }
        if (instruction._stringVal=="NTEST"){
            System.out.print("Creating native string\n");
            Thingy t = new Thingy();
            t._stringVal = "Native test";
            NativeLoader n = new NativeLoader();
            t._structVal = NativeLoader.reify(new String[]{"nativestring"});
            e.lexStack.push(environment);
            e.dataStack.push(t);
        }
        return e;
    }
}

