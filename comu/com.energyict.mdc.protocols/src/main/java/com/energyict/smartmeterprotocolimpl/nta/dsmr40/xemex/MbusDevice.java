package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.protocols.messaging.LoadProfileRegisterMessaging;
import com.energyict.protocols.messaging.PartialLoadProfileMessaging;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages.XemexMbusMessaging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 30/01/13 - 10:18
 */
public class MbusDevice extends AbstractNtaMbusDevice implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

    private final TopologyService topologyService;

    public MbusDevice(TopologyService topologyService) {
        super();
        this.topologyService = topologyService;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new XemexMbusMessaging();
    }

    @Override
    public String getProtocolDescription() {
        return "XEMEX ReMI Mbus Slave";
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
        return new ArrayList<>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder(this.topologyService);
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder(topologyService);
    }

}