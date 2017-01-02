package com.energyict.protocolimplv2.elster.ctr.EK155;

import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;

/**
 * @author sva
 * @since 6/06/13 - 15:09
 */
public class EK155Properties extends MTU155Properties {

    public EK155Properties(TypedProperties typedProperties) {
        super(typedProperties);
    }

    public boolean isLogObjectIDs() {
        return false;
    }
}
