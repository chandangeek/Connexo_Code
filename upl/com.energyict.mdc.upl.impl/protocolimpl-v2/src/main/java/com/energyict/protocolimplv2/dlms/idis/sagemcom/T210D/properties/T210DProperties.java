package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.properties;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130Properties;

/**
 * Created by cisac on 12/19/2016.
 */
public class T210DProperties extends AM130Properties {

    private final CertificateWrapperExtractor certificateWrapperExtractor;

    public T210DProperties(CertificateWrapperExtractor certificateWrapperExtractor) {
        this.certificateWrapperExtractor = certificateWrapperExtractor;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new T210DSecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), certificateWrapperExtractor, DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
        }
        return securityProvider;
    }
}
