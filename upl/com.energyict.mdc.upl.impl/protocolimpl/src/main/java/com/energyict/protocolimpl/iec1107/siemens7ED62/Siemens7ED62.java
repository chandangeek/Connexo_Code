/*
 * Siemens7ED62.java
 *
 * <B>Description :</B><BR>
 * Class that implements the Siemens7ED62 IEC1107-VDEW protocol version of the meter.
 * <BR>
 * <B>@beginchanges</B><BR>
KV|05072004|Initial version
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
KV|06092005|VDEW changed to do channel mapping!
 * @endchanges
 */

package com.energyict.protocolimpl.iec1107.siemens7ED62;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class Siemens7ED62 extends AbstractIEC1107Protocol implements SerialNumberSupport {

    private Siemens7ED62Registry siemens7ED62Registry = null;
    private Siemens7ED62Profile siemens7ED62Profile = null;

    public Siemens7ED62(PropertySpecService propertySpecService) {
        super(true, propertySpecService);
    }

    @Override
    protected void doConnect() throws IOException {
        siemens7ED62Registry = new Siemens7ED62Registry(this,this);
        siemens7ED62Profile = new Siemens7ED62Profile(this,this,siemens7ED62Registry);
    }

    @Override
    public String getSerialNumber() {
        try {
            return (String)getSiemens7ED62Registry().getRegister("MeterSerialNumber");
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:27 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCalendar(getTimeZone());
        fromCalendar.add(Calendar.YEAR,-10);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        toCalendar.setTime(to);
        return doGetProfileData(fromCalendar,toCalendar,includeEvents);
    }

    private ProfileData doGetProfileData(Calendar fromCalendar,Calendar toCalendar,boolean includeEvents) throws IOException {
        return getSiemens7ED62Profile().getProfileData(fromCalendar,
        toCalendar,
        getNumberOfChannels(),
        1,
        includeEvents);
    }

    // Only for debugging
    public ProfileData getProfileData(Calendar from,Calendar to) throws IOException {
        return getSiemens7ED62Profile().getProfileData(from,
        to,
        getNumberOfChannels(),
        1,
        false);
    }

    @Override
    public String getRegister(String name) throws IOException {
        return getSiemens7ED62Registry().getRegister(name).toString();
    }

    @Override
    public Date getTime() throws IOException {
        Date dateTime = (Date)getSiemens7ED62Registry().getRegister("DateTime");
        return new Date(dateTime.getTime()-getRoundtripCorrection());
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getChannelMap().getNrOfChannels();
    }

    private com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62Registry getSiemens7ED62Registry() {
        return siemens7ED62Registry;
    }

    private com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62Profile getSiemens7ED62Profile() {
        return siemens7ED62Profile;
    }

}