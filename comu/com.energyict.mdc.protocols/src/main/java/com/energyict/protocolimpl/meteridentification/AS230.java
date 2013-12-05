/*
 * LGZ.java
 *
 * Created on 15 april 2005, 11:53
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;


/**
 *
 * @author  Koen
 */
public class AS230 extends AbstractManufacturer {

    /** Creates a new instance of AS230 */
    public AS230() {
    }

    public String getManufacturer() throws IOException {
        return "Elster AS230";
    }

    public String getMeterProtocolClass() throws IOException {

        if ((getSignOnString()==null) || (getSignOnString().indexOf("\\2")>=0) || (getSignOnString().indexOf("\2")>=0))
           return "com.energyict.protocolimpl.iec1107.abba230.ABBA230";

        throw new IOException("Unknown metertype for signonstring "+getSignOnString());
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        if ((getSignOnString()==null) || (getSignOnString().indexOf("\\2")>=0) || (getSignOnString().indexOf("\2")>=0))
           return new String[]{"0.0.0","00","0"};

        throw new IOException("Unknown metertype for signonstring "+getSignOnString());
    }

    public String getMeterDescription() throws IOException {
        if ((getSignOnString()==null) || (getSignOnString().indexOf("\\2")>=0) || (getSignOnString().indexOf("\2")>=0))
           return "Elster AS230";

        throw new IOException("Unknown metertype for signonstring "+getSignOnString());
    }

}
