/*
 * U1600.java
 *
 * Created on 26. August 2004, 16:01
 */

package com.energyict.protocolimpl.gmc.u1600;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.gmc.base.EclConnection;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import com.energyict.protocolimpl.myprotocol.*;

/**
 *
 * @author  weinert
 * @beginchanges
K&P||Initial version
KV|23032005|Changed header to be compatible with protocol version tool
KV|05052006|Avoid NULL unit in ChannelInfo and use deviceID to access LON registgers
KV|11052006|Avoid NumberFormatException in TotalRegisters parse()
KV|11052006|Avoid NumberFormatException in TotalRegisters parse() fix
KV|18052006|fixes
KV|04122006|Implement setTime() & getTime() and fix DST transision behaviour
 * @endchanges
 */
public class U1600 extends AbstractProtocol {

    private LogicalAddressFactory logicalAddressFactory;
    private U1600Profile u1600Profile = null;
    private EclConnection eclConnection = null;

    public U1600(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    protected void doConnect() throws java.io.IOException {
       logicalAddressFactory = new LogicalAddressFactory(this, this);

        u1600Profile = new U1600Profile(this);
    }

    @Override
    protected void doDisconnect() throws IOException {
    }

    @Override
    protected ProtocolConnection doInit(java.io.InputStream inputStream, java.io.OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws java.io.IOException {
        eclConnection=new EclConnection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor);
        return eclConnection;
    }

    @Override
   public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCalendar(getTimeZone());
        calendarFrom.setTime(lastReading);
        Calendar calendarTo = ProtocolUtils.getCalendar(getTimeZone());
        calendarTo.setTime(new Date());
        return u1600Profile.getProfileData(calendarFrom.getTime(),calendarTo.getTime());
    }

    @Override
  public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCalendar(getTimeZone());
        calendarFrom.setTime(from);
        Calendar calendarTo = ProtocolUtils.getCalendar(getTimeZone());
        calendarTo.setTime(to);
        return u1600Profile.getProfileData(calendarFrom.getTime(),calendarTo.getTime());
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Thu Nov 3 10:52:06 2016 +0100 $";
    }

    // KV 04122006
    @Override
    public void setTime() throws IOException {
        DateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
        sdf.setTimeZone(getTimeZone());
        String timeDateString = sdf.format(new Date());
        //System.out.println(timeDateString);
        getEclConnection().setTimeDateString(timeDateString);
    }

    // KV 04122006
    @Override
    public Date getTime() throws IOException {
        Date date = null;
        String timeDateString = getEclConnection().getTimeDateString().trim();
        DateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
        sdf.setTimeZone(getTimeZone());

        try {
            date = sdf.parse(timeDateString);
            return date;
        }
        catch(ParseException e) {
            throw new NestedIOException(e,"U1600, getTime(), Error parsing the timeDateString "+timeDateString);
        }
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getLogicalAddressFactory());
        return ocm.getRegisterValue(obisCode);
    }

    private com.energyict.protocolimpl.gmc.u1600.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }

    EclConnection getEclConnection() {
        return eclConnection;
    }

}