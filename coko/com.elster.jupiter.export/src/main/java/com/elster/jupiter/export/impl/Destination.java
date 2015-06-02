package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.FormattedExportData;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 2/06/2015
 * Time: 15:39
 */
public interface Destination {
    void send(List<FormattedExportData> data);
}
