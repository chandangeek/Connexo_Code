/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.UkHubSecurityProvider;

import java.util.ArrayList;
import java.util.List;

public class AS300Properties extends DlmsProtocolProperties {

    public static final String DEFAULT_AS300_CLIENT_MAC_ADDRESS = "64";
    public static final String DEFAULT_AS300_LOGICAL_DEVICE_ADDRESS = "45";

    private static final String LOGBOOK_SELECTOR = "LogbookSelector";
    private static final String DEFAULT_LOGBOOK_SELECTOR = "-1";

    private static final int FIRMWARE_CLIENT = 0x50;
    private static final String MaxReceivePduSize = "128";

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(DlmsProtocolProperties.ADDRESSING_MODE);
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.FORCED_DELAY);
        optional.add(DlmsProtocolProperties.DELAY_AFTER_ERROR);
        optional.add(DlmsProtocolProperties.SYSTEM_IDENTIFIER);
        optional.add(DlmsProtocolProperties.INFORMATION_FIELD_SIZE);
        optional.add(DlmsProtocolProperties.MAX_REC_PDU_SIZE);
        optional.add(DlmsProtocolProperties.RETRIES);
        optional.add(DlmsProtocolProperties.TIMEOUT);
        optional.add(DlmsProtocolProperties.BULK_REQUEST);
        optional.add(DlmsProtocolProperties.ROUND_TRIP_CORRECTION);
        optional.add(UkHubSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        optional.add(UkHubSecurityProvider.DATATRANSPORT_ENCRYPTIONKEY);
        optional.add(LOGBOOK_SELECTOR);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        required.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return required;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {
    }

    @ProtocolProperty
    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_AS300_LOGICAL_DEVICE_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_AS300_CLIENT_MAC_ADDRESS);
    }

    @ProtocolProperty
    @Override
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, "1");
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if(super.securityProvider == null){
            setSecurityProvider(new UkHubSecurityProvider(getProtocolProperties()));
        }
        return super.securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider){
        super.securityProvider = securityProvider;
    }

    @ProtocolProperty
    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, MaxReceivePduSize);
    }

    /**
     * Getter for the LogBookSelector bitmask
     *
     * @return the bitmask, containing which event logbooks that should be read out.
     */
    public int getLogbookSelector() {
        return getIntProperty(LOGBOOK_SELECTOR, DEFAULT_LOGBOOK_SELECTOR);
    }
}
