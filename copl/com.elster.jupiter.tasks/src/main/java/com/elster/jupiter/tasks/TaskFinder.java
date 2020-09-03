/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Created by igh on 5/11/2015.
 */
@ProviderType
public interface TaskFinder {

    public List<? extends RecurrentTask> find();

    long count();

}
