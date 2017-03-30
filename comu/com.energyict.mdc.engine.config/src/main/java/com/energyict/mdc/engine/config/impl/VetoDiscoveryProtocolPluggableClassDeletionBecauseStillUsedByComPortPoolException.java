/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.pluggable.PluggableClass;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

public class VetoDiscoveryProtocolPluggableClassDeletionBecauseStillUsedByComPortPoolException extends LocalizedException {

    public VetoDiscoveryProtocolPluggableClassDeletionBecauseStillUsedByComPortPoolException(Thesaurus thesaurus, PluggableClass pluggableClass, List<InboundComPortPool> comPortPools) {
        super(thesaurus, MessageSeeds.VETO_DISCOVERYPROTOCOLPLUGGABLECLASS_DELETION, pluggableClass.getName(), getComPortPoolNames(comPortPools));
    }

    private static String getComPortPoolNames(List<InboundComPortPool> comPortPools) {
        List<String> names = new ArrayList<>();
        for (InboundComPortPool comPortPool : comPortPools) {
            names.add(comPortPool.getName());
        }
        return Joiner.on(",").join(names);
    }

}