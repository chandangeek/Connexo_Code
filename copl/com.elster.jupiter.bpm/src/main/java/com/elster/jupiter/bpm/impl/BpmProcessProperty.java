/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by dragos on 2/19/2016.
 */

@ProviderType
public interface BpmProcessProperty {

    String getName();

    Object getValue();

    void setValue(Object value);

    BpmProcessDefinition getProcessDefinition();
}
