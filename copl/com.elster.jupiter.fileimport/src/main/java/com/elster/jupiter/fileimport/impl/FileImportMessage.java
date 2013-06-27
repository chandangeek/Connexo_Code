package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
class FileImportMessage {

    public long fileImportId;

    private FileImportMessage() {
    }

    public FileImportMessage(FileImport fileImport) {
        this.fileImportId = fileImport.getId();
    }

}
