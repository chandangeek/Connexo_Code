package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.systemproperties.SystemProperty;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

public class SystemPropertyImpl implements SystemProperty {

    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String key;

    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String value;

    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName; // for auditing
    @SuppressWarnings("unused")
    private long version;

    private final transient DataModel dataModel;

    @Inject
    public SystemPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public enum Fields {
        PROP_KEY("key"),
        PROP_VALUE("value");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    public void update() {
        Save.UPDATE.save(this.dataModel, this);
    }
}
