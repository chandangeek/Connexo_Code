/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.users.Group;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

/**
 * Created by bvn on 5/4/16.
 */
public class EndPointConfigurationServiceImpl implements EndPointConfigurationService {
    private volatile DataModel dataModel;
    private volatile EventService eventService;

    @Inject
    EndPointConfigurationServiceImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
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
    public QueryStream<EndPointConfiguration> streamEndPointConfigurations() {
        return dataModel.stream(EndPointConfiguration.class);
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
        eventService.postEvent(EventType.ENDPOINT_CONFIGURATION_VALIDATE_DELETE.topic(), endPointConfiguration);
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
        public InboundEndPointConfigBuilder withProperties(Map<String, Object> properties) {
            instance.setProperties(properties);
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

        @Override
        public OutboundEndPointConfigBuilder withProperties(Map<String, Object> properties) {
            instance.setProperties(properties);
            return this;
        }
    }
}
