package com.energyict.protocolimpl.iec1107.unilog;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 7-dec-2010
 * Time: 16:51:30
 */
abstract class AbstractUnilog extends PluggableMeterProtocol implements RegisterProtocol, ProtocolLink, MeterExceptionInfo {

    private Logger logger;
    private TimeZone timeZone;

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException("Method 'getMeterReading(String name)' not supported in the Unigas300 protocol.");
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException("Method 'getMeterReading(int channelId)' not supported in the Unigas300 protocol.");
    }

    @Override
    public String getRegister(String name) throws UnsupportedException {
        throw new UnsupportedException("Method 'getRegister(String name)' not supported in the Unigas300 protocol.");
    }

    @Override
    public void setRegister(String name, String value) throws UnsupportedException {
        throw new UnsupportedException("Method 'getRegister(String name)' not supported in the Unigas300 protocol.");
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException("Method 'initializeDevice()' not supported in the Unigas300 protocol.");
    }

    @Override
    public ChannelMap getChannelMap() {
        return null;
    }

    @Override
    public void release() {
    }

    @Override
    public String getExceptionInfo(String id) {
        if (id != null && "ERROR".equals(id)) {
            return "Request could not execute!";
        } else {
            return "No meter specific exception info for " + id;
        }
    }

}