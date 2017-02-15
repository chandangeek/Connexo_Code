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

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.landisgyr.sentry.s200.core.CommandFactory;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;

/**
 *
 * @author Koen
 |09052007&Change to ForceStatusCommand.java|
 */

public class S200 extends AbstractProtocol {

    private S200Connection s200Connection=null;
    private CommandFactory commandFactory = null;
    private S200Profile s200Profile=null;
    private int crnInitialValue;
    private int modeOfOperation;

    public S200(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    protected void doConnect() throws IOException {
        getCommandFactory().getForceStatusCommand();
    }

    @Override
    protected void doDisconnect() throws IOException {
        getCommandFactory().hangup();
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getS200Profile().getProfileData(lastReading,includeEvents);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.integerSpec("CRNInitialValue", PropertyTranslationKeys.LANDISGYR_CRN_INITIAL_VALUE, false));
        propertySpecs.add(this.integerSpec("ModeOfOperation", PropertyTranslationKeys.LANDISGYR_MODE_OF_OPERATION, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeNodeAddress(properties.getTypedProperty(NODEID.getName(), "0000000"));
        setInfoTypePassword(properties.getTypedProperty(PASSWORD.getName(), "0000"));
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(PROP_FORCED_DELAY, "0")));
        setCrnInitialValue(Integer.parseInt(properties.getTypedProperty("CRNInitialValue", "-1")));
        setModeOfOperation(Integer.parseInt(properties.getTypedProperty("ModeOfOperation", "0"), 16));
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getCommandFactory().getLookAtCommand().getNrOfInputs();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getCommandFactory().getBeginRecordTimeCommand().getProfileInterval()*60;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "Version="+getCommandFactory().getVerifyCommand().getSoftwareVersion()+", Revision="+getCommandFactory().getRevisionLevelCommand().getRev();
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {

        s200Connection = new S200Connection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getCrnInitialValue());
        commandFactory = new CommandFactory(this);
        setS200Profile(new S200Profile(this));

        return s200Connection;
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int channelNr=0;channelNr<getNumberOfChannels();channelNr++) {
            ObisCode obisCode = ObisCode.fromString("1."+channelNr+".82.8.0.255");
            builder.append(obisCode).append(", ").append(ObisCodeMapper.getRegisterInfo(obisCode)).append("\n");
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
    public Date getTime() throws IOException {
        return getCommandFactory().getQueryTimeCommand().getTime();
    }

    @Override
    public void setTime() throws IOException {
        getCommandFactory().getEnterTimeCommand();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
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