package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.protocols.messaging.LoadProfileRegisterMessaging;
import com.energyict.protocols.messaging.PartialLoadProfileMessaging;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Place holder class for the MBus device.
 * Contains the standard DSMR 2.3 MBus messages (connect control and setup)
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 16:04
 * Author: khe
 */
public class MBusDevice extends AbstractNtaMbusDevice implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

    private final TopologyService topologyService;

    @Inject
    public MBusDevice(TopologyService topologyService) {
        super();
        this.topologyService = topologyService;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23MbusMessaging();
    }

    @Override
    public String getProtocolDescription() {
        return "Landis+Gyr Mbus Slave NTA DSMR 4.0";
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder(this.topologyService);
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder(topologyService);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }
}
