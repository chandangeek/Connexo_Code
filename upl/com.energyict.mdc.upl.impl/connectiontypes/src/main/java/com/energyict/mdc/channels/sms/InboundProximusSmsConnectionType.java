package com.energyict.mdc.channels.sms;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.protocol.LegacyProtocolProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of the {@link com.energyict.mdc.tasks.ConnectionType} interface specific for inbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
@XmlRootElement
public class InboundProximusSmsConnectionType extends AbstractInboundSmsConnectionType {

    public static final String DEVICE_PHONE_NUMBER_PROPERTY_NAME = "phoneNumber";

    private PropertySpec phoneNumberPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(DEVICE_PHONE_NUMBER_PROPERTY_NAME);
    }

    private PropertySpec callHomeIdPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case DEVICE_PHONE_NUMBER_PROPERTY_NAME:
                return this.phoneNumberPropertySpec();
            case LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME:
                return this.callHomeIdPropertySpec();
            default:
                return null;
        }
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return DEVICE_PHONE_NUMBER_PROPERTY_NAME.equals(name) ||
                LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME.equals(name);
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-02-03 13:33:01 +0100 (Tue, 03 Feb 2015) $";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Arrays.asList(phoneNumberPropertySpec(), callHomeIdPropertySpec());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }
}
