/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.engine.config.ComPortPool;

public class VetoDeleteComPortPoolException extends LocalizedException {

    public VetoDeleteComPortPoolException(Thesaurus thesaurus, ComPortPool comPortPool) {
        super(thesaurus, MessageSeeds.VETO_COMPORTPOOL_DELETION, comPortPool.getName());
    }

}