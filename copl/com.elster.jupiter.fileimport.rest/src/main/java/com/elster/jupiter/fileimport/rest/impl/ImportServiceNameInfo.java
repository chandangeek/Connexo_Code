/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.ImportSchedule;

/**
 * Created by Lucian on 6/2/2015.
 */
public class ImportServiceNameInfo {
    public long id;
    public String name;

    public ImportServiceNameInfo() {
    }

    public ImportServiceNameInfo(ImportSchedule importSchedule) {
        this();
        this.id = importSchedule.getId();
        this.name = importSchedule.getName();
    }

}