package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocol.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.protocol.messaging.LoadProfileRegisterMessaging;
import com.energyict.protocol.messaging.PartialLoadProfileMessaging;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages.XemexMbusMessaging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 30/01/13 - 10:18
 */
public class MbusDevice extends AbstractNtaMbusDevice implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

    public MbusDevice() {
        super();
    }

    public MbusDevice(final AbstractSmartNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        super(meterProtocol, serialNumber, physicalAddress);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new XemexMbusMessaging();
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date: 2012-08-06 14:46:33 +0200 (ma, 06 aug 2012) $";
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> required = new ArrayList<>();
        return required;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optional = new ArrayList<>();
        return optional;
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder();
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder();
    }
}