/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;

import com.google.inject.Inject;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;

public final class MeterRoleImpl implements MeterRole {

    public enum Fields {
        KEY("key");
        ;
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String key;

    private final Thesaurus thesaurus;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public MeterRoleImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public MeterRoleImpl init(String nameKey) {
        this.key = nameKey;
        return this;
    }

    @Override
    public String getKey() {
        return this.key.substring(MetrologyConfigurationServiceImpl.METER_ROLE_KEY_PREFIX.length());
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(new SimpleTranslationKey(this.key, this.key)).format();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MeterRoleImpl that = (MeterRoleImpl) o;
        return this.key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key);
    }
}
