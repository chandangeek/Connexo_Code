/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap;


import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

public enum MasterDataLinkageAction {
    CREATE {
        @Override
        public MessageSeeds getBasicSeed() {
            return MessageSeeds.UNABLE_TO_LINK_METER;
        }
    },
    CLOSE {
        @Override
        public MessageSeeds getBasicSeed() {
            return MessageSeeds.UNABLE_TO_UNLINK_METER;
        }
    };

    public abstract MessageSeeds getBasicSeed();
}
