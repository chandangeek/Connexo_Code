package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Created by bvn on 4/29/16.
 */
public abstract class EndPointConfigurationImpl implements EndPointConfiguration {
    private final Clock clock;

    private long id;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String url;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String webServiceName;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private LogLevel logLevel;
    private boolean tracing;
    private boolean httpCompression;
    private boolean schemaValidation;

    private boolean active;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @SuppressWarnings("unused")
    private long version;

    private final DataModel dataModel;
    private static final String INBOUND_WEBSERVICE_DISCRIMINATOR = "0";

    private static final String OUTBOUND_WEBSERVICE_DISCRIMINATOR = "1";
    static final Map<String, Class<? extends EndPointConfiguration>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends EndPointConfiguration>>of(
                    INBOUND_WEBSERVICE_DISCRIMINATOR, InboundEndPointConfigurationImpl.class,
                    OUTBOUND_WEBSERVICE_DISCRIMINATOR, OutboundEndPointConfigurationImpl.class);

    @Inject
    public EndPointConfigurationImpl(Clock clock, DataModel dataModel) {
        this.clock = clock;
        this.dataModel = dataModel;
    }

    enum Fields {
        ID("id"),
        NAME("name"),
        URL("url"),
        WEB_SERVICE_NAME("webServiceName"),
        LOG_LEVEL("logLevel"),
        TRACING("tracing"),
        HTTP_COMPRESSION("httpCompression"),
        SCHEMA_VALIDATION("schemaValidation"),
        AUTHENTICATED("authenticated"),
        ACTIVE("active");

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
        this.url = url;
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
    public void setTracing(boolean tracing) {
        this.tracing = tracing;
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
        EndPointLogImpl log = dataModel.getInstance(EndPointLogImpl.class)
                .init(this, message, logLevel, clock.instant());
        log.save();
    }

    @Override
    public List<EndPointLog> getLogs() {
        return dataModel.query(EndPointLog.class)
                .select(where(EndPointLogImpl.Fields.endPointConfiguration.fieldName()).isEqualTo(this), Order.descending(EndPointLogImpl.Fields.timestamp
                        .fieldName()));
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
}
