package com.elster.insight.usagepoint.config;

import java.time.Instant;

public interface MetrologyConfiguration {
    long getId();
    String getName();
    void setName(String name);
    void save();
    void delete();
    long getVersion();
    Instant getCreateTime();
    Instant getModTime();
    String getUserName();
}
