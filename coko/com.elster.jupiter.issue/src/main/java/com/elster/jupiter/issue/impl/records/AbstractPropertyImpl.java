package com.elster.jupiter.issue.impl.records;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.PersistentProperty;
import com.elster.jupiter.properties.PropertySpec;

public abstract class AbstractPropertyImpl implements PersistentProperty {

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 256, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_256 + "}")
    private String value;

    // Audit fields
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;

    public String getName() {
        return name;
    }

    public Object getValue() {
        return getPropertySpec().getValueFactory().fromStringValue(value);
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        this.value = getPropertySpec().getValueFactory().toStringValue(value);
    }

    protected abstract PropertySpec getPropertySpec();

    protected AbstractPropertyImpl init(String name, Object value) {
        this.name = name;
        setValue(value);
        return this;
    }
}
