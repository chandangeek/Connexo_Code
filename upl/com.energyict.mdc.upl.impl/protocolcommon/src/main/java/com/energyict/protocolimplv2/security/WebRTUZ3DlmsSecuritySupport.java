package com.energyict.protocolimplv2.security;

/**
 * From function perspective, this class is same as DlmsSecuritySupport, but some of the properties have a different name.
 * <p/>
 *
 * @author sva
 * @since 3/10/2014 - 16:48
 */
public class WebRTUZ3DlmsSecuritySupport extends DlmsSecuritySupport {

    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportKey";

    @Override
    protected String getDataTransportEncryptionKeyLegacyPropertyName() {
        return DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME;
    }
}