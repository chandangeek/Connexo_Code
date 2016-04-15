package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300Properties;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.EventLogs;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/11/11
 * Time: 14:48
 */
public class AS300DPET extends AS300 {

    @Override
    public String getProtocolDescription() {
        return "Elster AS300D-PET DLMS";
    }

    @Inject
    public AS300DPET(PropertySpecService propertySpecService, CalendarService calendarService, UserFileFactory userFileFactory, OrmClient ormClient) {
        super(propertySpecService, calendarService, userFileFactory, ormClient);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }

        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn, getProperties().getDeviceId());
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogReading) throws IOException {
        EventLogs logs = new EventLogs(this);
        Calendar fromCalendar = Calendar.getInstance(getTimeZone());
        if (lastLogReading == null) {
            lastLogReading = ParseUtils.getClearLastMonthDate(getTimeZone());
        }
        fromCalendar.setTime(lastLogReading);
        return logs.getEventLog(fromCalendar);
    }

    public AS300Properties getProperties() {
        if (properties == null) {
            properties = new AS300DPETProperties();
        }
        return properties;
    }

    @Override
    protected AS300LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new AS300DPETLoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    /**
     * Override because the parent method trims the serial number.
     *
     * @return Serial number of the devices, not trimmed
     * @throws java.io.IOException
     */
    @Override
    public String getMeterSerialNumber() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping serial number check!");
            return getSerialNumber();
        } else {
            OctetString serialNumber = (OctetString) getObjectFactory().getSerialNumber().getValueAttr();
            return serialNumber.stringValue();
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-06 17:15:55 +0200 (ma, 06 mei 2013) $";
    }

    @Override
    public AS300Messaging getMessageProtocol() {
        if (this.messageProtocol == null) {
            this.messageProtocol = new AS300DPETMessaging(new AS300DPETMessageExecutor(this, this.getCalendarService(), this.getUserFileFactory()));
        }
        return messageProtocol;
    }

}