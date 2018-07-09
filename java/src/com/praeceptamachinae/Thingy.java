package com.praeceptamachinae;
import java.util.*;

public class Thingy {
    public String tiipe;
    public String subType;
    public Thingy environment;

    public Object _structVal;
    public String _stringVal;
    public ThingyStack _arrayVal;
    public ThroffEngine _engineVal;
    public Hashtable<String, Thingy> _hashVal;
    public StepDefault _stub;
    public byte[] _byteVal;

    public int _id;
    public int _line;
    public String _filename;

    public  Thingy(String s) {
        this.tiipe = s;
        this._stub = new StepDefault();
        if (s == "HASH"){
            this._hashVal = new Hashtable<String, Thingy>;
        }
    }

    public  Thingy(String s, Thingy t) {
        this.tiipe = "TOKEN";
        this._stringVal = s;
        this.environment = t;
        this._stub = new StepDefault();

    }

}
