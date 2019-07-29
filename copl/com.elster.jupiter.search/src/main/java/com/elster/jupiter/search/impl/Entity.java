package com.elster.jupiter.search.impl;


import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface Entity {

    long getId();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();

    // Operational methods
    void update();

    void delete();
}
