package com.elster.jupiter.fileimport.impl;

import org.osgi.service.log.LogService;

import java.io.File;

public class DefaultFileHandler implements FileHandler {

    @Override
    public void handle(File file) {
        Bus.getLogService().log(LogService.LOG_INFO, file.toString());
        System.out.println(file.toString());
    }
}
