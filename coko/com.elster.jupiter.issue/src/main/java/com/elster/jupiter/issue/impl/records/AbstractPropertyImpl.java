/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.domain.util.AllowedChars;
import com.elster.jupiter.domain.util.HasOnlyWhiteListedCharacters;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.PersistentProperty;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public abstract class AbstractPropertyImpl implements PersistentProperty {

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.ALLOWED_SPECIAL_CHARS)
    private String name;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = Table.DESCRIPTION_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.ALLOWED_SPECIAL_CHARS)
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
