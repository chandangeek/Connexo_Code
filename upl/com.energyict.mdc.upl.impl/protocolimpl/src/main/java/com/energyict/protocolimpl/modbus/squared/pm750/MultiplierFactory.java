/*
 * MultiplierFactory.java
 *
 * Created on 19 oktober 2007, 10:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.squared.pm750;

import java.io.*;
import java.math.*;

/**
 *
 * @author kvds
 */
public class MultiplierFactory {

    private BigDecimal i;

    private BigDecimal v;

    private BigDecimal w;

    private BigDecimal e;

    PM750 pm750;
    
    /** Creates a new instance of MultiplierFactory */
    public MultiplierFactory(PM750 pm750) {
        this.pm750=pm750;
        i=v=w=e=null;
    }


    public BigDecimal getMultiplier(int scale) throws IOException {
        BigDecimal scalefactor= new BigDecimal(0);
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
        else throw new IOException("PM750, invalid scale "+(char)scale);
        
        //return new BigDecimal(""+Math.pow(10, scalefactor.longValue()));
        return (new BigDecimal("1")).movePointRight((int)scalefactor.longValue());
    }
    
    private BigDecimal getI() throws IOException {
        if (i==null)
            i= pm750.getRegisterFactory().findRegister("scale I").quantityValue().getAmount();
        return i;
    }

    private BigDecimal getV() throws IOException {
        if (v==null)
            v= pm750.getRegisterFactory().findRegister("scale V").quantityValue().getAmount();
        return v;
    }

    private BigDecimal getW() throws IOException {
        if (w==null)
            w= pm750.getRegisterFactory().findRegister("scale W").quantityValue().getAmount();
        return w;
    }

    private BigDecimal getE() throws IOException {
        if (e==null)
            e= pm750.getRegisterFactory().findRegister("scale E").quantityValue().getAmount();
        return e;
    }

}
