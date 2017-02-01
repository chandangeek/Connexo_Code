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

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
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
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

//import com.energyict.protocolimpl.transdata.markv.core.*;
/**
 *
 * @author koen
 */
public class MarkV extends AbstractProtocol implements SerialNumberSupport {

    private MarkVConnection markVConnection = null;
    private MarkVProfile markVProfile = null;
    private CommandFactory commandFactory = null;
    private SerialCommunicationChannel commChannel;
    private int verifyTimeDelay;

    public MarkV(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
        markVConnection.setSerialCommunicationChannel(commChannel);
    }

    @Override
    protected void doConnect() throws IOException {
    }

    @Override
    protected void doDisconnect() throws IOException {
        getCommandFactory().issueLOCommand();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append(getCommandFactory().getISCommand()).append("\n");
        builder.append(getCommandFactory().getMICommand()).append("\n");

        for (Object o : RegisterIdentification.getRegisterDataIds()) {
            RegisterDataId rdi = (RegisterDataId) o;
            builder.append(rdi).append("\n");
        }
        return builder.toString();
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return markVProfile.getProfileData(lastReading,includeEvents);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("VerifyTimeDelay", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        verifyTimeDelay=Integer.parseInt(properties.getTypedProperty("VerifyTimeDelay", "2000").trim());
    }

    @Override
    public String getSerialNumber() {
        try {
            return getCommandFactory().getMICommand().getSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getCommandFactory().getISCommand().getProfileInterval();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getCommandFactory().getDCCommand().getProtocolChannelMap().getNrOfProtocolChannels();
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeout,int maxRetries,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        markVConnection = new MarkVConnection(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController);
        markVConnection.setDtrBehaviour(getDtrBehaviour());
        commandFactory = new CommandFactory(this);
        markVProfile = new MarkVProfile(this);
        //classFactory = new ClassFactory(this);
        //alphaPlusProfile = new AlphaPlusProfile(this);
        return markVConnection;
    }

    @Override
    public Date getTime() throws IOException {
        return getCommandFactory().getGTCommand().getDate();
    }

    @Override
    public void setTime() throws IOException {
        getCommandFactory().issueTICommand();
        if (!verifySetTime(new Date(),getTime())) {
            getCommandFactory().issueTICommand();
            if (!verifySetTime(new Date(),getTime())) {
                throw new IOException("MarkV, setTime(), after 2 tries, the meter time still differs more then " + verifyTimeDelay + " ms (metertime=" + getTime() + ", systemtime=" + new Date() + ")");
            }
        }
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        TypedProperties properties = com.energyict.cpo.TypedProperties.empty();
        properties.setProperty("SecurityLevel", "0");
        if ((discoverInfo.getNodeId()!= null) && ("".compareTo(discoverInfo.getNodeId()) != 0)) {
            properties.setProperty(Property.NODEID.getName(), discoverInfo.getNodeId());
        }
        setUPLProperties(properties);
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

    @Override
    public TimeZone getTimeZone() {
        try {
            if (getCommandFactory().getISCommand().isDstEnabled()) {
                return super.getTimeZone();
            } else {
                return ProtocolUtils.getWinterTimeZone(super.getTimeZone());
            }
        }
        catch (IOException e) {
            getLogger().severe("getTimeZone(), Error requesting IS command!, use configured timezone, "+e.toString());
            return super.getTimeZone();
        }
    }

    //This used to implement the DialinScheduleProtocol interface
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

    @Override
    public int getInfoTypeRoundtripCorrection() {
        return super.getInfoTypeRoundtripCorrection();
    }

}