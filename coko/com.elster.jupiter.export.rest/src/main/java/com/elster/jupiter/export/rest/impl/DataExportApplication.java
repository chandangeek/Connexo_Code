package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.export.rest",
        service = {Application.class, MessageSeedProvider.class},
        immediate = true,
        property = {"alias=/export", "app=SYS", "name=" + DataExportApplication.COMPONENT_NAME})
public class DataExportApplication extends Application implements MessageSeedProvider {
    public static final String COMPONENT_NAME = "DER";

    private volatile DataExportService dataExportService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile AppService appService;

    private NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile TimeService timeService;
    private volatile PropertyValueInfoService propertyValueInfoService;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                DataExportTaskResource.class,
                ExportDirectoryResource.class,
                MeterGroupsResource.class,
                FormattersResource.class,
                SelectorsResource.class);
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(DataExportService.COMPONENTNAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus).join(nlsService.getThesaurus("MTR", Layer.DOMAIN));
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
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
                bind(propertyValueInfoService).to(PropertyValueInfoService.class);
                bind(nlsService).to(NlsService.class);
                bind(dataExportService).to(DataExportService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(meteringGroupsService).to(MeteringGroupsService.class);
                bind(timeService).to(TimeService.class);
                bind(meteringService).to(MeteringService.class);
                bind(transactionService).to(TransactionService.class);
                bind(appService).to(AppService.class);
                bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
                bind(DataSourceInfoFactory.class).to(DataSourceInfoFactory.class);
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
