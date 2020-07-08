/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OccurrenceLogFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by bvn on 4/29/16.
 */
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_MUST_BE_UNIQUE + "}")
@ValidTraceFileName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALID_FILE_NAME + "}")
@ValidUrl(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALID_FILE_NAME + "}")
public abstract class EndPointConfigurationImpl implements EndPointConfiguration {
    private final Clock clock;
    private final TransactionService transactionService;
    private long id;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(blacklisted = {'<', '>'})
    private String name;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(blacklisted = {'<', '>'})
    private String url;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String webServiceName;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private LogLevel logLevel;
    private boolean tracing;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String traceFile;
    private boolean httpCompression;
    private boolean schemaValidation;
    private EndPointAuthentication authenticationMethod;
    private boolean active;

    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName; // for auditing

    @SuppressWarnings("unused")
    private long version;

    private final DataModel dataModel;
    private final WebServicesService webServicesService;

    private List<EndPointProperty> properties = new ArrayList<>();

    private static final String INBOUND_WEBSERVICE_DISCRIMINATOR = "0";

    private static final String OUTBOUND_WEBSERVICE_DISCRIMINATOR = "1";

    static final Map<String, Class<? extends EndPointConfiguration>> IMPLEMENTERS =
            ImmutableMap.of(
                    INBOUND_WEBSERVICE_DISCRIMINATOR, InboundEndPointConfigurationImpl.class,
                    OUTBOUND_WEBSERVICE_DISCRIMINATOR, OutboundEndPointConfigurationImpl.class);

    public EndPointConfigurationImpl(Clock clock, DataModel dataModel, TransactionService transactionService, WebServicesService webServicesService) {
        this.clock = clock;
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.webServicesService = webServicesService;
    }

    public void delete() {
        this.dataModel.remove(this);
    }

