/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.upl.issue.Warning;

public interface FileImportLogger<T extends FileImportRecord>
        extends com.elster.jupiter.fileimport.csvimport.FileImportLogger<T> {
    void warning(Warning warning);
}
