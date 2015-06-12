package com.elster.jupiter.export;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

public interface DataExportProperty extends HasName {

    ExportTask getTask();

    String getDisplayName();

    Object getValue();

    boolean useDefault();

    void setValue(Object value);

    void save();

    boolean instanceOfSpec(PropertySpec spec);
}
