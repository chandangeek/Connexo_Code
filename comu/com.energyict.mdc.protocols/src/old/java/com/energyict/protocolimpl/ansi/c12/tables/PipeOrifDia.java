/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PipeOrifDia.java
 *
 * Created on 27 oktober 2005, 10:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PipeOrifDia {

    static private final int SIZE=2; // 2 x NiFormat

    private Number pipeDia;
    private Number orifDia;

    /** Creates a new instance of PipeOrifDia */
    public PipeOrifDia(byte[] data,int offset, int niFormat, int dataOrder) throws IOException {
        setPipeDia(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setOrifDia(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return SIZE*C12ParseUtils.getNonIntegerSize(niFormat);
    }

    public Number getPipeDia() {
        return pipeDia;
    }

    public void setPipeDia(Number pipeDia) {
        this.pipeDia = pipeDia;
    }

    public Number getOrifDia() {
        return orifDia;
    }

    public void setOrifDia(Number orifDia) {
        this.orifDia = orifDia;
    }

}
