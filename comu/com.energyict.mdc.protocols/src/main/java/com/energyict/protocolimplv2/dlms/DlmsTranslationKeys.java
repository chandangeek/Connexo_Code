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