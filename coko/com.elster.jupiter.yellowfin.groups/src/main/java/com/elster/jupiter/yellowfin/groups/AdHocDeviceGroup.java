/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.groups;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by Lucian on 1/8/2015.
 */
@ProviderType
public interface AdHocDeviceGroup {
    long getId();
    String getName();

}
