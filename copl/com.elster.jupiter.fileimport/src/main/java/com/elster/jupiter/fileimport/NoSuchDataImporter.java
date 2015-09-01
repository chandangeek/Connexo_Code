package com.elster.jupiter.fileimport;

import com.elster.jupiter.fileimport.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Created by Lucian on 5/11/2015.
 */
public class NoSuchDataImporter extends LocalizedException {
    public NoSuchDataImporter(Thesaurus thesaurus, String importerName) {
        super(thesaurus, MessageSeeds.NO_SUCH_IMPORTER, importerName);
    }
}
