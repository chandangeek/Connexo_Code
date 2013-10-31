package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 3-mrt-2011
 * Time: 16:32:09
 */
public class SlaveMeter extends AbstractSlaveMeter implements SimpleMeter {

    private final WebRTUZ3 meterProtocol;
    private final String serialNumber;
    private final int physicalAddress;

    /**
     * Default constructor for EIServer instantiations
     */
    public SlaveMeter() {
        this(null, null, -1);
    }

    public SlaveMeter(WebRTUZ3 meterProtocol, String serialNumber, int physicalAddress) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = serialNumber;
        this.physicalAddress = physicalAddress;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 Slave";
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
    public void addProperties(final Properties properties) {
        // currently nothing to do
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

    /**
     * Return the DeviceTimeZone
     *
     * @return the DeviceTimeZone
     */
    public TimeZone getTimeZone() {
        return this.meterProtocol.getTimeZone();
    }

    /**
     * Getter for the used Logger
     *
     * @return the Logger
     */
    public Logger getLogger() {
        return this.meterProtocol.getLogger();
    }

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    public String getSerialNumber() {
        return this.serialNumber;
    }

    public WebRTUZ3 getMeterProtocol() {
        return meterProtocol;
    }

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return this.physicalAddress;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return this.meterProtocol.getDlmsSession().getCosemObjectFactory();
    }

    public DLMSMeterConfig getMeterConfig() {
        return this.meterProtocol.getDlmsSession().getMeterConfig();
    }
}
