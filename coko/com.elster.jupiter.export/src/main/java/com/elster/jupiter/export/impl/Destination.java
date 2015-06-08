package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.FormattedExportData;

import java.util.List;

interface Destination {
    void send(List<FormattedExportData> data);
}
