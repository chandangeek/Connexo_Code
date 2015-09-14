package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;
import java.util.stream.Collectors;

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
        return clients.stream().map(VetoDeleteComPortPoolException::asString).collect(Collectors.joining(", "));
    }

    private static String asString(PartialConnectionTask partialConnectionTask) {
        return partialConnectionTask.getConfiguration().getDeviceType().getName() + "::" + partialConnectionTask.getConfiguration().getName() + "::" + partialConnectionTask.getName();
    }

}