package com.energyict.mdc.protocol.inbound.general;

import com.energyict.cpo.Environment;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;

import java.util.List;

/**
 * In the case of DialHomeIdRequestDiscover, a meter starts an inbound session and pushes its unique Call Home ID and additional meter data.
 * There are no extra requests sent by the comserver.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/06/12
 * Time: 14:50
 */
public class DialHomeIdRequestDiscover extends RequestDiscover {

    private static final String CALL_HOME_ID = Environment.getDefault().getTranslation("deviceDialHomeId");

    @Override
    public List<PropertySpec> getRequiredProperties() {
        final List<PropertySpec> requiredProperties = super.getRequiredProperties();
        requiredProperties.add(PropertySpecFactory.stringPropertySpec(CALL_HOME_ID));
        return requiredProperties;
    }

    @Override
    protected void setSerialNumber(String callHomeId) {
        // The 'SerialId' field contains the unique devices Call Home Id.
        setDeviceIdentifier(new DialHomeIdDeviceIdentifier(callHomeId));
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}