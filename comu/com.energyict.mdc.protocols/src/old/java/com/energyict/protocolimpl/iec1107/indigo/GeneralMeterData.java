/*
 * GeneralMeterData.java
 *
 * Created on 15 juli 2004, 11:41
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class GeneralMeterData extends AbstractLogicalAddress {

    // partially implemented...

    int nrOfMDResets;

    /** Creates a new instance of GeneralMeterData */
    public GeneralMeterData(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }
    public String toString() {
        return "GemeralMeterData: nrOfMDResets="+getNrOfMDResets();
    }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        setNrOfMDResets(ProtocolUtils.getInt(data,0,2));
    }

    /**
     * Getter for property nrOfMDResets.
     * @return Value of property nrOfMDResets.
     */
    public int getNrOfMDResets() {
        return nrOfMDResets;
    }

    /**
     * Setter for property nrOfMDResets.
     * @param nrOfMDResets New value of property nrOfMDResets.
     */
    public void setNrOfMDResets(int nrOfMDResets) {
        this.nrOfMDResets = nrOfMDResets;
    }

}
