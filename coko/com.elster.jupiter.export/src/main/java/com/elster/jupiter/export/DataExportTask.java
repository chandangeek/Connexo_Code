package com.elster.jupiter.export;

import com.elster.jupiter.util.HasName;

public interface DataExportTask extends HasName {

    long getId();

    void activate();

    void deactivate();

    void save();
}
