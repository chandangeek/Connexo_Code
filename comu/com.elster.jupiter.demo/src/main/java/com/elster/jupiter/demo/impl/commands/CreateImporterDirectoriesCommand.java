package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.AddImportScheduleToAppServerPostBuilder;
import com.elster.jupiter.demo.impl.templates.FileImporterTpl;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * Purpose for this command is to install default Importers in the demo system, with their
 * respective properties so they can be used without additional configuration.
 *
 * Copyrights EnergyICT
 * Date: 15/09/2015
 * Time: 10:47
 */
public class CreateImporterDirectoriesCommand {



    private String baseImportPath;
    private FileImportService fileImportService;

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
//            importSchedule.
        }


    }

}
