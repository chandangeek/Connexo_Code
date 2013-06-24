package com.elster.jupiter.fileimport.impl;

import org.osgi.service.log.LogService;

import java.nio.file.Path;

/**
 * Copyrights EnergyICT
 * Date: 24/06/13
 * Time: 15:36
 */
public class DefaultPathHandler implements PathHandler {

    @Override
    public void handle(Path path) {
        Bus.getLogService().log(LogService.LOG_INFO, path.toString());

    }
}
