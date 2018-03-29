/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class CustomTaskBuilderImpl implements CustomTaskBuilder {

    private final DataModel dataModel;

    private List<PropertyBuilderImpl> properties = new ArrayList<>();
    private ScheduleExpression scheduleExpression;
    private Instant nextExecution;
    private boolean scheduleImmediately;
    private String name;
    private String taskType;
    private String application;
    private int logLevel;
    private List<RecurrentTask> nextRecurrentTasks = new ArrayList<>();

    CustomTaskBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public CustomTaskBuilderImpl setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public CustomTaskBuilderImpl setNextExecution(Instant nextExecution) {
        this.nextExecution = nextExecution;
        return this;
    }

    @Override
    public CustomTaskBuilderImpl setApplication(String application) {
        this.application = application;
        return this;
    }

    @Override
    public CustomTaskBuilderImpl setLogLevel(int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    @Override
    public CustomTask create() {
        CustomTaskImpl customTask = CustomTaskImpl.from(dataModel, name, taskType, scheduleExpression, nextExecution, application, logLevel);
        customTask.setScheduleImmediately(scheduleImmediately);
        customTask.setNextRecurrentTasks(nextRecurrentTasks);
        properties.forEach(p -> customTask.setProperty(p.name, p.value));
        customTask.doSave();
        return customTask;
    }

    @Override
    public CustomTaskBuilderImpl setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public CustomTaskBuilderImpl setTaskType(String taskType) {
        this.taskType = taskType;
        return this;
    }

    @Override
    public CustomTaskBuilderImpl setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks) {
        this.nextRecurrentTasks = nextRecurrentTasks;
        return this;
    }

    @Override
    public PropertyBuilder<CustomTaskBuilder> addProperty(String name) {
        return new PropertyBuilderImpl<>(this, name);
    }

    private class PropertyBuilderImpl<T> implements PropertyBuilder<T> {
        private final String name;
        private final T source;
        private Object value;

        private PropertyBuilderImpl(T source, String name) {
            this.name = name;
            this.source = source;
        }

        @Override
        public T withValue(Object value) {
            this.value = value;
            properties.add(this);
            return source;
        }
    }
}
