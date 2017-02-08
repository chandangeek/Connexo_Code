/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.cron.CronExpressionParser;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * {@link Builder} for AppServer
 */
public class AppServerBuilder extends NamedBuilder<AppServer, AppServerBuilder> {

    private static final String DEFAULT_EXPORTPATH = "D:\\Data-Export";
    private static final String DEFAULT_IMPORTPATH = "D:\\Data-Import";

    private final AppService appService;
    private final CronExpressionParser cronExpressionParser;
    private final MessageService messageService;
    private final DataExportService dataExportService;

    private int threadCount = 1;
    private String importPath = DEFAULT_IMPORTPATH;
    private String exportPath = DEFAULT_EXPORTPATH;

    @Inject
    public AppServerBuilder(AppService appService,
                            CronExpressionParser cronExpressionParser,
                            MessageService messageService,
                            DataExportService dataExportService) {
        super(AppServerBuilder.class);
        this.appService = appService;
        this.cronExpressionParser = cronExpressionParser;
        this.messageService = messageService;
        this.dataExportService = dataExportService;
    }

    /**
     * Sets the thread count
     * @param count the new value
     * @return itself (allowing method chaining)
     */
    public AppServerBuilder withThreadCount(int count){
        this.threadCount = count;
        return this;
    }

    /**
     * Sets the import path
     * @param importPath the new value
     * @return itself (allowing method chaining)
     */
    public AppServerBuilder withImportPath(String importPath){
        this.importPath = importPath;
        return this;
    }

    /**
     * Sets the export path
     * @param exportPath the new value
     * @return itself (allowing method chaining)
     */
    public AppServerBuilder withExportPath(String exportPath){
        this.exportPath = exportPath;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AppServer> find() {
        return appService.findAppServer(getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppServer create() {
        Log.write(this);
        AppServer appServer = appService.createAppServer(getName(), cronExpressionParser.parse("0 0 * * * ? *").get());
        Path importDirectory = Paths.get(importPath);
        appServer.setImportDirectory(importDirectory);

        messageService.getNonSystemManagedSubscribers().stream().forEach(subscriber -> appServer.createSubscriberExecutionSpec(subscriber, threadCount));
        dataExportService.setExportDirectory(appServer, Paths.get(exportPath));
        appServer.activate();
        return appServer;
    }

}