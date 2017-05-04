/*
 * S200.java
 *
 * Created on 18 juli 2006, 13:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.landisgyr.sentry.s200.core.CommandFactory;

import javax.inject.Inject;
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
 |09052007&Change to ForceStatusCommand.java|
 */

public class S200 extends AbstractProtocol {

    @Override
    public String getProtocolDescription() {
        return "Landis&Gyr Sentry S200";
    }

    private S200Connection s200Connection=null;
    private CommandFactory commandFactory = null;
    private S200Profile s200Profile=null;
    private int crnInitialValue;
    private int modeOfOperation;

    @Inject
    public S200(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doConnect() throws IOException {
        getCommandFactory().getForceStatusCommand();
    }

    protected void doDisConnect() throws IOException {
        getCommandFactory().hangup();
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getS200Profile().getProfileData(lastReading,includeEvents);
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"0000000"));
        setInfoTypePassword(properties.getProperty(MeterProtocol.PASSWORD,"0000"));
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0")));
        setCrnInitialValue(Integer.parseInt(properties.getProperty("CRNInitialValue","-1")));
        setModeOfOperation(Integer.parseInt(properties.getProperty("ModeOfOperation","0"),16));
    }

    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        list.add("CRNInitialValue");
        list.add("ModeOfOperation");
        return list;
    }

    public int getNumberOfChannels() throws IOException {
        return getCommandFactory().getLookAtCommand().getNrOfInputs();
    }

    public int getProfileInterval() throws IOException {
        return getCommandFactory().getBeginRecordTimeCommand().getProfileInterval()*60;
    }

    public String getFirmwareVersion() throws IOException {
        return "Version="+getCommandFactory().getVerifyCommand().getSoftwareVersion()+", Revision="+getCommandFactory().getRevisionLevelCommand().getRev();
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {

        s200Connection = new S200Connection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getCrnInitialValue());
        commandFactory = new CommandFactory(this);
        setS200Profile(new S200Profile(this));

        return s200Connection;
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();

        for (int channelNr=0;channelNr<getNumberOfChannels();channelNr++) {
            ObisCode obisCode = ObisCode.fromString("1."+channelNr+".82.8.0.255");
            strBuff.append(obisCode+", "+ObisCodeMapper.getRegisterInfo(obisCode)+"\n");
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



    public Date getTime() throws IOException {
        return getCommandFactory().getQueryTimeCommand().getTime();
    }

    public void setTime() throws IOException {
        getCommandFactory().getEnterTimeCommand();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public S200Connection getS200Connection() {
        return s200Connection;
    }

    private void setS200Connection(S200Connection s200Connection) {
        this.s200Connection = s200Connection;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public S200Profile getS200Profile() {
        return s200Profile;
    }

    private void setS200Profile(S200Profile s200Profile) {
        this.s200Profile = s200Profile;
    }

    public int getCrnInitialValue() {
        return crnInitialValue;
    }

    public void setCrnInitialValue(int crnInitialValue) {
        this.crnInitialValue = crnInitialValue;
    }

    public int getModeOfOperation() {
        return modeOfOperation;
    }

    public void setModeOfOperation(int modeOfOperation) {
        this.modeOfOperation = modeOfOperation;
    }

}