package com.energyict.protocolimpl.dlms.prime;

import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocol.support.SerialNumberSupport;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 16:43
 * Author: khe
 */
public class AS330D extends AbstractPrimeMeter implements SerialNumberSupport{

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:25 +0200 (Thu, 26 Nov 2015)$";
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
