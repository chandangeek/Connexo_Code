package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.cron.CronExpressionParser;

import javax.inject.Inject;
import java.nio.file.Paths;

public class AppServerFactory extends NamedFactory<AppServerFactory, AppServer> {
    private final Store store;
    private final AppService appService;
    private final CronExpressionParser cronExpressionParser;
    private final MessageService messageService;
    private final DataExportService dataExportService;

    private int threadCount;
    private String exportPath;

    @Inject
    public AppServerFactory(Store store, AppService appService, CronExpressionParser cronExpressionParser, MessageService messageService, DataExportService dataExportService) {
        super(AppServerFactory.class);
        this.store = store;
        this.appService = appService;
        this.cronExpressionParser = cronExpressionParser;
        this.messageService = messageService;
        this.dataExportService = dataExportService;

        this.exportPath = "D:\\Data-Export";
        this.threadCount = 1;
    }

    public AppServerFactory withThreadCount(int count){
        this.threadCount = count;
        return this;
    }

    public AppServerFactory withExportPath(String exportPath){
        this.exportPath = exportPath;
        return this;
    }

    @Override
    public AppServer get() {
        Log.write(this);
        AppServer appServer = appService.findAppServer(getName()).orElse(appService.createAppServer(getName(), cronExpressionParser.parse("0 0 * * * ? *").get()));
        messageService.getSubscribers().stream().forEach(subscriber -> appServer.createSubscriberExecutionSpec(subscriber, this.threadCount));
        dataExportService.setExportDirectory(appServer, Paths.get(exportPath));
        appServer.activate();
        store.add(AppServer.class, appServer);
        return appServer;
    }
}
