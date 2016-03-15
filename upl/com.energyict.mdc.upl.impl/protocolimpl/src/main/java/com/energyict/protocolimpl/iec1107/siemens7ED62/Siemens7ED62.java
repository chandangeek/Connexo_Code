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

import com.energyict.protocol.*;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author  Koen
 */
public class Siemens7ED62 extends AbstractIEC1107Protocol implements SerialNumberSupport {
    
    
    Siemens7ED62Registry siemens7ED62Registry=null;
    Siemens7ED62Profile siemens7ED62Profile=null;
    
    /** Creates a new instance of Siemens7ED62 */
    public Siemens7ED62() {
        super(true);
    }
    
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

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:27 +0200 (Thu, 26 Nov 2015)$";
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCalendar(getTimeZone());
        fromCalendar.add(Calendar.YEAR,-10);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }
    
    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
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
    
    
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return getSiemens7ED62Registry().getRegister(name).toString();
    }
    
    protected List doGetOptionalKeys() {
        return null;
    }
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    }
    
    public Date getTime() throws IOException {
        Date dateTime = (Date)getSiemens7ED62Registry().getRegister("DateTime");
        Date date = new Date(dateTime.getTime()-getRoundtripCorrection());
        return date;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getChannelMap().getNrOfChannels();
    }
    
    /**
     * Getter for property siemens7ED62Registry.
     * @return Value of property siemens7ED62Registry.
     */
    public com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62Registry getSiemens7ED62Registry() {
        return siemens7ED62Registry;
    }
    
    /**
     * Getter for property siemens7ED62Profile.
     * @return Value of property siemens7ED62Profile.
     */
    public com.energyict.protocolimpl.iec1107.siemens7ED62.Siemens7ED62Profile getSiemens7ED62Profile() {
        return siemens7ED62Profile;
    }
}
