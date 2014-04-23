package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.util.time.UtcInstant;

public interface Entity {

    long getId();

    long getVersion();

    UtcInstant getCreateTime();

    UtcInstant getModTime();

    String getUserName();

    // Operational methods
    void save();

    void update();

    void delete();
}
