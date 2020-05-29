/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import java.io.IOException;
import java.util.logging.FileHandler;

public class LogFileHandler extends FileHandler {
    private final String file;

    public LogFileHandler(String logDir, String file, int limit, int count, boolean append)
            throws IOException, SecurityException {
        super(logDir + file, limit, count, append);
        this.file = file;
    }

    public String getFile() {
        return file;
    }
}
