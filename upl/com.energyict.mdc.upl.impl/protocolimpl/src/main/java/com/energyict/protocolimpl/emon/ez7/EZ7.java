/*
 * EZ7.java
 *
 * Created on 27 april 2005, 11:03
 */

package com.energyict.protocolimpl.emon.ez7;


import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolChannel;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.SecurityLevelException;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;
import com.energyict.protocolimpl.emon.ez7.core.EZ7Connection;
import com.energyict.protocolimpl.emon.ez7.core.EZ7Profile;
import com.energyict.protocolimpl.emon.ez7.core.ObisCodeMapper;
import com.energyict.protocolimpl.emon.ez7.core.command.SetKey;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author  Koen
 */
public class EZ7 extends AbstractProtocol implements SerialNumberSupport {

    // core objects
    private EZ7Connection ez7Connection = null;
    private EZ7Profile ez7Profile = null;
    private EZ7CommandFactory ez7CommandFactory = null;

    public EZ7(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getSerialNumber() {
        try {
            return getEz7CommandFactory().getRGLInfo().getSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:46 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return ez7CommandFactory.getVersion().getCompleteVersionString();
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return ez7Profile.getProfileData(from,to,includeEvents);
    }

    @Override
    protected void doConnect() throws IOException {
        ez7Profile = new EZ7Profile(this);
        ez7CommandFactory = new EZ7CommandFactory(this);
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0)) {
            ez7CommandFactory.getSetKey().logon(getInfoTypePassword());
        }
    }

    @Override
    protected void doDisconnect() throws IOException {
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0)) {
            ez7CommandFactory.getSetKey().logoff();
        }

        // This command must be initiated when the meter sets up the connection to indicate a successfull read!
        // See page 1-32 of the protocoldescription...
        // Do not wait for a response since the meter hangsup the connection!
        getEz7Connection().sendCommand("SRD","0",false);
    }

    @Override
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

    @Override
    protected ProtocolConnection doInit(java.io.InputStream inputStream, java.io.OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws java.io.IOException {
        ez7Connection = new EZ7Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,halfDuplexController);
        return ez7Connection;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0) && (getInfoTypePassword().length() != 16)) {
            throw new InvalidPropertyException("EZ7, doValidateProperties, password length error! Password must have a length of 16 characters!");
        }
        setInfoTypeNodeAddress(properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "1A"));
    }

    @Override
    public Date getTime() throws IOException {
        // to verify device timezone against meter timezone...
        ez7CommandFactory.getImonInformation().getTimeZone();
        return ez7CommandFactory.getRTC().getDate();
    }

    @Override
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
        } else {
            ProtocolChannel pc = ez7CommandFactory.getEz7().getProtocolChannelMap().getProtocolChannel(channel);
            if (pc == null) {
                return -1;
            } else {
                return pc.getValue();
            }
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        return ez7CommandFactory.getProfileStatus().getProfileInterval();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return ez7CommandFactory.getHookUp().getNrOfChannels();
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getEz7CommandFactory());
        return ocm.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
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
                builder.append(obisCode).append(", ").append(obisCode.toString()).append("\n");
            }
        }
        builder.append("******************************************************************\n");
        builder.append("Maximum demand registers\n");
        for (int channel=1;channel<=8;channel++) {
            for (int tariff=1;tariff<=8;tariff++) {
                ObisCode obisCode = new ObisCode(1,channel,1,6,tariff,255);
                builder.append(obisCode).append(", ").append(obisCode.toString()).append("\n");
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
        builder.append("1.0.1.7.0.255, ").append(ObisCode.fromString("1.0.1.7.0.255").toString()).append("\n");
        builder.append("1.0.21.7.0.255, ").append(ObisCode.fromString("1.0.21.7.0.255").toString()).append("\n");
        builder.append("1.0.41.7.0.255, ").append(ObisCode.fromString("1.0.41.7.0.255").toString()).append("\n");
        builder.append("1.0.61.7.0.255, ").append(ObisCode.fromString("1.0.61.7.0.255").toString()).append("\n");
        builder.append("1.0.11.7.0.255, ").append(ObisCode.fromString("1.0.11.7.0.255").toString()).append("\n");
        builder.append("1.0.31.7.0.255, ").append(ObisCode.fromString("1.0.31.7.0.255").toString()).append("\n");
        builder.append("1.0.51.7.0.255, ").append(ObisCode.fromString("1.0.51.7.0.255").toString()).append("\n");
        builder.append("1.0.71.7.0.255, ").append(ObisCode.fromString("1.0.71.7.0.255").toString()).append("\n");
        builder.append("1.0.12.7.0.255, ").append(ObisCode.fromString("1.0.12.7.0.255").toString()).append("\n");
        builder.append("1.0.32.7.0.255, ").append(ObisCode.fromString("1.0.32.7.0.255").toString()).append("\n");
        builder.append("1.0.52.7.0.255, ").append(ObisCode.fromString("1.0.52.7.0.255").toString()).append("\n");
        builder.append("1.0.72.7.0.255, ").append(ObisCode.fromString("1.0.72.7.0.255").toString()).append("\n");
        builder.append("1.0.13.7.0.255, ").append(ObisCode.fromString("1.0.13.7.0.255").toString()).append("\n");
        builder.append("1.0.33.7.0.255, ").append(ObisCode.fromString("1.0.33.7.0.255").toString()).append("\n");
        builder.append("1.0.53.7.0.255, ").append(ObisCode.fromString("1.0.53.7.0.255").toString()).append("\n");
        builder.append("1.0.73.7.0.255, ").append(ObisCode.fromString("1.0.73.7.0.255").toString()).append("\n");
        builder.append("1.0.14.7.0.255, ").append(ObisCode.fromString("1.0.14.7.0.255").toString()).append("\n");
        builder.append("1.0.34.7.0.255, ").append(ObisCode.fromString("1.0.34.7.0.255").toString()).append("\n");
        builder.append("1.0.54.7.0.255, ").append(ObisCode.fromString("1.0.54.7.0.255").toString()).append("\n");
        builder.append("1.0.74.7.0.255, ").append(ObisCode.fromString("1.0.74.7.0.255").toString()).append("\n");

        return builder.toString();
    }

    public com.energyict.protocolimpl.emon.ez7.core.EZ7Connection getEz7Connection() {
        return ez7Connection;
    }

    public com.energyict.protocolimpl.emon.ez7.core.EZ7Profile getEz7Profile() {
        return ez7Profile;
    }

    public com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory getEz7CommandFactory() {
        return ez7CommandFactory;
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        TypedProperties properties = com.energyict.cpo.TypedProperties.empty();

        setUPLProperties(properties);
        init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
        connect();
        // disconnect(); // no disconnect because the meter will hangup the link... disconnect contains an EZ7 protocol command to the meter that hangup the link!
        return getEz7CommandFactory().getRGLInfo().getSerialNumber();
    }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return super.getProtocolChannelMap();
    }

}