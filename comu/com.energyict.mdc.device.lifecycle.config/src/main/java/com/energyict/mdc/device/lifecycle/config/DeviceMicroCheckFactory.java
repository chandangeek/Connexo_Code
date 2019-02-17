/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.orm.DataModel;

import java.util.Optional;
import java.util.Set;

@ConsumerType
public interface DeviceMicroCheckFactory {

    Optional<MicroCheckNew> from(String microCheckKey);

    Set<MicroCheckNew> getAllChecks();

    void setDataModel(DataModel dataModel);
}