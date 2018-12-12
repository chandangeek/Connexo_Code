package com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;

public enum MasterDataLinkageAction {
    CREATE {
        @Override
        MessageSeeds getBasicSeed() {
            return MessageSeeds.UNABLE_TO_LINK_METER;
        }
    },
    CLOSE {
        @Override
        MessageSeeds getBasicSeed() {
            return MessageSeeds.UNABLE_TO_UNLINK_METER;
        }
    };

    abstract MessageSeeds getBasicSeed();
}
