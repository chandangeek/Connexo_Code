package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.nio.file.FileSystem;
import java.util.*;

@Component(name = "com.elster.jupiter.fileimport.rest", service = {Application.class, InstallService.class}, immediate = true, property = {"alias=/fir", "app=SYS", "name=" + FileImportApplication.COMPONENT_NAME})
public class FileImportApplication extends Application implements InstallService {
    public static final String COMPONENT_NAME = "FIR";
    private volatile FileImportService fileImportService;
    private volatile AppService appService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile FileSystem fileSystem;

    private NlsService nlsService;
    private volatile Thesaurus thesaurus;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                FileImportersResource.class,
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


    @Activate
    public void activate() {

    }

    @Deactivate
    public void deactivate() {

    }

    @Override
    public final void install() {
        new TranslationInstaller(thesaurus).createTranslations();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "NLS", "FIM");
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
            }
        });
        return Collections.unmodifiableSet(hashSet);
    }

}
