/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.UkHubSecurityProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides property information for the InHomeDisplay
 */
public class InHomeDisplayProperties extends DlmsProtocolProperties {

    public static final String DEFAULT_IHD_CLIENT_MAC_ADDRESS = "64";

    /**
     * Uses the same logical device address as the HUB!!
     */
    public static final String DEFAULT_IHD_LOGICAL_DEVICE_ADDRESS = "1";

    private SecurityProvider securityProvider;


    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
        //TODO implement proper functionality.
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(DlmsProtocolProperties.ADDRESSING_MODE);
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.FORCED_DELAY);
        optional.add(DlmsProtocolProperties.DELAY_AFTER_ERROR);
        optional.add(DlmsProtocolProperties.INFORMATION_FIELD_SIZE);
        optional.add(DlmsProtocolProperties.MAX_REC_PDU_SIZE);
        optional.add(DlmsProtocolProperties.RETRIES);
        optional.add(DlmsProtocolProperties.TIMEOUT);
        optional.add(DlmsProtocolProperties.ROUND_TRIP_CORRECTION);
        optional.add(DlmsProtocolProperties.BULK_REQUEST);
        optional.add(DlmsProtocolProperties.CIPHERING_TYPE);
        optional.add(DlmsProtocolProperties.NTA_SIMULATION_TOOL);
        optional.add(UkHubSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        optional.add(UkHubSecurityProvider.DATATRANSPORT_ENCRYPTIONKEY);
        return optional;
    }

    public List<String> getRequiredKeys() {
        ArrayList<String> required = new ArrayList<String>();
        required.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return required;
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_IHD_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_IHD_LOGICAL_DEVICE_ADDRESS);
    }

    public void setSecurityProvider(final UkHubSecurityProvider ukHubSecurityProvider) {
        this.securityProvider = ukHubSecurityProvider;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new UkHubSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }
}