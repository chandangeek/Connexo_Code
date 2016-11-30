package com.elster.jupiter.usagepoint.lifecycle.config;

public interface UsagePointStage {
    enum Stage {
        PRE_OPERATIONAL,
        OPERATIONAL,
        POST_OPERATIONAL
    }

    Stage getKey();

    String getDisplayName();
}