package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;

import java.util.List;

public interface FileImportDescription<T extends FileImportRecord> {

    T getFileImportRecord();

    List<FileImportField<?>> getFields(T record);

}
