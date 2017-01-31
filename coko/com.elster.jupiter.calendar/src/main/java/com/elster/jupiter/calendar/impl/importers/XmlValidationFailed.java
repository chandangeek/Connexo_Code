/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.calendar.impl.MessageSeeds;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.JAXBException;

class XmlValidationFailed extends LocalizedException {

    public XmlValidationFailed(Thesaurus thesaurus, JAXBException cause, String message) {
            super(thesaurus, MessageSeeds.VALIDATION_OF_FILE_FAILED_WITH_DETAIL, cause, message);
    }

    public XmlValidationFailed(Thesaurus thesaurus, JAXBException cause) {
            super(thesaurus, MessageSeeds.VALIDATION_OF_FILE_FAILED, cause);
    }
}
