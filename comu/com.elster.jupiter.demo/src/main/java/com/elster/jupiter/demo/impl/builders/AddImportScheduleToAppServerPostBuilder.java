package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.fileimport.ImportSchedule;

import java.util.function.Consumer;

/**
 * Copyrights EnergyICT
 * Date: 29/09/2015
 * Time: 16:22
 */
public class AddImportScheduleToAppServerPostBuilder implements Consumer<ImportSchedule> {

    private final AppServer appServer;

    public AddImportScheduleToAppServerPostBuilder(AppServer appServer){
       this.appServer = appServer;
    }

    @Override
    public void accept(ImportSchedule importSchedule) {
        appServer.addImportScheduleOnAppServer(importSchedule);
    }
}
