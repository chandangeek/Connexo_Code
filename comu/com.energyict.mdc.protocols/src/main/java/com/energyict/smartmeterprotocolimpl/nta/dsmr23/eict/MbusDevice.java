package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.MessageProtocol;

import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import javax.inject.Inject;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 12:00:53
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    @Inject
    public MbusDevice(TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, OrmClient ormClient) {
        super(topologyService, ormClient, readingTypeUtilService);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23MbusMessaging();
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder(this.getTopologyService());
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder(this.getTopologyService());
    }

}