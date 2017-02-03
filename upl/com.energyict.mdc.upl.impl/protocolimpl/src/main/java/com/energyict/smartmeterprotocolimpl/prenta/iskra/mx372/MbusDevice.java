package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
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
    private int physicalAddress = -1;    // this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
    private String customerID;
    private Logger logger;

    public MbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        this(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor, true);
    }

    public MbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor, boolean hasBreaker) {
        super(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor);
        this.hasBreaker = hasBreaker;
    }

    public MbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor, int phyAddress, String serial, IskraMx372 protocol) {
        this(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor, protocol.hasBreaker());
        this.physicalAddress = phyAddress;
        this.customerID = serial;
        this.logger = protocol.getLogger();
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
    }

    public String getCustomerID() {
        return customerID;
    }

    @Override
    public int getPhysicalAddress() {
        return physicalAddress;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

}