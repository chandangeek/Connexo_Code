package com.elster.jupiter.time;

import java.time.Instant;

public interface Entity {
    long getId();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();

    void delete();
}
