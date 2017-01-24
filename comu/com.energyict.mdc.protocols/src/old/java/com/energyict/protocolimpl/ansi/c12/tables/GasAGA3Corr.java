/*
 * GasAGA3Corr.java
 *
 * Created on 27 oktober 2005, 10:03
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
public class GasAGA3Corr {


    private Number auxCorrFactor;
    private Number gasAGA3CorrFactor;
    private PipeOrifDia pipeOrifDia;
    private int tapUpDn;
    private GasPress gasPressParm;
    private GasTemp gasTempParm;

    /** Creates a new instance of GasAGA3Corr */
    public GasAGA3Corr(byte[] data,int offset,int niFormat,int dataOrder) throws IOException {
        setAuxCorrFactor(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setGasAGA3CorrFactor(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setPipeOrifDia(new PipeOrifDia(data,offset,niFormat,dataOrder));
        offset += PipeOrifDia.getSize(niFormat);
        setTapUpDn(C12ParseUtils.getInt(data,offset));
        offset++;
        setGasPressParm(new GasPress(data,offset,niFormat,dataOrder));
        offset += GasPress.getSize(niFormat);
        setGasTempParm(new GasTemp(data,offset,niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return C12ParseUtils.getNonIntegerSize(niFormat)*2+1+PipeOrifDia.getSize(niFormat)+GasPress.getSize(niFormat)+GasTemp.getSize(niFormat);
    }

    public Number getAuxCorrFactor() {
        return auxCorrFactor;
    }

    public void setAuxCorrFactor(Number auxCorrFactor) {
        this.auxCorrFactor = auxCorrFactor;
    }

    public Number getGasAGA3CorrFactor() {
        return gasAGA3CorrFactor;
    }

    public void setGasAGA3CorrFactor(Number gasAGA3CorrFactor) {
        this.gasAGA3CorrFactor = gasAGA3CorrFactor;
    }

    public PipeOrifDia getPipeOrifDia() {
        return pipeOrifDia;
    }

    public void setPipeOrifDia(PipeOrifDia pipeOrifDia) {
        this.pipeOrifDia = pipeOrifDia;
    }

    public int getTapUpDn() {
        return tapUpDn;
    }

    public void setTapUpDn(int tapUpDn) {
        this.tapUpDn = tapUpDn;
    }

    public GasPress getGasPressParm() {
        return gasPressParm;
    }

    public void setGasPressParm(GasPress gasPressParm) {
        this.gasPressParm = gasPressParm;
    }

    public GasTemp getGasTempParm() {
        return gasTempParm;
    }

    public void setGasTempParm(GasTemp gasTempParm) {
        this.gasTempParm = gasTempParm;
    }

}
