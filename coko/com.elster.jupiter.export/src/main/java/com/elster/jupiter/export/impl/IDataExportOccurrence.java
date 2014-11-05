package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 10:22
 */
public interface IDataExportOccurrence extends DataExportOccurrence {

    void persist();

    void update();
}
