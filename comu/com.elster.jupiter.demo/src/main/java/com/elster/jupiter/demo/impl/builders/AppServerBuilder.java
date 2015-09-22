package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.cron.CronExpressionParser;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.Optional;

public class AppServerBuilder extends NamedBuilder<AppServer, AppServerBuilder> {
    private final AppService appService;
    private final CronExpressionParser cronExpressionParser;
    private final MessageService messageService;
    private final DataExportService dataExportService;

    private int threadCount;
    private String exportPath;

    @Inject
    public AppServerBuilder(AppService appService, CronExpressionParser cronExpressionParser, MessageService messageService, DataExportService dataExportService) {
        super(AppServerBuilder.class);
        this.appService = appService;
        this.cronExpressionParser = cronExpressionParser;
        this.messageService = messageService;
        this.dataExportService = dataExportService;

        this.exportPath = "D:\\Data-Export";
        this.threadCount = 1;
    }

    public AppServerBuilder withThreadCount(int count){
        this.threadCount = count;
        return this;
    }

    public AppServerBuilder withExportPath(String exportPath){
        this.exportPath = exportPath;
        return this;
    }

    @Override
    public Optional<AppServer> find() {
        return appService.findAppServer(getName());
    }

    @Override
    public AppServer create() {
        Log.write(this);
        AppServer appServer = appService.createAppServer(getName(), cronExpressionParser.parse("0 0 * * * ? *").get());
        messageService.getSubscribers().stream().forEach(subscriber -> appServer.createSubscriberExecutionSpec(subscriber, getThreadCount(subscriber)));
        dataExportService.setExportDirectory(appServer, Paths.get(exportPath));
        appServer.activate();
        return appServer;
    }

    private int getThreadCount(SubscriberSpec subscriber){
        return this.threadCount;
    }
}
