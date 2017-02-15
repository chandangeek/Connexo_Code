/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

public class QualityOfServiceElement extends Structure {

    int precedence;
    int delay;
    int reliability;
    int peakThroughput;
    int meanThroughput;


    public QualityOfServiceElement(final byte[] berEncodedData) throws IOException {
        super(berEncodedData, 0, 0);
    }

    public final int getPrecedence() {
        return getDataType(0).intValue();
    }

    public final int getDelay() {
        return getDataType(1).intValue();
    }

    public final int getReliability() {
        return getDataType(2).intValue();
    }

    public final int getPeakThroughput() {
        return getDataType(3).intValue();
    }

    public final int getMeanThroughput() {
        return getDataType(4).intValue();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("QoS");
        sb.append("{precedence=").append(precedence);
        sb.append(", delay=").append(delay);
        sb.append(", reliability=").append(reliability);
        sb.append(", peakThroughput=").append(peakThroughput);
        sb.append(", meanThroughput=").append(meanThroughput);
        sb.append('}');
        return sb.toString();
    }

    public static final QualityOfServiceElement fromStructure(final Structure structure) throws IOException {
        return new QualityOfServiceElement(structure.getBEREncodedByteArray());
    }

}
