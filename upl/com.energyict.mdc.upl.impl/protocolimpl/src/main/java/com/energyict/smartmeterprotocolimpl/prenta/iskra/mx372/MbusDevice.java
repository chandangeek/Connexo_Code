package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.cbo.Unit;
import com.energyict.mdw.core.Device;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.IskraMx372MbusMessaging;

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

    private final boolean hasBreaker;
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
        hasBreaker = true;
    }

    public MbusDevice(boolean hasBreaker) {
        this.hasBreaker = hasBreaker;
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
        this.hasBreaker = iskra.hasBreaker();
        if (mbus != null) {
            setProperties(mbus.getProtocolProperties().toStringProperties());
        }
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new IskraMx372MbusMessaging(hasBreaker);
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
        return "$Date: 2015-03-10 09:02:30 +0100 (Tue, 10 Mar 2015) $";
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

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        rtuType = properties.getProperty("DeviceType", "mbus");
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
