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

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.transdata.markv.core.MarkVProfile;
import com.energyict.protocolimpl.transdata.markv.core.commands.CommandFactory;
import com.energyict.protocolimpl.transdata.markv.core.commands.ObisCodeMapper;
import com.energyict.protocolimpl.transdata.markv.core.commands.RegisterDataId;
import com.energyict.protocolimpl.transdata.markv.core.commands.RegisterIdentification;
import com.energyict.protocolimpl.transdata.markv.core.connection.MarkVConnection;
import com.energyict.protocols.util.ProtocolUtils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

/**
 *
 * @author koen
 */
public class MarkV extends AbstractProtocol {

    @Override
    public String getProtocolDescription() {
        return "Transdata MarkV";
    }

    MarkVConnection markVConnection=null;
    MarkVProfile markVProfile=null;
    CommandFactory commandFactory=null;
    int verifyTimeDelay;

    @Inject
    public MarkV(PropertySpecService propertySpecService) {
        super(propertySpecService);
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

    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder strBuff = new StringBuilder();

        strBuff.append(getCommandFactory().getISCommand()).append("\n");
        strBuff.append(getCommandFactory().getMICommand()).append("\n");

        Iterator<RegisterDataId> it = RegisterIdentification.getRegisterDataIds().iterator();
        while(it.hasNext()) {
            RegisterDataId rdi = it.next();
            strBuff.append(rdi).append("\n");
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
        String sn = getCommandFactory().getMICommand().getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());

    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return markVProfile.getProfileData(lastReading,includeEvents);
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        verifyTimeDelay=Integer.parseInt(properties.getProperty("VerifyTimeDelay","2000").trim());
    }

    protected List<String> doGetOptionalKeys() {
        return Collections.singletonList("VerifyTimeDelay");
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public int getProfileInterval() throws IOException {
        return getCommandFactory().getISCommand().getProfileInterval();
    }

    public int getNumberOfChannels() throws IOException {
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
            if (!verifySetTime(new Date(),getTime())) {
                throw new IOException("MarkV, setTime(), after 2 tries, the meter time still differs more then " + verifyTimeDelay + " ms (metertime=" + getTime() + ", systemtime=" + new Date() + ")");
            }
        }
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("SecurityLevel", "0");
        if ((discoverInfo.getNodeId()!= null) && ("".compareTo(discoverInfo.getNodeId()) != 0)) {
            properties.setProperty(MeterProtocol.NODEID, discoverInfo.getNodeId());
        }
        setProperties(properties);
        init(discoverInfo.getCommChannel().getInputStream(),discoverInfo.getCommChannel().getOutputStream(),null,null);
        connect();
        BufferedReader br = new BufferedReader(new StringReader(new String(markVConnection.receiveWithTimeout("ID"))));
        br.readLine(); // skip ID code 1
        br.readLine(); // skip ID code 2
        return br.readLine().trim();
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
            if (getCommandFactory().getISCommand().isDstEnabled()) {
                return super.getTimeZone();
            }
            else {
                return ProtocolUtils.getWinterTimeZone(super.getTimeZone());
            }
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
            if (!verifySetTime(nd,getCommandFactory().getGCCommand().getDate())) {
                throw new IOException("MarkV, setDialinScheduleTime(), after 2 tries, the meter time still differs more then " + verifyTimeDelay + " ms (meter nextDialin=" + getCommandFactory().getGCCommand()
                        .getDate() + ", system nextDialin=" + nd + ")");
            }
        }
    }


    /*
     * Because we are working in a terminal mode, we need to verify the time. verifyTimeDelay is a custom property
     * we need to check the returned time against...
     */
    private boolean verifySetTime(Date src, Date dst) {
        //Date system = new Date();
        //Date meter = getTime();
        return Math.abs(src.getTime() - dst.getTime()) <= verifyTimeDelay;
    }
}
