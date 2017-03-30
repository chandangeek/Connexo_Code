/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * GEC.java
 *
 * Created on 15 april 2005, 13:10
 */

package com.energyict.protocolimpl.meteridentification;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class GEC extends AbstractManufacturer {
/*
/GEC5090100100400@000<CR><LF>

GEC   Manufacturers id (We were GEC, then ABB now Elster, but the original
one has been kept for continuity)
5     Baud Rate ( 0 - 300, 1 - 600, 2 - 1200, 3 - 2400, 4 - 4800, 5 - 9600)
09    Master Unit Id (09 - A1700), 01 - 08 PPM)
010   Product Range (010 - A1700, 001 - PPM)
010   Device No. The Device No is used for the firmware issue.
04    Issue No.
*/
    /** Creates a new instance of GEC */
    public GEC() {
    }

    public String getManufacturer() throws IOException {
        return "Elster";
    }


    private String getMasterUnit() {
        byte[] sub = getSignOnString().getBytes();
        return new String(ProtocolUtils.getSubArray(sub,5,6));
    }
    private String getProductRange() {
        byte[] sub = getSignOnString().getBytes();
        return new String(ProtocolUtils.getSubArray(sub,7,9));
    }

    public String getMeterProtocolClass() throws IOException {
        if ((getMasterUnit().compareTo("09") == 0) && (getProductRange().compareTo("010") == 0))
           return "com.energyict.protocolimpl.iec1107.abba1700.ABBA1700";

        throw new IOException("Unknown metertype for signonstring "+getSignOnString());
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        if ((getMasterUnit().compareTo("09") == 0) && (getProductRange().compareTo("010") == 0))
           return null;

        throw new IOException("Unknown metertype for signonstring "+getSignOnString());
    }

    public String getMeterDescription() throws IOException {
        if ((getMasterUnit().compareTo("09") == 0) && (getProductRange().compareTo("010") == 0))
           return "IEC1107 FLAG Elster A1700";


        throw new IOException("Unknown metertype for signonstring "+getSignOnString());

    }

}
