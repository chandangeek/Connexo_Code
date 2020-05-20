package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.systemproperties.SystemProperty;
import com.elster.jupiter.systemproperties.SystemPropertyService;

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
    private final transient SystemPropertyService systemPropertyService;
    private PropertySpec propertySpec = null;

    @Inject
    public SystemPropertyImpl(DataModel dataModel,
                              SystemPropertyService systemPropertyService) {
        this.dataModel = dataModel;
        this.systemPropertyService = systemPropertyService;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getValueObject() {
        return getPropertySpec().getValueFactory().fromStringValue(value);
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public PropertySpec getPropertySpec(){
        if (propertySpec == null){
            propertySpec = systemPropertyService.findPropertySpec(key);
        }
        return propertySpec;
    }

    @Override
    public void update() {
        Save.UPDATE.save(this.dataModel, this);
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
}
