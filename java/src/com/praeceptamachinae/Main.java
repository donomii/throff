package com.praeceptamachinae;

public class Main {

    public static void main(String[] args) throws CloneNotSupportedException {
        ThroffEngine e = new ThroffEngine();
        e.Run("NTEST EMIT HELLOWORLD");
        System.out.print(e);
        System.out.print("Job done!");
    }
}
