package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class AS330D extends AbstractPrimeMeter implements SerialNumberSupport {

    public AS330D(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Thu Nov 26 15:23:57 2015 +0200 $";
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS330D DLMS (PRIME1.5)";
    }

    @Override
    public String getSerialNumber() {
        try {
            return super.readSerialNumber();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, super.getSession().getProperties().getRetries() + 1);
        }
    }

}