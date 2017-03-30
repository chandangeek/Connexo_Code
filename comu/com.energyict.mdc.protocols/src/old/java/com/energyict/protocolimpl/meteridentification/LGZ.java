/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
public class LGZ extends AbstractManufacturer {

    /** Creates a new instance of LGZ */
    public LGZ() {
    }

    public String getManufacturer() throws IOException {
        return "Landes & Gyr";
    }

    public String getMeterProtocolClass() throws IOException {

        if ((getSignOnString() == null) || (getSignOnString().indexOf("\\2") >= 0) || (getSignOnString().indexOf("\2") >= 0)) {
            return "com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD";
        }

        throw new IOException("Unknown metertype for signonstring "+getSignOnString());
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        if ((getSignOnString()==null) || (getSignOnString().indexOf("\\2")>=0) || (getSignOnString().indexOf("\2")>=0))
           return new String[]{"0.0.0","00","0"};

        throw new IOException("Unknown metertype for signonstring "+getSignOnString());
    }

    public String getMeterDescription() throws IOException {
        if ((getSignOnString()==null) || (getSignOnString().indexOf("\\2")>=0) || (getSignOnString().indexOf("\2")>=0))
           return "DLMS Siemens ZMD";

        throw new IOException("Unknown metertype for signonstring "+getSignOnString());
    }

}
