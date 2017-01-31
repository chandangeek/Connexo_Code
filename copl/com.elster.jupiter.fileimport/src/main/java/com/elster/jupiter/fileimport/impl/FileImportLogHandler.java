/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;


import java.util.logging.Handler;

public interface FileImportLogHandler {

    Handler asHandler();

    void saveLogEntries();

}
