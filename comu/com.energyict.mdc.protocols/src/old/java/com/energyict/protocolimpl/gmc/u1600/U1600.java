/*
 * U1600.java
 *
 * Created on 26. August 2004, 16:01
 */

package com.energyict.protocolimpl.gmc.u1600;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.gmc.base.EclConnection;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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

    @Override
    public String getProtocolDescription() {
        return "GMC U1600";
    }

    LogicalAddressFactory logicalAddressFactory;
    U1600Profile u1600Profile=null;
    EclConnection eclConnection=null;

    @Inject
    public U1600(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }



    protected void doConnect() throws java.io.IOException {
       logicalAddressFactory = new LogicalAddressFactory(this, this);

        u1600Profile = new U1600Profile(this);
    }

    protected void doDisConnect() throws IOException {
    }


    protected List doGetOptionalKeys() {
        return null;
    }

    protected ProtocolConnection doInit(java.io.InputStream inputStream, java.io.OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        eclConnection=new EclConnection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor);
        return eclConnection;

    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    }


   public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCalendar(getTimeZone());
        calendarFrom.setTime(lastReading);
        Calendar calendarTo = ProtocolUtils.getCalendar(getTimeZone());
        calendarTo.setTime(new Date());
        return u1600Profile.getProfileData(calendarFrom.getTime(),calendarTo.getTime());
    }

  public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCalendar(getTimeZone());
        calendarFrom.setTime(from);
        Calendar calendarTo = ProtocolUtils.getCalendar(getTimeZone());
        calendarTo.setTime(to);
        return u1600Profile.getProfileData(calendarFrom.getTime(),calendarTo.getTime());
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    // KV 04122006
    public void setTime() throws IOException {
        DateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
        sdf.setTimeZone(getTimeZone());
        String timeDateString = sdf.format(new Date());
        //System.out.println(timeDateString);
        getEclConnection().setTimeDateString(timeDateString);
    }
       /*
     * Override this method if the subclass wants to get the device time
     */
    // KV 04122006
    public Date getTime() throws IOException {
        Date date;
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
    } // public Date getTime() throws IOException


       /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getLogicalAddressFactory());
        return ocm.getRegisterValue(obisCode);
    }


     /**
     * Getter for property logicalAddressFactory.
     * @return Value of property logicalAddressFactory.
     */
    public com.energyict.protocolimpl.gmc.u1600.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }
      /**
     * Getter for property flagIEC1107Connection, the low level communication implementation class.
     * @return Value of property flagIEC1107Connection.
     */
    //public com.energyict.protocolimpl.gmc.base.EclConnection getEclConnection() {
    public EclConnection getEclConnection() {
        return eclConnection;
    }


}
