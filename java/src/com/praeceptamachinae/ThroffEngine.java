package com.praeceptamachinae;

public class ThroffEngine implements Cloneable{
    public  Thingy environment;
    public  ThingyStack dataStack;
    public  ThingyStack dyn;
    public ThingyStack codeStack;
    public  ThingyStack lexStack;
    public int sequenceID;
    public Boolean running;
    public int _prevLevel;
    public int _funcLevel;
    public int _line;

    public ThroffEngine() {
        this.environment = new Thingy("HASH");
        this.environment._hashVal.put("EMIT", new Thingy("EMIT", new StepLibrary()));
        this.environment._hashVal.put("NTEST", new Thingy("NTEST", new StepLibrary()));
        this.dataStack = new ThingyStack();
        this.dyn = new ThingyStack();
        this.codeStack = new ThingyStack();
        this.lexStack = new ThingyStack();
        this.sequenceID = 0;
        this.running = false;
    }
    public ThingyStack tokenise(String s, String filename) {
        int line = 0;
        s = s.replace("\n", "LINEBREAKHERE");
        String[] bits = s.split("\\s+");
        ThingyStack tokens = new ThingyStack();
        for (int i = 0; i < bits.length; i = i + 1) {
            sequenceID = sequenceID + 1;
            String str = bits[i];
            if (str.length()>0) {
                if (str == "LINEBREAKHERE") {
                    line = line + 1;
                } else {
                    Thingy t = new Thingy("TOKEN", str, new Thingy("HASH")); //value, environment
                    t._id = sequenceID;
                    t._line = line;
                    t._filename = filename;
                    tokens.push(t);
                }
            }
        }
        return tokens;
    }

    void LoadTokens( ThingyStack s) {
        for (Thingy elem : s)  {
            elem.environment = this.environment;
            this.lexStack.push(this.environment);
            this.codeStack.push(elem);
        } //All tokens start off sharing the root environment

    }

    public ThroffEngine Run(String s) throws CloneNotSupportedException {
        ThingyStack tokens = this.tokenise(s, "run");
        this.LoadTokens(tokens);
        ThroffEngine estep = this;
        estep.running = true;
        while (estep.running) {
            estep = this.Step();
        }
        return estep;
    }

    public ThroffEngine realStep() throws CloneNotSupportedException {

        //Set up engine for this step
        Thingy lex = this.lexStack.pop();
        Thingy code = this.codeStack.pop();
        System.out.print(code._stringVal);
        System.out.print("\n");
        this.environment = lex;

        //Actually do something
        ThroffEngine steppedEngine = code._stub.go(this, code, lex);

        if (steppedEngine.codeStack.isEmpty()){
            steppedEngine.running = false;
        }
        return steppedEngine;
    }

    public ThroffEngine Step() throws CloneNotSupportedException {
        ThroffEngine engine = (ThroffEngine ) this.clone();
        return engine.realStep();
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
