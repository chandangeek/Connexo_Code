/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;


import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ProcessAssociationProvider extends HasName, HasDynamicProperties {

    String getType();

    String getAppKey();
}
