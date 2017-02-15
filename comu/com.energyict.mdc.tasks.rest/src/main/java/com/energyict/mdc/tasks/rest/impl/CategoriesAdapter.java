/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.tasks.rest.Categories;

/**
 * Maps Categories to the corresponding REST message seed
 * Created by gde on 4/05/2015.
 */
public class CategoriesAdapter extends MapBasedXmlAdapter<Categories> {

    public CategoriesAdapter() {
        register(MessageSeeds.LOGBOOKS.getKey(), Categories.LOGBOOKS);
        register(MessageSeeds.REGISTERS.getKey(), Categories.REGISTERS);
        register(MessageSeeds.TOPOLOGY.getKey(), Categories.TOPOLOGY);
        register(MessageSeeds.LOADPROFILES.getKey(), Categories.LOADPROFILES);
        register(MessageSeeds.CLOCK.getKey(), Categories.CLOCK);
        register(MessageSeeds.STATUS_INFORMATION.getKey(), Categories.STATUSINFORMATION);
        register(MessageSeeds.BASIC_CHECK.getKey(), Categories.BASICCHECK);
    }
}
