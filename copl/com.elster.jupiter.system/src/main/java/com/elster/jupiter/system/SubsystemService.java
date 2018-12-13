/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface SubsystemService {

    String COMPONENTNAME = "SBS";

    void registerSubsystem(Subsystem subsystem);

    void unregisterSubsystem(Subsystem subsystem);

    List<Subsystem> getSubsystems();

    List<RuntimeComponent> getRuntimeComponents();

}
