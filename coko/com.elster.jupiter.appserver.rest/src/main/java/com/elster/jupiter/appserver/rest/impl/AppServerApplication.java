package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.common.collect.ImmutableSet;

import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(
        name = "com.elster.jupiter.appserver.rest",
        service = {Application.class},
        immediate = true,
        property = {"alias=/apr", "app=SYS", "name=" + AppServerApplication.COMPONENT_NAME})
public class AppServerApplication extends Application {

    public static final String COMPONENT_NAME = "APR";
    private volatile RestQueryService restQueryService;
    private volatile AppService appService;
    private volatile MessageService messageService;
    private volatile TransactionService transactionService;
    private volatile FileImportService fileImportService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile FileSystem fileSystem;

    private NlsService nlsService;
    private volatile Thesaurus thesaurus;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                AppServerResource.class,
                ImportDirectoryResource.class);
    }
    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
    }
    @Reference
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(AppService.COMPONENT_NAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(appService).to(AppService.class);
                bind(messageService).to(MessageService.class);
                bind(transactionService).to(TransactionService.class);
                bind(cronExpressionParser).to(CronExpressionParser.class);
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(fileImportService).to(FileImportService.class);
                bind(fileSystem).to(FileSystem.class);
            }
        });
        return Collections.unmodifiableSet(hashSet);
    }
}
