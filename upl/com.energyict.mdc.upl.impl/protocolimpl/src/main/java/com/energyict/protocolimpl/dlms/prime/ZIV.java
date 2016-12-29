package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocol.support.SerialNumberSupport;

import java.io.IOException;

/**
 * Class for the PRIME meter ZIV 5CTM - E2C
 *
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class ZIV extends AbstractPrimeMeter  implements SerialNumberSupport{

    public ZIV(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getSerialNumber() {
        try {
            return super.readSerialNumber();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, super.getSession().getProperties().getRetries()+1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:39 +0200 (Thu, 26 Nov 2015)$";
    }

}