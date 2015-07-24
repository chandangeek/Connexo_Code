package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.RegistersTask;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;
import java.util.stream.Collectors;

public class VetoDeleteRegisterGroupException extends LocalizedException {

    public VetoDeleteRegisterGroupException(Thesaurus thesaurus, RegisterGroup registerGroup, List<RegistersTask> clients) {
        super(thesaurus, MessageSeeds.VETO_LOAD_PROFILE_TYPE_DELETION, registerGroup.getName(), asString(clients));
    }

    private static String asString(List<RegistersTask> usages) {
        return usages.stream().map(RegistersTask::getComTask).map(ComTask::getName).collect(Collectors.joining(", "));
    }

}