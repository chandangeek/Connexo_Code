/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.export.rest",
        service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/export", "app=SYS", "name=" + DataExportApplication.COMPONENT_NAME})
public class DataExportApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {
    public static final String COMPONENT_NAME = "DER";

    private volatile DataExportService dataExportService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile MeteringService meteringService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile AppService appService;
    private volatile Clock clock;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile TimeService timeService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile EndPointConfigurationService endPointConfigurationService;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                RestValidationExceptionMapper.class,
                DataExportTaskResource.class,
                ExportDirectoryResource.class,
                FieldResource.class,
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
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(DataExportService.COMPONENTNAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus).join(nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN));
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

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
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
                bind(metrologyConfigurationService).to(MetrologyConfigurationService.class);
                bind(transactionService).to(TransactionService.class);
                bind(appService).to(AppService.class);
                bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
                bind(DataSourceInfoFactory.class).to(DataSourceInfoFactory.class);
                bind(DataExportTaskInfoFactory.class).to(DataExportTaskInfoFactory.class);
                bind(DataExportTaskHistoryInfoFactory.class).to(DataExportTaskHistoryInfoFactory.class);
                bind(StandardDataSelectorInfoFactory.class).to(StandardDataSelectorInfoFactory.class);
                bind(clock).to(Clock.class);
                bind(endPointConfigurationService).to(EndPointConfigurationService.class);
                Arrays.stream(DestinationType.values())
                        .map(DestinationType::getFactoryClass)
                        .forEach(factoryClass -> bind(factoryClass).to(factoryClass));
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

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

}
