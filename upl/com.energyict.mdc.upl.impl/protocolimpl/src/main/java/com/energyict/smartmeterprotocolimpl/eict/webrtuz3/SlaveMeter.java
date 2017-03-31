package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.support.SerialNumberSupport;
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
public class SlaveMeter extends AbstractSlaveMeter implements SimpleMeter, SerialNumberSupport {

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
        return "EnergyICT WebRTU Z3 DLMS TIC Slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-26 15:23:42 +0200 (Thu, 26 Nov 2015)$";
    }

    /**
     * add the properties
     *
     * @param properties properties to add
     */
    public void addProperties(final Properties properties) {
        // currently nothing to do
    }

    @Override
    public TimeZone getTimeZone() {
        return this.meterProtocol.getTimeZone();
    }

    @Override
    public Logger getLogger() {
        return this.meterProtocol.getLogger();
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    public WebRTUZ3 getMeterProtocol() {
        return meterProtocol;
    }

    @Override
    public int getPhysicalAddress() {
        return this.physicalAddress;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return this.meterProtocol.getDlmsSession().getCosemObjectFactory();
    }

    public DLMSMeterConfig getMeterConfig() {
        return this.meterProtocol.getDlmsSession().getMeterConfig();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {

    }

}