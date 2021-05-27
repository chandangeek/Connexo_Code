/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Message wrapper around a FileImport id.
 */
@XmlRootElement
class FileImportMessage {

    public long fileImportId;
    public String appServerName;

    @SuppressWarnings("unused")
	private FileImportMessage() {
    }

    public FileImportMessage(FileImportOccurrence fileImportOccurrence, String appServerName) {
        this.fileImportId = fileImportOccurrence.getId();
        this.appServerName = appServerName;
    }

}
