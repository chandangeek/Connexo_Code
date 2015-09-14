package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.engine.config.ComPortPool;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 11:19
 */
public class VetoDeleteComPortPoolException extends LocalizedException {

    public VetoDeleteComPortPoolException(Thesaurus thesaurus, ComPortPool comPortPool) {
        super(thesaurus, MessageSeeds.VETO_COMPORTPOOL_DELETION, comPortPool.getName());
    }

}