package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;

public class AppServiceModule extends AbstractModule {
    @Override
    protected void configure() {

        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(TransactionService.class);
        requireBinding(MessageService.class);
        requireBinding(CronExpressionParser.class);
        requireBinding(JsonService.class);
        requireBinding(FileImportService.class);
        requireBinding(TaskService.class);
        requireBinding(UserService.class);
        requireBinding(BundleContext.class);

        bind(AppServiceImpl.class).in(Scopes.SINGLETON);
        bind(AppService.class).to(AppServiceImpl.class);
        bind(IAppService.class).to(AppServiceImpl.class);
        bind(MessageHandlerLauncherService.class).in(Scopes.SINGLETON);

    }
}