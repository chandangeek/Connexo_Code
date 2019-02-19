/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone;

import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ZoneType extends HasName, HasAuditInfo {

    long getId();

    String getApplication();

    String getName();

    void setName(String name);

    void setApplication(String name);

    void save();
}
