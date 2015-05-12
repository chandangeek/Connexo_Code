package com.elster.jupiter.fileimport;

import com.elster.jupiter.util.HasName;

public interface FileImporterProperty extends HasName {

    String getDisplayName();

    Object getValue();

    boolean useDefault();

    void setValue(Object value);

    void save();
}
