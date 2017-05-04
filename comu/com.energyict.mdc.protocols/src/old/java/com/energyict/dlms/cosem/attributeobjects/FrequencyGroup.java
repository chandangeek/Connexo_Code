/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Float32;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;

import java.io.IOException;

public class FrequencyGroup extends Structure {

    public static final int FS_INDEX = 0;
    public static final int FM_INDEX = 1;
    public static final int SNR_INDEX = 2;
    public static final int CREDIT_INDEX = 3;

    public FrequencyGroup(byte[] berEncodedByteArray) throws IOException {
        super(berEncodedByteArray, 0, 0);
    }

    public static FrequencyGroup createFrequencyGroup(long fs, long fm) {
        if ((fs != -1) && (fm != -1)) {
            try {
                Structure frequencyGroup = new Structure();
                frequencyGroup.addDataType(new Unsigned32(fs));
                frequencyGroup.addDataType(new Unsigned32(fm));
                return new FrequencyGroup(frequencyGroup.getBEREncodedByteArray());
            } catch (IOException e) {
                //Absorb
            }
        }
        return null;
    }

    public static FrequencyGroup createFrequencyGroup(long fs, long fm, float snr, float creditWeight) {
        if ((fs != -1) && (fm != -1)) {
            try {
                Structure frequencyGroup = new Structure();
                frequencyGroup.addDataType(new Unsigned32(fs));
                frequencyGroup.addDataType(new Unsigned32(fm));
                frequencyGroup.addDataType(new Float32(snr));
                frequencyGroup.addDataType(new Float32(creditWeight));
                return new FrequencyGroup(frequencyGroup.getBEREncodedByteArray());
            } catch (IOException e) {
                //Absorb
            }
        }
        return null;
    }

    public boolean isOldFormat() {
        return (nrOfDataTypes() == 2);
    }

    public long getFs() {
        return getDataType(FS_INDEX).longValue();
    }

    public long getFm() {
        return getDataType(FM_INDEX).longValue();
    }

    public float getSnr() {
        return isOldFormat() ? 0 : ((Float32) getDataType(SNR_INDEX)).getValue();
    }

    public float getCreditWeight() {
        return isOldFormat() ? 0 : ((Float32) getDataType(CREDIT_INDEX)).getValue();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(getFs()).append("Hz,");
        sb.append(getFm()).append("Hz");
        if (!isOldFormat()) {
            sb.append(",").append(getSnr());
            sb.append(",").append(getCreditWeight());
        }
        sb.append(")");
        return sb.toString();
    }

}