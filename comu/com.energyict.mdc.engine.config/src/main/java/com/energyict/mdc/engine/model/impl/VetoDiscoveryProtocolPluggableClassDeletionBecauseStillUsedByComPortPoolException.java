package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;

public class VetoDiscoveryProtocolPluggableClassDeletionBecauseStillUsedByComPortPoolException extends LocalizedException {

    public VetoDiscoveryProtocolPluggableClassDeletionBecauseStillUsedByComPortPoolException(Thesaurus thesaurus, InboundDeviceProtocolPluggableClass deviceProtocolPluggableClass, List<InboundComPortPool> comPortPools) {
        super(thesaurus, MessageSeeds.VETO_DISCOVERYPROTOCOLPLUGGABLECLASS_DELETION, deviceProtocolPluggableClass.getName(), getComPortPoolNames(comPortPools));
    }

    private static String getComPortPoolNames(List<InboundComPortPool> comPortPools) {
        List<String> names = new ArrayList<>();
        for (InboundComPortPool comPortPool : comPortPools) {
            names.add(comPortPool.getName());
        }
        return Joiner.on(",").join(names);
    }

}