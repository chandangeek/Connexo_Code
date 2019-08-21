/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum AuditTranslationKeys implements TranslationKey {

    LINK_UNLINK_METER_PROPERTY_NAME("audit.link.unlink.meter.property", "Link/unlink meter and meter role"),
    METROLOGY("audit.metrology.property", "Metrology"),
    USAGEPINT_WITH_METER_PROPERTY_FROM("audit.usagepoint.meter.from", "''{0}'' meter link to ''{1}'' usage point with ''{2}'' meter role to from {3} (UTC)"),
    METROLOGY_FROM("audit.metrology.meter.from", "Metrology from {0} (UTC)"),
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