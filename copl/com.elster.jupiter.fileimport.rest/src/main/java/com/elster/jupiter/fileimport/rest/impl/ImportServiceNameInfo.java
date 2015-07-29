package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Created by Lucian on 6/2/2015.
 */
public class ImportServiceNameInfo {
    public long id;
    public String name;

    public ImportServiceNameInfo() {

    }

    public ImportServiceNameInfo(ImportSchedule importSchedule, Thesaurus thesaurus) {

        this.id = importSchedule.getId();
        this.name = importSchedule.getName();

    }
}
