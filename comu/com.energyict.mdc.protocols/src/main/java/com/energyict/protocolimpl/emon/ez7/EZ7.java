/*
 * EZ7.java
 *
 * Created on 27 april 2005, 11:03
 */

package com.energyict.protocolimpl.emon.ez7;


import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolChannel;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.SecurityLevelException;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;
import com.energyict.protocolimpl.emon.ez7.core.EZ7Connection;
import com.energyict.protocolimpl.emon.ez7.core.EZ7Profile;
import com.energyict.protocolimpl.emon.ez7.core.ObisCodeMapper;
import com.energyict.protocolimpl.emon.ez7.core.command.SetKey;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author  Koen
 */
public class EZ7 extends AbstractProtocol {

    @Override
    public String getProtocolDescription() {
        return "EMON EZ7";
    }

    EZ7Connection ez7Connection=null;
    EZ7Profile ez7Profile=null;
    EZ7CommandFactory ez7CommandFactory=null;

    @Inject
    public EZ7(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return ez7CommandFactory.getVersion().getCompleteVersionString();
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return ez7Profile.getProfileData(from,to,includeEvents);
    }


    protected void doConnect() throws java.io.IOException {
        ez7Profile = new EZ7Profile(this);
        ez7CommandFactory = new EZ7CommandFactory(this);
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0)) {
            ez7CommandFactory.getSetKey().logon(getInfoTypePassword());
        }
    }

    protected void doDisConnect() throws IOException {
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0)) {
            ez7CommandFactory.getSetKey().logoff();
        }

        // This command must be initiated when the meter sets up the connection to indicate a successfull read!
        // See page 1-32 of the protocoldescription...
        // Do not wait for a response since the meter hangsup the connection!
        getEz7Connection().sendCommand("SRD","0",false);
    }

    protected void validateSerialNumber() throws IOException {
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
            return;
        }
        String sn = getEz7CommandFactory().getRGLInfo().getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }

    protected void validateDeviceId() throws IOException {
        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) {
            return;
        }
        String deviceId = getEz7CommandFactory().getRGLInfo().getDeviceId();
        if (deviceId.compareTo(getInfoTypeDeviceID()) == 0) {
            return;
        }
        throw new IOException("DeviceId mismatch! meter DeviceId="+deviceId+", configured deviceId="+getInfoTypeDeviceID());
    }


    protected List<String> doGetOptionalKeys() {
        return Collections.emptyList();
    }

    protected ProtocolConnection doInit(java.io.InputStream inputStream, java.io.OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws java.io.IOException {
        ez7Connection = new EZ7Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,halfDuplexController);
        return ez7Connection;
    }

    protected void doValidateProperties(java.util.Properties properties) throws MissingPropertyException, InvalidPropertyException {
        //halfDuplex=Integer.parseInt(properties.getProperty("HalfDuplex","20").trim());
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0) && (getInfoTypePassword().length() != 16)) {
            throw new InvalidPropertyException("EZ7, doValidateProperties, password length error! Password must have a length of 16 characters!");
        }
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"1A"));
    }



    public java.util.Date getTime() throws java.io.IOException {

        // to verify device timezone against meter timezone...
        ez7CommandFactory.getImonInformation().getTimeZone();


        return ez7CommandFactory.getRTC().getDate();
    }

    /*
     * Override this method if the subclass wants to set the device time
     */
    public void setTime() throws IOException {
        int accessLevel = getEz7CommandFactory().getSetKey().getAccessLevel();
        if (accessLevel < 2) {
            throw new SecurityLevelException("EZ7, setTime(), accesslevel is " + SetKey.ACCESSLEVELS[accessLevel]);
        }
        ez7CommandFactory.setRTC();
    }


    public int getProtocolChannelValue(int channel) {
        if (getProtocolChannelMap()==null) {
            return -1;
        }
        else {
            ProtocolChannel pc = ez7CommandFactory.getEz7().getProtocolChannelMap().getProtocolChannel(channel);
            if (pc==null) {
                return -1;
            }
            else {
                return pc.getValue();
            }
        }
    }

    public int getProfileInterval() throws IOException {
        return ez7CommandFactory.getProfileStatus().getProfileInterval();
    }

    public int getNumberOfChannels() throws IOException {
        return ez7CommandFactory.getHookUp().getNrOfChannels();
    }


    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getEz7CommandFactory());
        return ocm.getRegisterValue(obisCode);
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    public String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("******************************************************************\n");
        builder.append("Manufacturer specific registers with code 0.B.96.99.E.F\n");
        builder.append("B=0, REG, Event General\n");
        builder.append("B=1, REL, Event Load\n");
        builder.append("B=2, RF, Flags status\n");
        builder.append("B=3, RGL, Read group & location & recorder id & serial#\n");
        builder.append("E=row (e.g. 0=LINE-1, 1=LINE-2,... \n");
        builder.append("F=col (0..7) 1 of the 8 values of the data LINE...\n");
        builder.append("******************************************************************\n");
        builder.append("Cumulative energy registers\n");
        for (int channel=1;channel<=8;channel++) {
            for (int tariff=1;tariff<=8;tariff++) {
                ObisCode obisCode = new ObisCode(1,channel,1,8,tariff,255);
                builder.append(obisCode).append(", ").append(obisCode.getDescription()).append("\n");
            }
        }
        builder.append("******************************************************************\n");
        builder.append("Maximum demand registers\n");
        for (int channel=1;channel<=8;channel++) {
            for (int tariff=1;tariff<=8;tariff++) {
                ObisCode obisCode = new ObisCode(1,channel,1,6,tariff,255);
                builder.append(obisCode).append(", ").append(obisCode.getDescription()).append("\n");
            }
        }
        builder.append("******************************************************************\n");
        builder.append("Sliding demand registers (12 x 5 minute sliding demand registers)\n");
        for (int channel=1;channel<=8;channel++) {
            for (int tariff=1;tariff<=12;tariff++) {
                ObisCode obisCode = new ObisCode(1,channel,1,5,tariff,255);
                builder.append(obisCode).append(", ").append(tariff).append("-the 5 minute sliding demand register\n");
            }
        }
        builder.append("******************************************************************\n");
        builder.append("Power quality, instantaneous values \n");
        this.appendObisCodeDescription(builder, "1.0.1.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.21.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.61.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.11.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.31.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.51.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.71.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.12.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.32.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.52.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.72.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.13.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.33.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.53.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.73.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.714.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.34.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.54.7.0.255");
        this.appendObisCodeDescription(builder, "1.0.74.7.0.255");
        return builder.toString();
    }

    protected void appendObisCodeDescription(StringBuilder builder, String obisCode) {
        builder.append(obisCode).append(", ").append(ObisCode.fromString(obisCode).getDescription()).append("\n");
    }

    /**
     * Getter for property ez7Connection.
     * @return Value of property ez7Connection.
     */
    public com.energyict.protocolimpl.emon.ez7.core.EZ7Connection getEz7Connection() {
        return ez7Connection;
    }

    /**
     * Getter for property ez7Profile.
     * @return Value of property ez7Profile.
     */
    public com.energyict.protocolimpl.emon.ez7.core.EZ7Profile getEz7Profile() {
        return ez7Profile;
    }

    /**
     * Getter for property ez7CommandFactory.
     * @return Value of property ez7CommandFactory.
     */
    public com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory getEz7CommandFactory() {
        return ez7CommandFactory;
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        Properties properties = new Properties();
        setProperties(properties);
        init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
        connect();
        return getEz7CommandFactory().getRGLInfo().getSerialNumber();
    }

}
