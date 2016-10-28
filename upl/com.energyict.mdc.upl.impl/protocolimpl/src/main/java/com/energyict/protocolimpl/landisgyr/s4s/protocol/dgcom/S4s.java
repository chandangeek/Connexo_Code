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

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command.CommandFactory;
import com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.registermappping.RegisterMapperFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Koen
 */
public class S4s extends AbstractProtocol implements SerialNumberSupport{

    private DGCOMConnection dgcomConnection;
    private CommandFactory commandFactory;
    S4sProfile s4sProfile;
    private RegisterMapperFactory registerMapperFactory;
    String modemPassword;

    /** Creates a new instance of S4s */
    public S4s() {
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
        else getDgcomConnection().signon();

        if (modemPassword!=null)
            getCommandFactory().modemUnlock(modemPassword);


    }


    protected void doDisConnect() throws IOException {
        getCommandFactory().logoff();
    }


    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return s4sProfile.getProfileData(from, to, includeEvents);
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return getCommandFactory().getDemandIntervalCommand().getProfileInterval()*60;
    }

    protected void validateDeviceId() throws IOException {
        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) return;
        String devId = getCommandFactory().getDeviceIDExtendedCommand().getDeviceID();
        if (devId.compareTo(getInfoTypeDeviceID()) == 0) return;
        throw new IOException("Device ID mismatch! meter devId="+devId+", configured devId="+getInfoTypeDeviceID());
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        setInfoTypeSecurityLevel(Integer.parseInt(properties.getProperty("SecurityLevel","0").trim()));
        modemPassword = properties.getProperty("ModemPassword");
    }

    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("ModemPassword");
        return result;
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
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
        return "$Date: 2015-11-26 15:23:42 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "ProductFamily: "+getCommandFactory().getFirmwareVersionCommand().getProductFamily()+"\nFirmware version: "+getCommandFactory().getFirmwareVersionCommand().getFirmwareVersion()+"\nDGCOM version: "+getCommandFactory().getFirmwareVersionCommand().getDgcomVersion()+"\nDSP revision: "+getCommandFactory().getSerialNumberCommand().getDspRevision();
    }

    public String getSerialNumber() {
        try {
            return String.valueOf(getCommandFactory().getSerialNumberCommand().getSerialNumber());
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
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
        StringBuffer strbuff = new StringBuffer();
//        strbuff.append(getCommandFactory().getLoadProfileMetricSelectionRXCommand());
//        strbuff.append(getCommandFactory().getThirdMetricValuesCommand());
//        strbuff.append(getCommandFactory().getMeasurementUnitsCommand());
        strbuff.append(getRegisterMapperFactory().getRegisterMapper().getRegisterInfo());
        return strbuff.toString();
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
