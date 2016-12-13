package com.elster.jupiter.usagepoint.lifecycle.config;

public interface UsagePointStage {
    enum Key {
        PRE_OPERATIONAL,
        OPERATIONAL,
        POST_OPERATIONAL
    }

    Key getKey();

    String getDisplayName();
}