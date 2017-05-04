/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MultiplierFactory.java
 *
 * Created on 19 oktober 2007, 10:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.squared.pm800;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author kvds
 */
public class MultiplierFactory {

    private BigDecimal a;

    private BigDecimal b;

    private BigDecimal d;

    private BigDecimal e;

    private BigDecimal f;

    PM800 pm800;

    /** Creates a new instance of MultiplierFactory */
    public MultiplierFactory(PM800 pm800) {
        this.pm800=pm800;
        a=b=d=e=f=null;
    }


    public BigDecimal getMultiplier(int scale) throws IOException {
        BigDecimal scalefactor = BigDecimal.ZERO;
        if (scale == (char)'A') {
           scalefactor = getA();
        }
        else if (scale == (char)'B') {
           scalefactor = getB();
        }
        else if (scale == (char)'D') {
           scalefactor = getD();
        }
        else if (scale == (char)'E') {
           scalefactor = getE();
        }
        else if (scale == (char)'F') {
           scalefactor = getF();
        }
        else {
            throw new IOException("PM800, invalid scale " + (char) scale);
        }

        //return new BigDecimal(""+Math.pow(10, scalefactor.longValue()));
        return BigDecimal.ONE.movePointRight((int)scalefactor.longValue());
    }

    private BigDecimal getA() throws IOException {
        if (a==null) {
            a = pm800.getRegisterFactory().findRegister("scale A").quantityValue().getAmount();
        }
        return a;
    }

    private BigDecimal getB() throws IOException {
        if (b==null) {
            b = pm800.getRegisterFactory().findRegister("scale B").quantityValue().getAmount();
        }
        return b;
    }

    private BigDecimal getD() throws IOException {
        if (d==null) {
            d = pm800.getRegisterFactory().findRegister("scale D").quantityValue().getAmount();
        }
        return d;
    }

    private BigDecimal getE() throws IOException {
        if (e==null) {
            e = pm800.getRegisterFactory().findRegister("scale E").quantityValue().getAmount();
        }
        return e;
    }

    private BigDecimal getF() throws IOException {
        if (f==null) {
            f = pm800.getRegisterFactory().findRegister("scale F").quantityValue().getAmount();
        }
        return f;
    }
}
