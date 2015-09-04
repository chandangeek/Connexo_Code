package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 11:19
 */
public class VetoDeleteComTaskException extends LocalizedException {

    public VetoDeleteComTaskException(Thesaurus thesaurus, ComTask comTask) {
        super(thesaurus, MessageSeeds.VETO_COMTASK_DELETION, comTask.getName());
    }

}