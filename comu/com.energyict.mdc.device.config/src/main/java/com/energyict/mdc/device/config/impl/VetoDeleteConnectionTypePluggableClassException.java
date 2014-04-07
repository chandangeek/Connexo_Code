package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 11:19
 */
public class VetoDeleteConnectionTypePluggableClassException extends LocalizedException {

    public VetoDeleteConnectionTypePluggableClassException(Thesaurus thesaurus, ConnectionTypePluggableClass connectionTypePluggableClass, List<PartialConnectionTask> clients) {
        super(thesaurus, MessageSeeds.VETO_CONNECTIONTYPE_PLUGGABLECLASS_DELETION, connectionTypePluggableClass.getJavaClassName(), asString(clients));
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
