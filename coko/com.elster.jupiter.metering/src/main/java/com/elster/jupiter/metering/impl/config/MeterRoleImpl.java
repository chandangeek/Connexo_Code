package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;

import com.google.inject.Inject;

import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.Optional;

public final class MeterRoleImpl implements MeterRole {

    public enum Fields {
        NAME("name");
        ;
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;

    private final Thesaurus thesaurus;

    @Inject
    public MeterRoleImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public MeterRoleImpl init(String name) {
        this.name = name;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        Optional<DefaultMeterRole> defaultMeterRole = DefaultMeterRole.from(this.name);
        if (defaultMeterRole.isPresent()) {
            return thesaurus.getFormat(defaultMeterRole.get()).format();
        } else {
            return thesaurus.getStringBeyondComponent(this.name, this.name);
        }
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

        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}
