/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

import org.json.JSONException;
import org.json.JSONWriter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCollectedDataProcessingEventImpl<T extends CollectedData> extends AbstractComServerEventImpl implements CollectedDataProcessingEvent {

    private T payload;
    private List<Issue> issues = new ArrayList<>();

    protected AbstractCollectedDataProcessingEventImpl(ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    protected AbstractCollectedDataProcessingEventImpl(ServiceProvider serviceProvider, T payload) {
        super(serviceProvider);
        if (payload == null){
            throw new IllegalArgumentException("Payload cannot be null");
        }
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }

    @Override
    public List<Issue> getIssues() {
        return issues;
    }

    public void addIssue(Issue issue) {
        this.issues.add(issue);
    }

    public void addIssues(List<Issue> issues) {
        this.issues.addAll(issues);
    }

    public boolean hasIssues() {
        return (issues != null && !issues.isEmpty());
    }

    public Category getCategory (){
        return Category.COLLECTED_DATA_PROCESSING;
    }

    protected void toString(JSONWriter writer) throws JSONException {
        super.toString(writer);
        addEventInfo(writer);
    }

    protected void addEventInfo(JSONWriter writer) throws JSONException {
        writer.key("log-level").value(String.valueOf(this.getLogLevel()));
        writer.key("message").value(getDescription());
        writer.key("details");
        writer.object();
        addPayload(writer);
        writer.endObject();
        addIssues(writer);
    }

    /**
     * To be overriden by classes for which the source needs to be marschalled and written by the given writer
     * @param writer used to marschal this event
     * @throws JSONException when marschalling fails
     */
    protected void addPayload(JSONWriter writer) throws JSONException{
        // By default nothing is written on the writer
    }

    private void addIssues(JSONWriter writer) throws JSONException {
        if (hasIssues()) {
            writer.key("issues");
            writer.array();

            for (Issue issue : getIssues()) {
                writer.object();
                writer.
                        key(issue.isWarning() ? "warning" : "problem").
                        value(issue.getDescription());
                writer.endObject();
            }
            writer.endArray();
        }
    }

    @Override
    public LogLevel getLogLevel (){
        if (!this.hasIssues()) {
            return LogLevel.INFO;
        } else if (getIssues().stream().allMatch(Issue::isWarning)) {
            return LogLevel.WARN;
        }
        return LogLevel.ERROR;
    }

    public String getLogMessage (){
        return this.toString();
    }
}
