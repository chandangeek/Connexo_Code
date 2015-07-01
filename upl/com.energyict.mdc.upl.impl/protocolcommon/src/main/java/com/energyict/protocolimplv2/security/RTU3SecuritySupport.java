package com.energyict.protocolimplv2.security;

/**
 * Extension of the DSMR security set, adding the master key.
 * This is a different relation type in the EIServer database, because it has a new property.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 11:45
 */
public class RTU3SecuritySupport extends DsmrSecuritySupport {

    public RTU3SecuritySupport() {
        setIncludeMasterKey(true);
    }

    @Override
    public String getSecurityRelationTypeName() {   //Make sure this is a different relation type, it includes an extra property
        return SecurityRelationTypeName.RTU3_DLMS_SECURITY.toString();
    }
}