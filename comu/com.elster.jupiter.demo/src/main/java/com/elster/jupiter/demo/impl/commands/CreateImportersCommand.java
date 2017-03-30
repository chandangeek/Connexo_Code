/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.AddImportScheduleToAppServerPostBuilder;
import com.elster.jupiter.demo.impl.templates.FileImporterTpl;

import javax.inject.Inject;

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
