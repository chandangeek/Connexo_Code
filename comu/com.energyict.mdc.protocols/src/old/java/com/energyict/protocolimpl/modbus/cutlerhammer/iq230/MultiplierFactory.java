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

package com.energyict.protocolimpl.modbus.cutlerhammer.iq230;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author kvds
 */
public class MultiplierFactory {

    private BigDecimal i;

    private BigDecimal v;

    private BigDecimal w;

    private BigDecimal e;

    IQ230 iq230;

    /** Creates a new instance of MultiplierFactory */
    public MultiplierFactory(IQ230 iq230) {
        this.iq230=iq230;
        i=v=w=e=null;
    }


    public BigDecimal getMultiplier(int scale) throws IOException {
        BigDecimal scalefactor = BigDecimal.ZERO;
        if (scale == (char)'I') {
           scalefactor = getI();
        }
        else if (scale == (char)'V') {
           scalefactor = getV();
        }
        else if (scale == (char)'W') {
           scalefactor = getW();
        }
        else if (scale == (char)'E') {
           scalefactor = getE();
        }
        else {
            throw new IOException("PM750, invalid scale " + (char) scale);
        }

        //return new BigDecimal(""+Math.pow(10, scalefactor.longValue()));
        return BigDecimal.ONE.movePointRight((int)scalefactor.longValue());
    }

    private BigDecimal getI() throws IOException {
        if (i==null) {
            i = iq230.getRegisterFactory().findRegister("scale I").quantityValue().getAmount();
        }
        return i;
    }

    private BigDecimal getV() throws IOException {
        if (v==null) {
            v = iq230.getRegisterFactory().findRegister("scale V").quantityValue().getAmount();
        }
        return v;
    }

    private BigDecimal getW() throws IOException {
        if (w==null) {
            w = iq230.getRegisterFactory().findRegister("scale W").quantityValue().getAmount();
        }
        return w;
    }

    private BigDecimal getE() throws IOException {
        if (e==null) {
            e = iq230.getRegisterFactory().findRegister("scale E").quantityValue().getAmount();
        }
        return e;
    }

}
