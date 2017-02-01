package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdw.core.Device;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.IskraMx372MbusMessaging;

import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 24/01/12
 * Time: 16:27
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private final boolean hasBreaker;
    public Device mbus;
    private int mbusAddress = -1;        // this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
    private int physicalAddress = -1;    // this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
    private int medium = 15;
    private String customerID;
    private String rtuType;
    private Unit mbusUnit;
    private IskraMx372 iskra;
    private Logger logger;

    public MbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        this(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor, true);
    }

    public MbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, boolean hasBreaker) {
        super(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor);
        this.hasBreaker = hasBreaker;
    }

    public MbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, int mbusAddress, int phyAddress, String serial, int mbusMedium, Device rtu, Unit unit, IskraMx372 protocol) {
        this(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor, protocol.hasBreaker());
        this.mbusAddress = mbusAddress;
        this.physicalAddress = phyAddress;
        this.customerID = serial;
        this.medium = mbusMedium;
        this.mbus = rtu;
        this.mbusUnit = unit;
        this.logger = protocol.getLogger();
        this.iskra = protocol;
        if (mbus != null) {
            setUPLProperties(mbus.getProtocolProperties());
        }
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new IskraMx372MbusMessaging(hasBreaker);
    }

    @Override
    public String getVersion() {
        return "$Date: Mon Jan 2 11:14:35 2017 +0100 $";
    }

    public void setUPLProperties(TypedProperties properties) {
        rtuType = properties.getTypedProperty("DeviceType", "mbus");
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

    @Override
    public Logger getLogger() {
        return logger;
    }

}