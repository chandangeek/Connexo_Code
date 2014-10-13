package com.elster.jupiter.issue.share.entity;

import java.time.Instant;

public interface Entity {

    long getId();
    long getVersion();
    Instant getCreateTime();
    Instant getModTime();
    String getUserName();

    // Operational methods
    void save();
    void delete();
}
