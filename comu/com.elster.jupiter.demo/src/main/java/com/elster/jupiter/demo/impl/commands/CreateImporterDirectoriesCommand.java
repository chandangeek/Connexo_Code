package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Purpose for this command is to create directories for all available importers
 * <p>
 * Copyrights EnergyICT
 * Date: 10/10/2016
 * Time: 10:47
 */
public class CreateImporterDirectoriesCommand {
    private final FileImportService fileImportService;
    private final AppService appService;

    private String baseImportPath;
    private static final Logger LOGGER = Logger.getLogger(CreateImporterDirectoriesCommand.class.getName());
    private Path basePath;

    @Inject
    public CreateImporterDirectoriesCommand(FileImportService fileImportService, AppService appService) {
        this.fileImportService = fileImportService;
        this.appService = appService;
    }

    public void setBaseImportPath(String baseImportPath) {
        this.baseImportPath = baseImportPath;
    }

    public void run() {
        if (this.baseImportPath != null) {
            this.basePath = Paths.get(this.baseImportPath);
        } else {
            this.basePath = this.appService.getAppServer()
                    .orElseThrow(() -> new UnableToCreate("Base path was not specified and current machine has no active appserver."))
                    .getImportDirectory()
                    .orElseThrow(() -> new UnableToCreate("Base path was not specified and appserver on current machine has no import directory."));
        }
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new UnableToCreate("Unable to create base importer directory");
        }
        for (ImportSchedule importSchedule : this.fileImportService.getImportSchedules()) {
            createDirectory(importSchedule.getImportDirectory());
            createDirectory(importSchedule.getSuccessDirectory());
            createDirectory(importSchedule.getFailureDirectory());
            createDirectory(importSchedule.getInProcessDirectory());
            importSchedule.setActive(Stream.of(importSchedule.getImportDirectory(),
                    importSchedule.getInProcessDirectory(),
                    importSchedule.getSuccessDirectory(),
                    importSchedule.getFailureDirectory())
                    .map(this.basePath::resolve)
                    .allMatch(Files::exists));
            importSchedule.update();
        }
    }

    private void createDirectory(Path path) {
        try {
            if (path != null && !Files.exists(path)) {
                Files.createDirectories(this.basePath.resolve(path));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to create importer directory " + path);
        }
    }

}
