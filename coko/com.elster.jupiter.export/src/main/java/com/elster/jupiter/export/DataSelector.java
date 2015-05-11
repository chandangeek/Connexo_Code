package com.elster.jupiter.export;

import java.time.Instant;
import java.util.List;

public interface DataSelector {

    List<ExportData> selectData(Instant triggerTime);
}
