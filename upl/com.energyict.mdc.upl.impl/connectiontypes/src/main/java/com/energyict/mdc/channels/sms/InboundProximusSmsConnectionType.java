package com.energyict.mdc.channels.sms;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.xml.bind.annotation.XmlRootElement;
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
        return UPLPropertySpecFactory.string(DEVICE_PHONE_NUMBER_PROPERTY_NAME, true);
    }

    private PropertySpec callHomeIdPropertySpec() {
        return UPLPropertySpecFactory.string(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, true);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(phoneNumberPropertySpec(), callHomeIdPropertySpec());
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-02-03 13:33:01 +0100 (Tue, 03 Feb 2015) $";
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }
}
