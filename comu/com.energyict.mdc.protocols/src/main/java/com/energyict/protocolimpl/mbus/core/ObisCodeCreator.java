/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeCreator.java
 *
 * Created on 5 oktober 2007, 14:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

/**
 *
 * @author kvds
 */
public class ObisCodeCreator {
    
    private int a=-1;
    private int b=-1;
    private int c=-1;
    private int d=-1;
    private int e=-1;
    private int f=-1;
    
    public ObisCodeCreator() {
    }
    
    /** Creates a new instance of ObisCodeCreator */
    public ObisCodeCreator(int a, int b, int c, int d, int e, int f) {
        this.setA(a);
        this.setB(b);
        this.setC(c);
        this.setD(d);
        this.setE(e);
        this.setF(f);
    }

    /** Creates a new instance of ObisCodeCreator */
    public ObisCodeCreator(int a, int b, int c, int d) {
        this.setA(a);
        this.setB(b);
        this.setC(c);
        this.setD(d);
    }
    public ObisCodeCreator(int e, int f) {
        this.setE(e);
        this.setF(f);
    }

    public int getA() {
        return a;
    }

    public ObisCodeCreator setA(int a) {
        this.a = a;
        return this;
    }

    public int getB() {
        return b;
    }

    public ObisCodeCreator setB(int b) {
        this.b = b;
        return this;
    }

    public int getC() {
        return c;
    }

    public ObisCodeCreator setC(int c) {
        this.c = c;
        return this;
    }

    public int getD() {
        return d;
    }

    public ObisCodeCreator setD(int d) {
        this.d = d;
        return this;
    }

    public int getE() {
        return e;
    }

    public ObisCodeCreator setE(int e) {
        this.e = e;
        return this;
    }

    public int getF() {
        return f;
    }

    public ObisCodeCreator setF(int f) {
        this.f = f;
        return this;
    }

    public String toString() {
		return String.format("%d.%d.%d.%d.%d.%d", a, b, c, d, e, f);    	
    }
    
    
}
