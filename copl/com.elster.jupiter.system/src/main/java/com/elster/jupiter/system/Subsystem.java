/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface Subsystem {

    String getId();

    String getName();

    String getVersion();

    List<Component> getComponents();

}
