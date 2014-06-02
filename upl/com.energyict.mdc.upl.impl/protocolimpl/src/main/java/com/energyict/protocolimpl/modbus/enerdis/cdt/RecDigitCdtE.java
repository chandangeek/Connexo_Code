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

    public String getProtocolVersion() {
        return "$Date$";
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }    
    
}
