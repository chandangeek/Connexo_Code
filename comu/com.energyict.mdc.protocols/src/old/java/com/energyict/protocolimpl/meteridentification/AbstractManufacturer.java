/*
 * AbstractManufacturer.java
 *
 * Created on 15 april 2005, 11:53
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public abstract class AbstractManufacturer {

    String signOnString=null;

    public abstract String getMeterProtocolClass() throws IOException;
    public abstract String[] getMeterSerialNumberRegisters() throws IOException;
    public abstract String getMeterDescription() throws IOException;
    public abstract String getManufacturer() throws IOException;

    public String toString() {
        try {
           return "AbstractManufacturer, "+getMeterProtocolClass()+", "+getMeterDescription()+", "+getManufacturer();
        }
        catch(IOException e) {
            return e.toString();
        }
    }

    /**
     * Getter for property signOnString.
     * @return Value of property signOnString.
     */
    public java.lang.String getSignOnString() {
        return signOnString;
    }

    /**
     * Setter for property signOnString.
     * @param signOnString New value of property signOnString.
     */
    public void setSignOnString(java.lang.String signOnString) {
        this.signOnString = signOnString;
    }

    /** Creates a new instance of AbstractManufacturer */
    public AbstractManufacturer() {
    }

    public String getResourceName() throws IOException {
        return null;
    }

}
