package com.praeceptamachinae;
import java.util.*;

public class Thingy {
    public int _intVal;
    public String tiipe;
    public String subType;
    public Thingy environment;

    public Object _structVal;
    public String _stringVal;
    public ThingyStack _arrayVal;
    public ThroffEngine _engineVal;
    public Hashtable<String, Thingy> _hashVal;
    public Stepper _stub;
    public byte[] _byteVal;

    public int _id;
    public int _line;
    public String _filename;


    public Thingy() {
    }

    //Creates code of the requested type. We can't create a fresh environment here, it would be infinityly recursive
    public  Thingy(String tiipe) {
        this.tiipe = tiipe;
        this._stub = new StepDefault();
        //Hashtable<String, Thingy> environment = new Hashtable<String, Thingy>();
        //this.environment = environment;

        if (tiipe == "BOOLEAN") {
            this.subType = "NATIVE";
            this._intVal = 0;
        }
        if (tiipe == "HASH") {
            this.subType = "NATIVE";
            this._hashVal = new Hashtable<String, Thingy>();
        }
        if (tiipe == "TOKEN") {
            this.subType = "NATIVE";
            this._stringVal = "";
        }
        if (tiipe == "ARRAY") {
            this.subType = "INTERPRETED";
            this._arrayVal = new ThingyStack();
        }

    }

    //New CODE Thingy.  You must provide the stepper function(StepDefault, StepLibrary)
    public  Thingy(String s, Stepper l) {
        this.tiipe = "CODE";
        this._stringVal = s;
        this._stub = l;
    }


    public  Thingy(Hashtable<String, Thingy> hashVal) {
        this.tiipe = "HASH";
        this._stub = new StepDefault();
        this.subType = "NATIVE";
        this._hashVal = hashVal;
    }

    //Creates code of the requested type, and uses the provided environment
    public  Thingy(String tiipe, String content, Thingy environment) {
        this.tiipe = tiipe;
        this._stub = new StepDefault();
        //Hashtable<String, Thingy> environment = new Hashtable<String, Thingy>();
        this.environment = environment;

        if (tiipe == "BOOLEAN") {
            this.subType = "NATIVE";
            this._intVal = 0;
        }
        if (tiipe == "HASH") {
            this.subType = "NATIVE";
            this._hashVal = new Hashtable<String, Thingy>();
        }
        if (tiipe == "TOKEN") {
            this.subType = "NATIVE";
            this._stringVal = content;
        }
        if (tiipe == "ARRAY") {
            this.subType = "INTERPRETED";
            this._arrayVal = new ThingyStack();
        }
    }



    @Override
    public String toString() {
        return "A Thing\nString: "+this._stringVal+"\nStub: "+this._stub+"\n";
    }

    public String getSource() {
        return this._stringVal;
    }
}
