package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;

import java.io.IOException;
import java.util.Date;


/** 
 * RecDigit Cct meter is a pulse counter. 
 */

public class RecDigitCdtE extends RecDigitCdt {
    
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactoryCdtE(this));
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    /* meter does not have the time */
    public Date getTime() throws IOException {
        return new Date();
    }

    @Override
    public String getProtocolDescription() {
        return "Enerdis Recdigit CDT E";
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }    
    
}
