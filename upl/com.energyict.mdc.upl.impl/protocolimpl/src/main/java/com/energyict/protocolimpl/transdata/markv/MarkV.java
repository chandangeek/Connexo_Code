/*
 * MarkV.java
 *
 * Created on 8 augustus 2005, 10:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.transdata.markv.core.MarkVProfile;
import com.energyict.protocolimpl.transdata.markv.core.commands.CommandFactory;
import com.energyict.protocolimpl.transdata.markv.core.commands.ObisCodeMapper;
import com.energyict.protocolimpl.transdata.markv.core.commands.RegisterDataId;
import com.energyict.protocolimpl.transdata.markv.core.commands.RegisterIdentification;
import com.energyict.protocolimpl.transdata.markv.core.connection.MarkVConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

//import com.energyict.protocolimpl.transdata.markv.core.*;
/**
 *
 * @author koen
 */
public class MarkV extends AbstractProtocol implements SerialNumberSupport {

    MarkVConnection markVConnection=null;
    MarkVProfile markVProfile=null;
    CommandFactory commandFactory=null;
    int verifyTimeDelay;

    /** Creates a new instance of MarkV */
    public MarkV() {
    }

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    SerialCommunicationChannel commChannel;
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
        markVConnection.setSerialCommunicationChannel(commChannel);
    }

    protected void doConnect() throws IOException {
    }


    protected void doDisConnect() throws IOException {
        getCommandFactory().issueLOCommand();
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();

        strBuff.append(getCommandFactory().getISCommand()+"\n");
        strBuff.append(getCommandFactory().getMICommand()+"\n");

        Iterator it = RegisterIdentification.getRegisterDataIds().iterator();
        while(it.hasNext()) {
            RegisterDataId rdi = (RegisterDataId)it.next();
            strBuff.append(rdi+"\n");
        }
        return strBuff.toString();

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

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return markVProfile.getProfileData(lastReading,includeEvents);
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        verifyTimeDelay=Integer.parseInt(properties.getProperty("VerifyTimeDelay","2000").trim());
    }

    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("VerifyTimeDelay");
        return result;
    }

    @Override
    public String getSerialNumber() {
        try {
            return getCommandFactory().getMICommand().getSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:47 +0200 (Thu, 26 Nov 2015)$";
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return getCommandFactory().getISCommand().getProfileInterval();
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getCommandFactory().getDCCommand().getProtocolChannelMap().getNrOfProtocolChannels();
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeout,int maxRetries,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        markVConnection = new MarkVConnection(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController);
        markVConnection.setDtrBehaviour(getDtrBehaviour());
        commandFactory = new CommandFactory(this);
        markVProfile = new MarkVProfile(this);
        //classFactory = new ClassFactory(this);
        //alphaPlusProfile = new AlphaPlusProfile(this);
        return markVConnection;
    }


    public Date getTime() throws IOException {
        return getCommandFactory().getGTCommand().getDate();
    }

    public void setTime() throws IOException {
        getCommandFactory().issueTICommand();
        if (!verifySetTime(new Date(),getTime())) {
            getCommandFactory().issueTICommand();
            if (!verifySetTime(new Date(),getTime()))
                throw new IOException("MarkV, setTime(), after 2 tries, the meter time still differs more then "+verifyTimeDelay+" ms (metertime="+getTime()+", systemtime="+new Date()+")");
        }


    }


    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("SecurityLevel", "0");
        if ((discoverInfo.getNodeId()!= null) && ("".compareTo(discoverInfo.getNodeId()) != 0))
            properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(),discoverInfo.getNodeId());
        setProperties(properties);
        init(discoverInfo.getCommChannel().getInputStream(),discoverInfo.getCommChannel().getOutputStream(),null,null);
        connect();
        BufferedReader br = new BufferedReader(new StringReader(new String(markVConnection.receiveWithTimeout("ID"))));
        br.readLine(); // skip ID code 1
        br.readLine(); // skip ID code 2
        String serialNumber = br.readLine().trim();
        //String serialNumber =  getCommandFactory().getIDCommand().getSerialNr();
        //disconnect(); // KV 13102005 LO command does not react here...
        return serialNumber;
    }

    public MarkVConnection getMarkVConnection() {
        return markVConnection;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    /*
     * Getter for the configured device TimeZone.
     * @return Value of the device TimeZone.
     */
    public TimeZone getTimeZone() {
        try {
            if (getCommandFactory().getISCommand().isDstEnabled())
                return super.getTimeZone();
            else
                return ProtocolUtils.getWinterTimeZone(super.getTimeZone());
        }
        catch (IOException e) {
            getLogger().severe("getTimeZone(), Error requesting IS command!, use configured timezone, "+e.toString());
            return super.getTimeZone();
        }
    }

    public void setDialinScheduleTime(Date nextDialin) throws IOException {
        Date nd = new Date(nextDialin.getTime() - nextDialin.getTime()%60000);
        getCommandFactory().issueTCCommand(nd);
        if (!verifySetTime(nd,getCommandFactory().getGCCommand().getDate())) {
            getCommandFactory().issueTCCommand(nd);
            if (!verifySetTime(nd,getCommandFactory().getGCCommand().getDate()))
                throw new IOException("MarkV, setDialinScheduleTime(), after 2 tries, the meter time still differs more then "+verifyTimeDelay+" ms (meter nextDialin="+getCommandFactory().getGCCommand().getDate()+", system nextDialin="+nd+")");
        }
    }


    /*
     * Because we are working in a terminal mode, we need to verify the time. verifyTimeDelay is a custom property
     * we need to check the returned time against...
     */
    private boolean verifySetTime(Date src, Date dst) throws IOException {
        //Date system = new Date();
        //Date meter = getTime();
        if (Math.abs(src.getTime() - dst.getTime()) > verifyTimeDelay)
           return false;
        else
           return true;
    }
}
