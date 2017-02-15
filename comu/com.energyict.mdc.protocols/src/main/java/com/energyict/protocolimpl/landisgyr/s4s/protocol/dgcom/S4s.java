/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * S4s.java
 *
 * Created on 22 mei 2006, 14:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;

import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command.CommandFactory;
import com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.registermappping.RegisterMapperFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
/**
 *
 * @author Koen
 */
public class S4s extends AbstractProtocol {

    @Override
    public String getProtocolDescription() {
        return "Landis&Gyr S4S DGCOM";
    }

    private DGCOMConnection dgcomConnection;
    private CommandFactory commandFactory;
    S4sProfile s4sProfile;
    private RegisterMapperFactory registerMapperFactory;
    String modemPassword;

    @Inject
    public S4s(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    SerialCommunicationChannel commChannel;
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
    }

    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
        if (commChannel!=null) {
            commChannel.setBaudrate(9600);
            commChannel.getSerialPort().setDTR(getDtrBehaviour()==1);
        }
        else {
            getDgcomConnection().signon();
        }

        if (modemPassword!=null) {
            getCommandFactory().modemUnlock(modemPassword);
        }
    }


    protected void doDisConnect() throws IOException {
        getCommandFactory().logoff();
    }


    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return s4sProfile.getProfileData(from, to, includeEvents);
    }

    public int getProfileInterval() throws IOException {
        return getCommandFactory().getDemandIntervalCommand().getProfileInterval()* 60;
    }

   /*
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
            return;
        }
        String sn = getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());

    }

    protected void validateDeviceId() throws IOException {
        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) {
            return;
        }
        String devId = getCommandFactory().getDeviceIDExtendedCommand().getDeviceID();
        if (devId.compareTo(getInfoTypeDeviceID()) == 0) {
            return;
        }
        throw new IOException("Device ID mismatch! meter devId="+devId+", configured devId="+getInfoTypeDeviceID());
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        setInfoTypeSecurityLevel(Integer.parseInt(properties.getProperty("SecurityLevel","0").trim()));
        modemPassword = properties.getProperty("ModemPassword");
    }

    protected List<String> doGetOptionalKeys() {
        return Collections.singletonList("ModemPassword");
    }

    public int getNumberOfChannels() throws IOException {
        return getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getNrOfActiveChannels();
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setDgcomConnection(new DGCOMConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel()));
        setCommandFactory(new CommandFactory(this));
        s4sProfile = new S4sProfile(this);
        registerMapperFactory = new RegisterMapperFactory(this);
        return getDgcomConnection();
    }

    public Date getTime() throws IOException {
        return getCommandFactory().getTime();
    }

    public void setTime() throws IOException {
        getCommandFactory().setTime();
    }

    public String getProtocolVersion() {
        return "$Revision: 1.8 $";
    }

    public String getFirmwareVersion() throws IOException {
        return "ProductFamily: "+getCommandFactory().getFirmwareVersionCommand().getProductFamily()+"\nFirmware version: "+getCommandFactory().getFirmwareVersionCommand().getFirmwareVersion()+"\nDGCOM version: "+getCommandFactory().getFirmwareVersionCommand().getDgcomVersion()+"\nDSP revision: "+getCommandFactory().getSerialNumberCommand().getDspRevision();
    }

    public String getSerialNumber() throws IOException {
        return ""+getCommandFactory().getSerialNumberCommand().getSerialNumber();
    }




    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return getRegisterMapperFactory().getRegisterMapper().getRegisterInfo();
    }

    public DGCOMConnection getDgcomConnection() {
        return dgcomConnection;
    }

    private void setDgcomConnection(DGCOMConnection dgcomConnection) {
        this.dgcomConnection = dgcomConnection;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public RegisterMapperFactory getRegisterMapperFactory() {
        return registerMapperFactory;
    }

}