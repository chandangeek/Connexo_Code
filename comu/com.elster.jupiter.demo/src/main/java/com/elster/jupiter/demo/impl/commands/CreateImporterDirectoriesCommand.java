package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Purpose for this command is to create directories for all available importers
 *
 * Copyrights EnergyICT
 * Date: 10/10/2016
 * Time: 10:47
 */
public class CreateImporterDirectoriesCommand {

    private String baseImportPath;
    private FileImportService fileImportService;
    private static final Logger LOGGER = Logger.getLogger(CreateImporterDirectoriesCommand.class.getName());

    @Inject
    public CreateImporterDirectoriesCommand(FileImportService fileImportService){
        this.fileImportService = fileImportService;
    }

    public void setBaseImportPath(String baseImportPath) {
        this.baseImportPath = baseImportPath;
    }

    public void run(){
        Path basePath = Paths.get(baseImportPath);
        try {
            Files.createDirectory(basePath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to create base importer directory");
        }
        for (ImportSchedule importSchedule : fileImportService.getImportSchedules()) {
            createDirectory(importSchedule.getImportDirectory());
            createDirectory(importSchedule.getSuccessDirectory());
            createDirectory(importSchedule.getFailureDirectory());
            createDirectory(importSchedule.getInProcessDirectory());
        }
    }

    private void createDirectory(Path path){
        try{
            if(path!=null){
                Files.createDirectory(path);
            }
        } catch (IOException e){
            LOGGER.log(Level.WARNING, "Unable to create importer directory " + path, e);
        }
    }

}
