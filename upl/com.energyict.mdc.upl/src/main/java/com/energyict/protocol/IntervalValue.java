/*
 * IntervalValue.java
 *
 * Created on 15 juli 2003, 10:24
 */

package com.energyict.protocol;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Koen
 */
@XmlRootElement
public class IntervalValue implements Serializable {

    Number number;
    int protocolStatus;
    int eiStatus;

    /**
     * A list of the CIM codes of the reading qualities that apply to this interval value.
     * E.g. "1.2.1001" is power down.
     */
    private Set<String> readingQualityTypes = new HashSet<>();

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private IntervalValue() {
    }

    /**
     * Creates a new instance of IntervalValue
     */
    protected IntervalValue(Number number) {
        this(number, 0, 0);
    }

    public IntervalValue(Number number, int protocolStatus, int eiStatus) {
        this.number = number;
        this.protocolStatus = protocolStatus;
        this.eiStatus = eiStatus;
        this.generateReadingQualities(eiStatus);
    }

    public IntervalValue(Number number, int protocolStatus, Set<String> readingQualityTypes) {
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

    @XmlAttribute
    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    @XmlAttribute
    public int getProtocolStatus() {
        return protocolStatus;
    }

    protected void setProtocolStatus(int protocolStatus) {
        this.protocolStatus = protocolStatus;
    }

    @XmlAttribute
    public int getEiStatus() {
        return eiStatus;
    }


    // KV 25082004
    protected void setEiStatus(int eiStatus) {
        this.eiStatus = eiStatus;
        readingQualityTypes.clear();
        generateReadingQualities(eiStatus);
    }

    /**
     * A list of the CIM codes of the reading qualities that apply to this interval value.
     */
    public Set<String> getReadingQualityTypes() {
        return readingQualityTypes;
    }

    public void setReadingQualityTypes(Set<String> readingQualityTypes) {
        this.readingQualityTypes = readingQualityTypes;
    }

    public void addReadingQualityType(String readingQualityType) {
        getReadingQualityTypes().add(readingQualityType);
    }

    public void addReadingQualityTypes(Set<String> readingQualityTypes) {
        getReadingQualityTypes().addAll(readingQualityTypes);
    }

    public String toString() {

        StringBuilder readingQualitiesDescription = new StringBuilder();
        for (String readingQualityType : getReadingQualityTypes()) {
            if (readingQualitiesDescription.length() > 0) {
                readingQualitiesDescription.append(", ");
            }
            readingQualitiesDescription.append(readingQualityType);
        }

        return number + " " + protocolStatus + " " + eiStatus + (getReadingQualityTypes().isEmpty() ? "" : " ReadingQualities: " + readingQualitiesDescription.toString());
    }

}