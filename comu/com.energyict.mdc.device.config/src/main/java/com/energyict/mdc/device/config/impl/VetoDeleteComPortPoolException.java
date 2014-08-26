package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.engine.model.ComPortPool;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 11:19
 */
public class VetoDeleteComPortPoolException extends LocalizedException {

    public VetoDeleteComPortPoolException(Thesaurus thesaurus, ComPortPool comPortPool, List<PartialConnectionTask> clients) {
        super(thesaurus, MessageSeeds.VETO_COMPORTPOOL_DELETION, comPortPool.getName(), asString(clients));
    }

    private static String asString(List<PartialConnectionTask> clients) {
        StringBuilder builder = new StringBuilder();
        Holder<String> separator = HolderBuilder.first("").andThen(", ");
        for (PartialConnectionTask task : clients) {
            builder.append(separator.get()).append(task.getName());
        }
        return builder.toString();
    }

}