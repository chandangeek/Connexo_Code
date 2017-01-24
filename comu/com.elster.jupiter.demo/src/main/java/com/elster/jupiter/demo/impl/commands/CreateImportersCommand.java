package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.AddImportScheduleToAppServerPostBuilder;
import com.elster.jupiter.demo.impl.templates.FileImporterTpl;

import javax.inject.Inject;

/**
 *
 * Purpose for this command is to install default Importers in the demo system, with their
 * respective properties so they can be used without additional configuration.
 *
 * Copyrights EnergyICT
 * Date: 15/09/2015
 * Time: 10:47
 */
public class CreateImportersCommand extends CommandWithTransaction {

    private AppService appService;
    private String appServerName;
    private AddImportScheduleToAppServerPostBuilder postBuilder;

    @Inject
    public CreateImportersCommand(AppService appService){
        this.appService = appService;
    }

    public void setAppServerName(String appServerName) {
        this.appServerName = appServerName;
    }

    public void run(){
        postBuilder = null;
        if (appServerName != null){
            appService.findAppServer(appServerName).ifPresent(this::initPostBuilder);
        }
        for (FileImporterTpl importerTpl : FileImporterTpl.values()) {
            // Create an Importschedule and add it to the default appServer
            Builders.from(importerTpl).withPostBuilder(postBuilder).get();
        }
    }

    private void initPostBuilder(AppServer appServer){
        this.postBuilder = new AddImportScheduleToAppServerPostBuilder(appServer);
    }
}
