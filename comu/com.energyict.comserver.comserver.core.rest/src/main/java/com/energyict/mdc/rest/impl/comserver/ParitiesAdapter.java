package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

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
