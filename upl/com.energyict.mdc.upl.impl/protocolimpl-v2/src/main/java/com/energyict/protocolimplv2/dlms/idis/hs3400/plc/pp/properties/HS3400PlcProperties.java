package com.energyict.protocolimplv2.dlms.idis.hs3400.plc.pp.properties;

import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300Properties;

public class HS3400PlcProperties extends HS3300Properties {

    public HS3400PlcProperties(PropertySpecService propertySpecService, NlsService nlsService, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(propertySpecService, nlsService, certificateWrapperExtractor);
    }

    @Override
    public byte[] getSystemIdentifier() {
        // Property CallingAPTitle is used as system identifier in the AARQ
        final String callingAPTitle = getProperties().getTypedProperty(IDIS.CALLING_AP_TITLE, IDIS.CALLING_AP_TITLE_DEFAULT).trim();
        if (callingAPTitle.isEmpty()) {
            return super.getSystemIdentifier();
        } else {
            try {
                return ProtocolTools.getBytesFromHexString(callingAPTitle, "");
            } catch (Exception e) {
                throw DeviceConfigurationException.invalidPropertyFormat(IDIS.CALLING_AP_TITLE, callingAPTitle, "Should be a hex string of 16 characters");
            }
        }
    }

}
