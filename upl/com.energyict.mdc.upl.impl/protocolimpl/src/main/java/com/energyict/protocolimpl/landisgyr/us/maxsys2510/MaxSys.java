package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

/**
 * US variant of the MaxSys protocol
 */
public class MaxSys extends com.energyict.protocolimpl.landisgyr.maxsys2510.MaxSys {

    private static final String PD_NODE_PREFIX = "F";   // Standalone (this allows us in the US to interrogate standalone meters)

    public MaxSys(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected String getpNodePrefix(TypedProperties p) {
        return p.getTypedProperty(PK_NODE_PREFIX, PD_NODE_PREFIX);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-04-20 14:29:26 +0200 (Mon, 20 Apr 2015) $";
    }

}