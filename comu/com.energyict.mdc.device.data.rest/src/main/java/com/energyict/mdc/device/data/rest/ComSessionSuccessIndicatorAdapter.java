package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.history.ComSession;

/**
 * Maps TaskStatus to related REST message seed
 * Created by bvn on 7/30/14.
 */
public class ComSessionSuccessIndicatorAdapter extends MapBasedXmlAdapter<ComSession.SuccessIndicator> {

    public ComSessionSuccessIndicatorAdapter() {
        register(MessageSeeds.SUCCESS.getKey(), ComSession.SuccessIndicator.Success);
        register(MessageSeeds.BROKEN.getKey(), ComSession.SuccessIndicator.Broken);
        register(MessageSeeds.SETUP_ERROR.getKey(), ComSession.SuccessIndicator.SetupError);
    }
}
