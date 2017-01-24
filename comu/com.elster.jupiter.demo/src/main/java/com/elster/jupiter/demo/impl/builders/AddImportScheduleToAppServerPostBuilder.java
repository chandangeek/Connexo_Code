package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.fileimport.ImportSchedule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 29/09/2015
 * Time: 16:22
 */
public class AddImportScheduleToAppServerPostBuilder implements Consumer<ImportSchedule> {

    private final AppServer appServer;

    public AddImportScheduleToAppServerPostBuilder(AppServer appServer) {
        this.appServer = appServer;
    }

    @Override
    public void accept(ImportSchedule importSchedule) {
        Optional<Path> rootAppServerDirectory = this.appServer.getImportDirectory();
        importSchedule.setActive(rootAppServerDirectory.isPresent() && importersDirectoriesExist(importSchedule, rootAppServerDirectory.get()));
        importSchedule.update();
        appServer.addImportScheduleOnAppServer(importSchedule);
    }

    private boolean importersDirectoriesExist(ImportSchedule importSchedule, Path rootAppServerDirectory) {
        return Stream.of(importSchedule.getImportDirectory(), importSchedule.getInProcessDirectory(), importSchedule.getSuccessDirectory(), importSchedule.getFailureDirectory())
                .map(rootAppServerDirectory::resolve)
                .allMatch(Files::exists);
    }
}
