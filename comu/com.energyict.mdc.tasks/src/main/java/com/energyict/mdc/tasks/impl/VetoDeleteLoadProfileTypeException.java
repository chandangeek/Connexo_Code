/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.util.List;
import java.util.stream.Collectors;

public class VetoDeleteLoadProfileTypeException extends LocalizedException {

    public VetoDeleteLoadProfileTypeException(Thesaurus thesaurus, LoadProfileType loadProfileType, List<LoadProfilesTask> clients) {
        super(thesaurus, MessageSeeds.VETO_LOAD_PROFILE_TYPE_DELETION, loadProfileType.getName(), asString(clients));
    }

    private static String asString(List<LoadProfilesTask> usages) {
        return usages.stream().map(LoadProfilesTask::getComTask).map(ComTask::getName).collect(Collectors.joining(", "));
    }

}