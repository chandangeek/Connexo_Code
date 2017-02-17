/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface FileImportHistory {

    ImportSchedule getImportSchedule();

    String getUserName();

    Instant getUploadTime();

    void setImportSchedule(ImportSchedule importSchedule);

    void setUserName(String userName);

    void setUploadTime(Instant uploadTime);
}
