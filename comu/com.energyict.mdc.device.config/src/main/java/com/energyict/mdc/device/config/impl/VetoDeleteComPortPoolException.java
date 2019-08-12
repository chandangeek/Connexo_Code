/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

import java.util.List;
import java.util.stream.Collectors;

public class VetoDeleteComPortPoolException extends LocalizedException {

    public VetoDeleteComPortPoolException(Thesaurus thesaurus, ComPortPool comPortPool, List<PartialConnectionTask> clients) {
        super(thesaurus, MessageSeeds.VETO_COMPORTPOOL_DELETION, comPortPool.getName(), asString(clients));
    }

    private static String asString(List<PartialConnectionTask> clients) {
        return clients.stream().map(VetoDeleteComPortPoolException::asString).collect(Collectors.joining(", "));
    }

    private static String asString(PartialConnectionTask partialConnectionTask) {
        return partialConnectionTask.getConfiguration().getDeviceType().getName() + "::" + partialConnectionTask.getConfiguration().getName() + "::" + partialConnectionTask.getName();
    }

}