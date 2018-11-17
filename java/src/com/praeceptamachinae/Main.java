package com.praeceptamachinae;

public class Main {

    public static void main(String[] args) throws CloneNotSupportedException {
        ThroffEngine e = new ThroffEngine();
        ThroffEngine newEngine = e.Run("[ [ goodbye ] ] [ Hello ] EMIT HELLOWORLD");
	newEngine.dump();
        System.out.print("Job done!");
    }
}
