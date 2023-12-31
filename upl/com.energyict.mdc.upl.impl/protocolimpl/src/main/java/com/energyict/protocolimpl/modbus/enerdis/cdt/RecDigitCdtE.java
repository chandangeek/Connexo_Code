package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import java.io.IOException;
import java.util.Date;

/**
 * RecDigit Cct meter is a pulse counter.
 */

public class RecDigitCdtE extends RecDigitCdt {

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactoryCdtE(this));
    }

    public RecDigitCdtE(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public int getProfileInterval() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Date getTime() throws IOException {
        return new Date();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public String getProtocolDescription() {
        return "Enerdis Recdigit CDT E Modbus";
    }

}