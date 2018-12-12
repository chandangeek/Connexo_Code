/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Created by bvn on 3/1/16.
 */
public class EndPointLogImpl implements EndPointLog, HasId {

    enum Fields {
        timestamp("timestamp"),
        logLevel("logLevel"),
        endPointConfiguration("endPointConfiguration"),
        message("message"),
        stacetrace("stackTrace");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;

    @Inject
    public EndPointLogImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private long id;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Instant timestamp;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private LogLevel logLevel;
    private Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private String message;
    private String stackTrace;

    EndPointLogImpl init(EndPointConfiguration endPointConfiguration, String message, LogLevel logLevel, Instant timestamp) {
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.endPointConfiguration.set(endPointConfiguration);
        this.message = message;
        return this;
    }

    EndPointLogImpl init(EndPointConfiguration endPointConfiguration, String message, String stacetrace, LogLevel logLevel, Instant timestamp) {
        init(endPointConfiguration, message, logLevel, timestamp);
        this.stackTrace = stacetrace;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    public EndPointConfiguration getEndPointConfiguration() {
        return endPointConfiguration.get();
    }

    @Override
    public Instant getTime() {
        return timestamp;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getStackTrace() {
        return stackTrace;
    }

    public void save() {
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }

    @Override
    public void delete() {
        this.dataModel.remove(this);
    }
}
