package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.cbo.Unit;
import com.energyict.cpo.*;
import com.energyict.mdw.core.Device;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.IskraMx372MbusMessaging;

import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 24/01/12
 * Time: 16:27
 */
public class MbusDevice extends AbstractNtaMbusDevice implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

    private int mbusAddress = -1;        // this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
    private int physicalAddress = -1;    // this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
    private int medium = 15;

    private String customerID;
    private String rtuType;
    private Unit mbusUnit;

    private IskraMx372 iskra;
    public Device mbus;
    private Logger logger;

    public MbusDevice() {
    }

    public MbusDevice(int mbusAddress, int phyAddress, String serial, int mbusMedium, Device rtu, Unit unit, IskraMx372 protocol) throws InvalidPropertyException, MissingPropertyException {
        this.mbusAddress = mbusAddress;
        this.physicalAddress = phyAddress;
        this.customerID = serial;
        this.medium = mbusMedium;
        this.mbus = rtu;
        this.mbusUnit = unit;
        this.logger = protocol.getLogger();
        this.iskra = protocol;
        if (mbus != null) {
            setProperties(mbus.getProperties().toStringProperties());
        }
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new IskraMx372MbusMessaging();
    }

    public LoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return ((IskraMx372MbusMessaging) getMessageProtocol()).getLoadProfileRegisterMessageBuilder();
    }

    public PartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return ((IskraMx372MbusMessaging) getMessageProtocol()).getPartialLoadProfileMessageBuilder();
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
    public void addProperties(Properties properties) {
        try {
            setProperties(properties);
        } catch (InvalidPropertyException e) {
            e.printStackTrace();
        } catch (MissingPropertyException e) {
            e.printStackTrace();
        }
    }

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        rtuType = properties.getProperty("DeviceType", "mbus");
    }

    /**
     * Returns a list of required property keys
     *
     * @return a List of String objects
     */
    public List<String> getRequiredKeys() {
        return new ArrayList(0);
    }

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    public List<String> getOptionalKeys() {
        return new ArrayList(0);
    }

    public String getCustomerID() {
        return customerID;
    }

    @Override
    public int getPhysicalAddress() {
        return physicalAddress;
    }

    public int getMbusAddress() {
        return mbusAddress;
    }

    public int getMedium() {
        return medium;
    }

    public String getRtuType() {
        return rtuType;
    }

    public Unit getMbusUnit() {
        return mbusUnit;
    }

    public IskraMx372 getIskra() {
        return iskra;
    }

    public Device getMbus() {
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
