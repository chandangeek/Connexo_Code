/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

public enum ConnectionState implements TranslationKey {
    /**
     * @deprecated This value was originally introduced to identify the state of a usage point before physical installation.
     * But it should not be used anymore because {@link UsagePointStage.Key#PRE_OPERATIONAL} stage that has been introduced
     * by {@link UsagePointLifeCycle} models the same situation but in terms of lifecycle
     */
    @Deprecated
    UNDER_CONSTRUCTION("underConstruction", "Under construction"),
    CONNECTED("connected", "Connected"),
    PHYSICALLY_DISCONNECTED("physicallyDisconnected", "Physically disconnected"),
    LOGICALLY_DISCONNECTED("logicallyDisconnected", "Logically disconnected"),
    /**
     * @deprecated This value was originally introduced to identify the state of a usage point after demolish.
     * But it should not be used anymore because {@link UsagePointStage.Key#POST_OPERATIONAL} stage that has been introduced
     * by {@link UsagePointLifeCycle} models the same situation but in terms of lifecycle
     */
    @Deprecated
    DEMOLISHED("demolished", "Demolished");

    private String id;
    private String name;

    ConnectionState(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getKey() {
        return "connection.state." + this.id;
    }

    @Override
    public String getDefaultFormat() {
        return this.name;
    }
}
