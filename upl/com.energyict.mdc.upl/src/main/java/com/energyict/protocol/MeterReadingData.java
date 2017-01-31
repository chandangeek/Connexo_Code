/*
 * MeterReading.java
 *
 * Created on 24 februari 2003, 10:24
 */

package com.energyict.protocol;

import com.energyict.cbo.Quantity;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen
 */
@XmlRootElement
public class MeterReadingData implements java.io.Serializable {

    List<RegisterValue> registerValues = new ArrayList<RegisterValue>(); // of type RegisterValue

    /**
     * Creates a new instance of MeterReading
     */
    public MeterReadingData() {
    }

    public void add(RegisterValue registerValue) {
        registerValues.add(registerValue);
    }

    /**
     * @author Koen
     * @deprecated replace by getRegisterValues()
     */
    @XmlAttribute
    public List<RegisterValue> getReadings() {
        return registerValues;
    }

    /**
     * @author Koen
     * @deprecated replace by setRegisterValues()
     */
    public void setReadings(List<RegisterValue> registerValues) {
        this.registerValues = registerValues;
    }

    /* backwards compatibility */
    public List<Quantity> getQuantities() {
        List<Quantity> result = new ArrayList<Quantity>(registerValues.size());
        for (RegisterValue registerValue : registerValues) {
            result.add(registerValue.getQuantity());
        }
        return result;
    }

    public List<RegisterValue> getRegisterValues() {
        return registerValues;
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        for (RegisterValue registerValue : registerValues) {
            strBuff.append(registerValue.toString() + "\n");
        }
        return strBuff.toString();
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
