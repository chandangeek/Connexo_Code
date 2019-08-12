/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum AuditTranslationKeys implements TranslationKey {

    LINK_UNLINK_METER_PROPERTY_NAME("audit.link.unlink.meter.property", "Link/unlink meter and meter role"),
    LINK_METER_PROPERTY_VALUE_FROM("audit.link.meter.from", "Link ''{0}'' meter with ''{1}'' meter role from {2} (UTC)"),
    UNLINK_METER_PROPERTY_VALUE_FROM_UNTIL("audit.unlink.meter.from.until", "Unlink ''{0}'' meter with ''{1}'' meter role from {2} (UTC) until {3} (UTC)"),
    UNLINK_METER_PROPERTY_FROM("audit.unlink.meter.until", "Unlink ''{0}'' meter from {1} (UTC)"),
    ;

    private String key;
    private String defaultFormat;

    AuditTranslationKeys(String key, String defaultFormat) {
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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

}