package com.energyict.protocolimplv2.security;

import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;

public class WebRTUZ3DlmsSecuritySupport extends DlmsSecuritySupport {

    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportKey";

    @Inject
    public WebRTUZ3DlmsSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected String getDataTransportEncryptionKeyLegacyPropertyName() {
        return DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME;
    }
}
