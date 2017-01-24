package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

/**
 * Maps an action String to the corresponding REST message seed
 * Created by gde on 5/05/2015.
 */
public class ActionAdapter extends MapBasedXmlAdapter<String> {

    public ActionAdapter() {
        register(MessageSeeds.READ.getKey(), "read");
        register(MessageSeeds.UPDATE.getKey(), "update");
        register(MessageSeeds.VERIFY.getKey(), "verify");
        register(MessageSeeds.SET.getKey(), "set");
        register(MessageSeeds.FORCE.getKey(), "force");
        register(MessageSeeds.SYNCHRONIZE.getKey(), "synchronize");
        register(MessageSeeds.CHECK.getKey(), "check");
    }

}
