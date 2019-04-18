/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;

public enum Action {
    CREATE {
        @Override
        public MessageSeeds getBasicSeed() {
            return MessageSeeds.UNABLE_TO_CREATE_USAGE_POINT;
        }
    },
    UPDATE {
        @Override
        public MessageSeeds getBasicSeed() {
            return MessageSeeds.UNABLE_TO_UPDATE_USAGE_POINT;
        }
    };

    public abstract MessageSeeds getBasicSeed();
}
