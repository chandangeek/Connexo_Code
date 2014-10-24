package com.elster.jupiter.export;

import com.elster.jupiter.util.HasName;

public interface DataExportProperty extends HasName {

    ReadingTypeDataExportTask getTask();

    String getDisplayName();

    Object getValue();

    void setValue(Object value);
}
