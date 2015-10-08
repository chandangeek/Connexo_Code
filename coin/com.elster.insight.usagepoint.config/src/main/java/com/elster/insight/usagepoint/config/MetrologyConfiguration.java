package com.elster.insight.usagepoint.config;

public interface MetrologyConfiguration {
    long getId();
    String getName();
    void setName(String name);
    void save();
    void delete();
    long getVersion();
}
