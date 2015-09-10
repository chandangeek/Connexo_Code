package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;
import java.util.stream.Collectors;

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
        return clients.stream().map(PartialConnectionTask::getName).collect(Collectors.joining(", "));
    }

}