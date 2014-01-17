/*
 * MeterIdentification.java
 *
 * Created on 2 september 2005, 14:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core;

import com.energyict.mdc.protocol.api.inbound.MeterType;
/**
 *
 * @author Koen
 */
public class MeterIdentification {

    MeterType meterType;

    /** Creates a new instance of MeterIdentification */
    public MeterIdentification(MeterType meterType) {
        this.meterType=meterType;
    }

    public boolean isMeter() {
        return meterType.getReceivedIdent().compareTo("EMS75")==0;
    }
    public boolean isRecorder() {
        return meterType.getReceivedIdent().compareTo("EMS50")==0;
    }

}
