/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

final class FileImporterPropertyImpl implements FileImporterProperty, PersistenceAware {

    private final DataModel dataModel;

    private String name;
    private String stringValue;
    private transient PropertySpec propertySpec;

    private Reference<ImportSchedule> importScheduleReference = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    FileImporterPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    FileImporterPropertyImpl init(ImportSchedule importSchedule, PropertySpec propertySpec, Object value) {
        this.importScheduleReference.set(importSchedule);
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        setValue(value);
        return this;
    }

    static FileImporterPropertyImpl from(DataModel dataModel, ImportSchedule importSchedule, String name, Object value) {
        return dataModel.getInstance(FileImporterPropertyImpl.class).init(importSchedule, importSchedule.getPropertySpec(name), value);
    }

    @Override
    public void postLoad() {
        propertySpec = importScheduleReference.get().getPropertySpec(name);
    }

    @Override
    public ImportSchedule getImportSchedule() {
        return importScheduleReference.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return getImportSchedule().getPropertyDisplayName(name);
    }

    @Override
    public Object getValue() {
        return getPropertySpec().getValueFactory().fromStringValue(stringValue);
    }

    private PropertySpec getPropertySpec() {
        if (propertySpec == null) {
            postLoad();
        }
        return propertySpec;
    }

    public void setValue(Object value) {
        if (BigDecimal.class.equals(getPropertySpec().getValueFactory().getValueType())) {
            this.stringValue = toStringValue(new BigDecimal(value.toString()));
            return;
        }
        this.stringValue = toStringValue(value);
    }

    @Override
    public boolean useDefault() {
        return stringValue == null;
    }

    @SuppressWarnings("unchecked")
    private String toStringValue(Object object) {
        return getPropertySpec().getValueFactory().toStringValue(object);
    }

    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }

    public void save() {
        dataModel.mapper(FileImporterProperty.class).update(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileImporterPropertyImpl that = (FileImporterPropertyImpl) o;

        return getImportSchedule().getId() == that.getImportSchedule().getId() && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
