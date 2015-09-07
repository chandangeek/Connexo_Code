package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.ws.rs.core.Application;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.fileimport.rest",
        service = {Application.class, MessageSeedProvider.class},
        immediate = true,
        property = {"alias=/fir", "app=SYS", "name=" + FileImportApplication.COMPONENT_NAME})
public class FileImportApplication extends Application implements MessageSeedProvider, AppNamesProvider {
    public static final String COMPONENT_NAME = "FIR";

    private volatile FileImportService fileImportService;
    private volatile AppService appService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile FileSystem fileSystem;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private List<App> apps = new CopyOnWriteArrayList<>();

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                FileImporterResource.class,
                FileImportScheduleResource.class);
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(FileImportService.COMPONENT_NAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addApplication(App app) {
        apps.add(app);
    }

    public void removeApplication(App app) {
        apps.remove(app);
    }

    @Override
    public String findAppNameByKey(String appKey) {
        return apps.stream().filter(app -> app.getKey().equals(appKey)).map(App::getName).findFirst().orElse(appKey);
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
                bind(PropertyUtils.class).to(PropertyUtils.class);
                bind(nlsService).to(NlsService.class);
                bind(fileImportService).to(FileImportService.class);
                bind(cronExpressionParser).to(CronExpressionParser.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(transactionService).to(TransactionService.class);
                bind(fileSystem).to(FileSystem.class);
                bind(appService).to(AppService.class);
                bind(FileImportApplication.this).to(AppNamesProvider.class);
                bind(FileImportScheduleInfoFactory.class).to(FileImportScheduleInfoFactory.class);
                bind(FileImporterInfoFactory.class).to(FileImporterInfoFactory.class);
            }
        });
        return Collections.unmodifiableSet(hashSet);
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}
