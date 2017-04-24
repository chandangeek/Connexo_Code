/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IntervalRecord.java
 *
 * Created on 19 september 2006, 11:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class IntervalRecord {

    private int[] values;
    private int nrOfChannels;

    /** Creates a new instance of IntervalRecord */
    public IntervalRecord(byte[] data, int nibbleOffset, int nrOfChannels) {
        this.setNrOfChannels(nrOfChannels);
        setValues(new int[nrOfChannels]);
        for (int i=0;i< nrOfChannels;i++) {
            int value;
            if ((nibbleOffset%2)==0) {
                value = (int)ProtocolUtils.getNibble(data,nibbleOffset+2)<<8;
                value |= (int)ProtocolUtils.getNibble(data,nibbleOffset)<<4;
                value |= (int)ProtocolUtils.getNibble(data,nibbleOffset+1);

            } else {
                value = (int)ProtocolUtils.getNibble(data,nibbleOffset)<<8;
                value |= (int)ProtocolUtils.getNibble(data,nibbleOffset+1)<<4;
                value |= (int)ProtocolUtils.getNibble(data,nibbleOffset+2);
            }
            nibbleOffset+=3;
            getValues()[i] = value;
        }
    }

    static public int size(int nrOfChannel) {
        return nrOfChannel*3;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("IntervalRecord: nrOfChannels="+getNrOfChannels());
        for (int i=0;i<getValues().length;i++) {
            strBuff.append(", ");
            strBuff.append("values["+i+"]="+getValues()[i]);
        }
        strBuff.append("\n");
        return strBuff.toString();
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

    public int getNrOfChannels() {
        return nrOfChannels;
    }

    public void setNrOfChannels(int nrOfChannels) {
        this.nrOfChannels = nrOfChannels;
    }


}