    enum Fields {
        ID("id"),
        NAME("name"),
        URL("url"),
        WEB_SERVICE_NAME("webServiceName"),
        LOG_LEVEL("logLevel"),
        TRACING("tracing"),
        TRACEFILE("traceFile"),
        HTTP_COMPRESSION("httpCompression"),
        SCHEMA_VALIDATION("schemaValidation"),
        AUTHENTICATED("authenticationMethod"),
        ACTIVE("active"),
        USERNAME("username"),
        PASSWD("password"),
        CLIENT_ID("clientId"),
        CLIENT_SECRET("clientSecret"),
        GROUP("group");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }

    }

    protected void initialize(String name, String webServiceName, String url) {
        this.setName(name);
        this.setWebServiceName(webServiceName);
        this.setUrl(url);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getWebServiceName() {
        return webServiceName;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public boolean isTracing() {
        return tracing;
    }

    @Override
    public String getTraceFile() {
        return traceFile;
    }

    @Override
    public boolean isHttpCompression() {
        return httpCompression;
    }

    @Override
    public boolean isSchemaValidation() {
        return schemaValidation;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setUrl(String url) {
        this.url = url != null ? url.trim() : null;
    }

    @Override
    public void setWebServiceName(String webServiceName) {
        this.webServiceName = webServiceName;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public EndPointAuthentication getAuthenticationMethod() {
        return authenticationMethod;
    }

    @Override
    public void setAuthenticationMethod(EndPointAuthentication authenticated) {
        this.authenticationMethod = authenticated;
    }

    @Override
    public void setTracing(boolean tracing) {
        this.tracing = tracing;
    }

    @Override
    public void setTraceFile(String traceFile) {
        this.traceFile = traceFile.startsWith(File.separator) ? traceFile.substring(1) : traceFile;
    }

    @Override
    public void setHttpCompression(boolean httpCompression) {
        this.httpCompression = httpCompression;
    }

    @Override
    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void save() {
        Save.action(getId()).save(dataModel, this);
    }

    @Override
    public void log(LogLevel logLevel, String message) {
        log(logLevel, message, null);
    }

    private void doLog(LogLevel logLevel, String message) {
        EndPointLogImpl log = dataModel.getInstance(EndPointLogImpl.class)
                .init(this, message, logLevel, clock.instant());
        log.save();
    }

    @Override
    public void log(LogLevel logLevel, String message, WebServiceCallOccurrence occurrence) {
        if (this.logLevel.compareTo(logLevel) > -1) {
            if (transactionService.isInTransaction()) {
                doLog(logLevel, message, occurrence);
            } else {
                try (TransactionContext context = transactionService.getContext()) {
                    doLog(logLevel, message, occurrence);
                    context.commit();
                }
            }
        }
    }

    private void doLog(LogLevel logLevel, String message, WebServiceCallOccurrence occurrence) {
        EndPointLogImpl log = dataModel.getInstance(EndPointLogImpl.class)
                .init(this, message, logLevel, clock.instant(), occurrence);
        log.save();
    }

    @Override
    public void log(String message, Exception exception) {
        log(message, exception, null);
    }

    private void doLog(String message, Exception exception) {
        EndPointLogImpl log = dataModel.getInstance(EndPointLogImpl.class)
                .init(this, message, stackTrace2String(exception), LogLevel.SEVERE, clock.instant());
        log.save();
    }

    @Override
    public void log(String message, Exception exception, WebServiceCallOccurrence occurrence) {
        if (transactionService.isInTransaction()) {
            doLog(message, exception, occurrence);
        } else {
            try (TransactionContext context = transactionService.getContext()) {
                doLog(message, exception, occurrence);
                context.commit();
            }
        }
    }

    private void doLog(String message, Exception exception, WebServiceCallOccurrence occurrence) {
        EndPointLogImpl log = dataModel.getInstance(EndPointLogImpl.class)
                .init(this, message, stackTrace2String(exception), LogLevel.SEVERE, clock.instant(), occurrence);
        log.save();
    }

    private String stackTrace2String(Exception e) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (PrintWriter printWriter = new PrintWriter(byteArrayOutputStream)) {
            e.printStackTrace(printWriter);
        }
        return byteArrayOutputStream.toString();
    }

    @Override
    public Finder<EndPointLog> getLogs() {
        OccurrenceLogFinderBuilder finderBuilder =  new OccurrenceLogFinderBuilderImpl(dataModel);
        finderBuilder.withEndPointConfiguration(this);
        finderBuilder.withNoOccurrence();
        return finderBuilder.build();
    }

    @Override
    public Finder<WebServiceCallOccurrence> getOccurrences(boolean ascending) {
        return DefaultFinder.of(WebServiceCallOccurrence.class,
                Where.where(WebServiceCallOccurrenceImpl.Fields.ENDPOINT_CONFIGURATION.fieldName())
                        .isEqualTo(this), dataModel).sorted(WebServiceCallOccurrenceImpl.Fields.START_TIME.fieldName(), ascending);

    }


    @Override
    public List<EndPointProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return webServicesService.getWebServicePropertySpecs(getWebServiceName());
    }

    @Override
    public Map<String, Object> getPropertiesWithValue() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (EndPointProperty property : getProperties()) {
            builder.put(property.getName(), property.getValue());
        }
        return builder.build();
    }

    @Override
    public void setProperties(Map<String, Object> propertyMap) {
        DiffList<EndPointProperty> entryDiff = ArrayDiffList.fromOriginal(getProperties());
        entryDiff.clear();
        List<EndPointProperty> newProperties = new ArrayList<>();
        for (Map.Entry<String, Object> property : propertyMap.entrySet()) {
            EndPointPropertyImpl newProperty = new EndPointPropertyImpl().init(this, property.getKey(), property.getValue());
            newProperties.add(newProperty);
        }
        entryDiff.addAll(newProperties);
        properties.removeAll(entryDiff.getRemovals());
        for (EndPointProperty property : entryDiff.getRemaining()) {
            property.setValue(propertyMap.get(property.getName()));
            Optional<EndPointProperty> any = properties.stream().filter(aProperty -> aProperty.getName().equals(property.getName())).findAny();
            any.ifPresent(properties::remove);
            properties.add(property);
        }
        properties.addAll(entryDiff.getAdditions());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndPointConfigurationImpl that = (EndPointConfigurationImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public WebServiceCallOccurrence createWebServiceCallOccurrence(Instant startTime,
                                                                   String requestName,
                                                                   String applicationName) {
        return createWebServiceCallOccurrence(startTime, requestName, applicationName, null);
    }

    @Override
    public WebServiceCallOccurrence createWebServiceCallOccurrence(Instant startTime,
                                                                   String requestName,
                                                                   String applicationName,
                                                                   String payload) {
        WebServiceCallOccurrence occurrence = dataModel.getInstance(WebServiceCallOccurrenceImpl.class).init(
                startTime,
                requestName,
                applicationName,
                this,
                payload);
        occurrence.save();
        return occurrence;
    }
}
