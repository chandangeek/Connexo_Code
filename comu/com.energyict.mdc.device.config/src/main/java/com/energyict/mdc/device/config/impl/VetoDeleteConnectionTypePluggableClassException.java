/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.List;
import java.util.stream.Collectors;

public class VetoDeleteConnectionTypePluggableClassException extends LocalizedException {

    public VetoDeleteConnectionTypePluggableClassException(Thesaurus thesaurus, ConnectionTypePluggableClass connectionTypePluggableClass, List<PartialConnectionTask> clients) {
        super(thesaurus, MessageSeeds.VETO_CONNECTIONTYPE_PLUGGABLECLASS_DELETION, connectionTypePluggableClass.getJavaClassName(), asString(clients));
    }

    private static String asString(List<PartialConnectionTask> clients) {
        return clients.stream().map(PartialConnectionTask::getName).collect(Collectors.joining(", "));
    }

}