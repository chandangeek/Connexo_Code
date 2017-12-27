/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;


import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;

import java.util.List;

public interface FileImportDescription<T extends FileImportRecord> {

    T getFileImportRecord();

    List<FileImportField<?>> getFields(T record);

    default boolean isSkipTrailingNulls(){
        return true;
    }
}
