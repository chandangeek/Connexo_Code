/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IntervalValue.java
 *
 * Created on 15 juli 2003, 10:24
 */

package com.energyict.mdc.protocol.api.device.data;


import com.elster.jupiter.metering.ReadingQualityType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Koen
 */
public class IntervalValue implements java.io.Serializable {

    Number number;

    int protocolStatus;

    @Deprecated
    int eiStatus;

    /**
     * A list of the CIM codes of the reading qualities that apply to this interval value.
     * E.g. "1.2.1001" is power down.
     */
    private Set<ReadingQualityType> readingQualityTypes = new HashSet<>();

    /**
     * Creates a new instance of IntervalValue
     */
    protected IntervalValue(Number number) {
        this(number, 0, 0);
    }

    /**
     * @deprecated use readingQualityTypes instead of eiStatus
     */
    @Deprecated
    public IntervalValue(Number number, int protocolStatus, int eiStatus) {
        this.number = number;
        this.protocolStatus = protocolStatus;
        this.eiStatus = eiStatus;
        this.generateReadingQualities(eiStatus);
    }

    public IntervalValue(Number number, int protocolStatus, Set<ReadingQualityType> readingQualityTypes) {
        this.number = number;
        this.protocolStatus = protocolStatus;
        this.readingQualityTypes = readingQualityTypes;
    }

    /**
     * Generate the proper reading quality CIM codes based on the given eiStatus.
     */
    private void generateReadingQualities(int eiStatus) {
        readingQualityTypes.addAll(IntervalFlagMapper.map(eiStatus));
    }

    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    public int getProtocolStatus() {
        return protocolStatus;
    }

    protected void setProtocolStatus(int protocolStatus) {
        this.protocolStatus = protocolStatus;
    }

    /**
     * @deprecated use readingQualityTypes instead of eiStatus
     */
    @Deprecated
    public int getEiStatus() {
        return eiStatus;
    }

    /**
     * @deprecated use readingQualityTypes instead of eiStatus
     */
    // KV 25082004
    @Deprecated
    protected void setEiStatus(int eiStatus) {
        this.eiStatus = eiStatus;
        readingQualityTypes.clear();
        generateReadingQualities(eiStatus);
    }

    /**
     * A list of the CIM codes of the reading qualities that apply to this interval value.
     */
    public Set<ReadingQualityType> getReadingQualityTypes() {
        return readingQualityTypes;
    }

    public void setReadingQualityTypes(Set<ReadingQualityType> readingQualityTypes) {
        this.readingQualityTypes = readingQualityTypes;
    }

    public void addReadingQualityType(ReadingQualityType readingQualityType) {
        getReadingQualityTypes().add(readingQualityType);
    }

    public void addReadingQualityTypes(Set<ReadingQualityType> readingQualityTypes) {
        getReadingQualityTypes().addAll(readingQualityTypes);
    }

    public String toString() {

        StringBuilder readingQualitiesDescription = new StringBuilder();
        for (ReadingQualityType readingQualityType : getReadingQualityTypes()) {
            if (readingQualitiesDescription.length() > 0) {
                readingQualitiesDescription.append(", ");
            }
            readingQualitiesDescription.append(readingQualityType.getCode());
        }

        return number + " " + protocolStatus + " " + eiStatus + " ReadingQualities: " + readingQualitiesDescription.toString();
    }
}