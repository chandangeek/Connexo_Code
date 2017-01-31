/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 5/4/16.
 */
@Component(name = "com.elster.jupiter.soap.webservices.installer", service = {EndPointConfigurationService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + WebServicesService.COMPONENT_NAME,
        immediate = true)
public class EndPointConfigurationServiceImpl implements EndPointConfigurationService, MessageSeedProvider, TranslationKeyProvider {
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile HttpService httpService;

    // OSGi
    public EndPointConfigurationServiceImpl() {
    }

    @Inject // Test purposes only
    public EndPointConfigurationServiceImpl(EventService eventService, NlsService nlsService, OrmService ormService,
                                            UserService userService, TransactionService transactionService,
                                            HttpService httpService) {
        setEventService(eventService);
        setNlsService(nlsService);
        setOrmService(ormService);
        setUserService(userService);
        setTransactionService(transactionService);
        setHttpService(httpService);
        activate();
    }

    @Override
    public String getComponentName() {
        return WebServicesService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Stream.of(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(WebServicesService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(WebServicesService.COMPONENT_NAME, "Web services");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(EventService.class).toInstance(eventService);
                bind(EndPointConfigurationService.class).toInstance(EndPointConfigurationServiceImpl.this);
                bind(UserService.class).toInstance(userService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(HttpService.class).toInstance(httpService);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public InboundEndPointConfigBuilder newInboundEndPointConfiguration(String name, String webServiceName, String url) {
        InboundEndPointConfigurationImpl instance = dataModel.getInstance(InboundEndPointConfigurationImpl.class);
        instance.initialize(name, webServiceName, url);
        return new InboundEndPointConfigurationBuilderImpl(instance);
    }

    @Override
    public OutboundEndPointConfigBuilder newOutboundEndPointConfiguration(String name, String webServiceName, String url) {
        OutboundEndPointConfigurationImpl instance = dataModel.getInstance(OutboundEndPointConfigurationImpl.class);
        instance.initialize(name, webServiceName, url);
        return new OutboundEndPointConfigurationBuilderImpl(instance);
    }

    @Override
    public Optional<EndPointConfiguration> getEndPointConfiguration(String name) {
        return dataModel.mapper(EndPointConfiguration.class)
                .getUnique(EndPointConfigurationImpl.Fields.NAME.fieldName(), name);
    }

    @Override
    public Optional<EndPointConfiguration> getEndPointConfiguration(long id) {
        return dataModel.mapper(EndPointConfiguration.class)
                .getUnique(EndPointConfigurationImpl.Fields.ID.fieldName(), id);
    }

    @Override
    public Finder<EndPointConfiguration> findEndPointConfigurations() {
        return DefaultFinder.of(EndPointConfiguration.class, this.dataModel)
                .defaultSortColumn(EndPointConfigurationImpl.Fields.NAME.fieldName());
    }

    @Override
    @TransactionRequired
    public void activate(EndPointConfiguration endPointConfiguration) {
        ((EndPointConfigurationImpl) endPointConfiguration).setActive(true);
        endPointConfiguration.save();
        endPointConfiguration.log(LogLevel.INFO, "Endpoint was activated");
        eventService.postEvent(EventType.ENDPOINT_CONFIGURATION_CHANGED.topic(), endPointConfiguration);
    }

    @Override
    @TransactionRequired
    public void deactivate(EndPointConfiguration endPointConfiguration) {
        ((EndPointConfigurationImpl) endPointConfiguration).setActive(false);
        endPointConfiguration.save();
        endPointConfiguration.log(LogLevel.INFO, "Endpoint was de-activated");
        eventService.postEvent(EventType.ENDPOINT_CONFIGURATION_CHANGED.topic(), endPointConfiguration);
    }

    @Override
    public void delete(EndPointConfiguration endPointConfiguration) {
        ((EndPointConfigurationImpl) endPointConfiguration).delete();
        eventService.postEvent(EventType.ENDPOINT_CONFIGURATION_CHANGED.topic(), endPointConfiguration);
    }

    @Override
    public Optional<EndPointConfiguration> findAndLockEndPointConfigurationByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(EndPointConfiguration.class).lockObjectIfVersion(version, id);
    }

    class InboundEndPointConfigurationBuilderImpl implements InboundEndPointConfigBuilder {

        private final InboundEndPointConfigurationImpl instance;

        InboundEndPointConfigurationBuilderImpl(InboundEndPointConfigurationImpl instance) {
            this.instance = instance;
        }

        @Override
        public InboundEndPointConfigBuilder tracing() {
            instance.setTracing(true);
            return this;
        }

        @Override
        public InboundEndPointConfigBuilder traceFile(String traceFile) {
            instance.setTraceFile(traceFile);
            return this;
        }

        @Override
        public InboundEndPointConfigBuilder httpCompression() {
            instance.setHttpCompression(true);
            return this;
        }

        @Override
        public InboundEndPointConfigBuilder setAuthenticationMethod(EndPointAuthentication endPointAuthentication) {
            instance.setAuthenticationMethod(endPointAuthentication);
            return this;
        }

        @Override
        public InboundEndPointConfigBuilder schemaValidation() {
            instance.setSchemaValidation(true);
            return this;
        }

        @Override
        public InboundEndPointConfigBuilder logLevel(LogLevel logLevel) {
            instance.setLogLevel(logLevel);
            return this;
        }

        @Override
        public InboundEndPointConfigBuilder group(Group group) {
            instance.setGroup(group);
            return this;
        }

        @Override
        public EndPointConfiguration create() {
            instance.save();
            return instance;
        }

    }

    class OutboundEndPointConfigurationBuilderImpl implements OutboundEndPointConfigBuilder {

        private final OutboundEndPointConfigurationImpl instance;

        OutboundEndPointConfigurationBuilderImpl(OutboundEndPointConfigurationImpl instance) {
            this.instance = instance;
        }

        @Override
        public OutboundEndPointConfigBuilder tracing() {
            instance.setTracing(true);
            return this;
        }

        @Override
        public OutboundEndPointConfigBuilder traceFile(String traceFile) {
            instance.setTraceFile(traceFile);
            return this;
        }

        @Override
        public OutboundEndPointConfigBuilder httpCompression() {
            instance.setHttpCompression(true);
            return this;
        }

        @Override
        public OutboundEndPointConfigBuilder username(String username) {
            instance.setUsername(username);
            return this;
        }

        @Override
        public OutboundEndPointConfigBuilder password(String password) {
            instance.setPassword(password);
            return this;
        }

        @Override
        public OutboundEndPointConfigBuilder schemaValidation() {
            instance.setSchemaValidation(true);
            return this;
        }

        @Override
        public OutboundEndPointConfigBuilder logLevel(LogLevel logLevel) {
            instance.setLogLevel(logLevel);
            return this;
        }

        @Override
        public EndPointConfiguration create() {
            instance.save();
            return instance;
        }

        @Override
        public OutboundEndPointConfigBuilder setAuthenticationMethod(EndPointAuthentication id) {
            instance.setAuthenticationMethod(id);
            return this;
        }
    }
}
