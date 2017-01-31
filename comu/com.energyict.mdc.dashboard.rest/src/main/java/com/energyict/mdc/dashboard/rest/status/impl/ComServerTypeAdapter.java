/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.engine.status.ComServerType;

/**
 * Created by bvn on 10/2/14.
 */
public class ComServerTypeAdapter extends MapBasedXmlAdapter<ComServerType> {

    public ComServerTypeAdapter() {
        register("Online", ComServerType.ONLINE);
        register("Remote", ComServerType.REMOTE);
        register("Mobile", ComServerType.MOBILE);
        register("NotApplicable", ComServerType.NOT_APPLICABLE);
    }
}
