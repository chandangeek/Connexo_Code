/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (10:08)
 */
public enum DlmsTranslationKeys implements TranslationKey {

    VALIDATE_INVOKE_ID("dlms.validate.invoke.id", "Validate invoke id"),
    AARQ_TIMEOUT_PROP_NAME("dlms.aarq.timeout", "AARQ timeout"),
    READCACHE_PROPERTY("dlms.read.cache", "Read cache"),
    MAX_REC_PDU_SIZE("dlms.max.rec.pdu.size", "Max REC PDU size"),
    CLIENT_MAC_ADDRESS("dlms.protocol.property.client.mac.address", "Client mac address"),
    SERVER_MAC_ADDRESS("dlms.protocol.property.server.mac.address", "Server mac address"),
    DEVICE_ID_OBISCODE_KEY("dlms.protocol.property.device.id.obiscode", "DeviceIdObisCode"),
    ;

    private final String key;
    private final String defaultFormat;

    DlmsTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}