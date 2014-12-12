package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;

import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.IskraMx372MbusMessaging;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 24/01/12
 * Time: 16:27
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private IskraMx372 iskra;
    private String customerID;
    public BaseDevice mbus;
    private Logger logger;

    @Inject
    public MbusDevice(TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, OrmClient ormClient) {
        super(topologyService, ormClient, readingTypeUtilService);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new IskraMx372MbusMessaging(this.getTopologyService());
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return ((IskraMx372MbusMessaging) getMessageProtocol()).getLoadProfileRegisterMessageBuilder();
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return ((IskraMx372MbusMessaging) getMessageProtocol()).getPartialLoadProfileMessageBuilder();
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
        addProperties(properties.toStringProperties());
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    /**
     * add the properties
     *
     * @param properties properties to add
     */
    public void addProperties(Properties properties) {
        setProperties(properties);
    }

    public void setProperties(Properties properties) {
    }

    /**
     * Returns a list of required property keys
     *
     * @return a List of String objects
     */
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    public List<String> getOptionalKeys() {
        return Collections.emptyList();
    }

    public String getCustomerID() {
        return customerID;
    }

    @Override
    public int getPhysicalAddress() {
        return -1;
    }

    public IskraMx372 getIskra() {
        return iskra;
    }

    public BaseDevice getMbus() {
        return mbus;
    }

    /**
     * Getter for the used Logger
     *
     * @return the Logger
     */
    @Override
    public Logger getLogger() {
        return logger;
    }

}