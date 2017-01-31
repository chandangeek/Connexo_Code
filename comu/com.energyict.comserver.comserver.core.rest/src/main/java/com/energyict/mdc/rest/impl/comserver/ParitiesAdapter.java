/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.io.Parities;

public class ParitiesAdapter extends MapBasedXmlAdapter<Parities> {

    public ParitiesAdapter() {
        register("", null);
        register(ComServerFieldTranslationKeys.PARITY_NONE.getKey(), Parities.NONE);
        register(ComServerFieldTranslationKeys.PARITY_EVEN.getKey(), Parities.EVEN);
        register(ComServerFieldTranslationKeys.PARITY_ODD.getKey(), Parities.ODD);
        register(ComServerFieldTranslationKeys.PARITY_MARK.getKey(), Parities.MARK);
        register(ComServerFieldTranslationKeys.PARITY_SPACE.getKey(), Parities.SPACE);
    }
}
