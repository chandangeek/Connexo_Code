package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Message wrapper around a FileImport id.
 */
@XmlRootElement
class FileImportMessage {

    public long fileImportId;

    @SuppressWarnings("unused")
	private FileImportMessage() {
    }

    public FileImportMessage(FileImportOccurrence fileImportOccurrence) {
        this.fileImportId = fileImportOccurrence.getId();
    }

}
