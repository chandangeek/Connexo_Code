/*
 * Quantum1000.java
 *
 * Created on 20 november 2006, 16:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000;

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
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ApplicationStateMachine;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.DataDefinitionFactory;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.MiniDLMSConnection;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ProtocolLink;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.RegisterMapFactory;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.RemoteProcedureCallFactory;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ReplyException;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;

/**
 *
 * @author Koen
 */
public class Quantum1000 extends AbstractProtocol implements ProtocolLink, SerialNumberSupport {

    public static final String PROPERTY_APPLY_DEMAND_REGISTER_MULTIPLIER = "ApplyDemandRegisterMultiplier";
    public static final String PROPERTY_APPLY_ENERGY_REGISTER_MULTIPLIER = "ApplyEnergyRegisterMultiplier";
    public static final String PROPERTY_APPLY_SELF_READ_REGISTER_MULTIPLIER = "ApplySelfReadRegisterMultiplier";
    public static final String PROPERTY_SUPPORTS_IDENTIFY_COMMAND = "SupportsIdentifyCommand";

    private MiniDLMSConnection miniDLMSConnection=null;
    private ApplicationStateMachine applicationStateMachine=null;
    private int clientAddress;
    private DataDefinitionFactory dataDefinitionFactory=null;
    private RemoteProcedureCallFactory remoteProcedureCallFactory=null;

    private Quantum1000Profile quantum1000Profile=null;

    private int massMemoryId;

    private RegisterMapFactory registerMapFactory=null;

    private boolean applyDemandRegisterMultiplier = true;
    private boolean applyEnergyRegisterMultiplier = true;
    private boolean applySelfReadRegisterMultiplier = true;
    private boolean supportsIdentifyCommand = true;

    public Quantum1000(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getQuantum1000Profile().getProfileData(lastReading,includeEvents);
    }

    @Override
    protected void doConnect() throws IOException {
        String identify = getApplicationStateMachine().identify();
        getLogger().info("identify="+identify.trim());
        logon();
    }

    private void logon() throws IOException {
        if (getInfoTypePassword()!=null) {
            try {
                String initiate = getApplicationStateMachine().initiate().toString();
                getDataDefinitionFactory().setDLMLSecurity(getInfoTypePassword());
                getLogger().info("initiate="+initiate);
                return;
            }
            catch(ReplyException e) {
                getLogger().info("try with password, "+e.toString());
            }
        }

        String initiate = getApplicationStateMachine().initiate().toString();
        getDataDefinitionFactory().setDLMLSecurity(null);
        getLogger().info("initiate="+initiate);
    }

    @Override
    protected void doDisconnect() throws IOException {
        getRemoteProcedureCallFactory().endSession();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec(PROPERTY_APPLY_DEMAND_REGISTER_MULTIPLIER, PropertyTranslationKeys.ITRON_APPLY_DEMAND_REGISTER_MULTIPLIER, false));
        propertySpecs.add(this.stringSpec(PROPERTY_APPLY_ENERGY_REGISTER_MULTIPLIER, PropertyTranslationKeys.ITRON_APPLY_ENERGY_REGISTER_MULTIPLIER, false));
        propertySpecs.add(this.stringSpec(PROPERTY_APPLY_SELF_READ_REGISTER_MULTIPLIER, PropertyTranslationKeys.ITRON_APPLY_SELF_READ_REGISTER_MULTIPLIER, false));
        propertySpecs.add(this.stringSpec(PROPERTY_SUPPORTS_IDENTIFY_COMMAND, PropertyTranslationKeys.ITRON_SUPPORTS_IDENTIFY_COMMAND, false));
        propertySpecs.add(this.integerSpec("ClientAddress", PropertyTranslationKeys.ITRON_CLIENT_ADDRESS, false));
        propertySpecs.add(this.integerSpec("MassMemoryId", PropertyTranslationKeys.ITRON_MASS_MEMORY_ID, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        clientAddress = Integer.parseInt(properties.getTypedProperty("ClientAddress", "254"));
        setInfoTypeNodeAddress(properties.getTypedProperty(NODEID.getName(), "01"));
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(PROP_FORCED_DELAY, "0").trim()));
        setMassMemoryId(Integer.parseInt(properties.getTypedProperty("MassMemoryId", "0").trim()));
        if (getMassMemoryId()>1) {
            setMassMemoryId(1);
        }
        setApplyDemandRegisterMultiplier(Boolean.parseBoolean(properties.getTypedProperty(PROPERTY_APPLY_DEMAND_REGISTER_MULTIPLIER, "true")));
        setApplyEnergyRegisterMultiplier(Boolean.parseBoolean(properties.getTypedProperty(PROPERTY_APPLY_ENERGY_REGISTER_MULTIPLIER, "true")));
        setApplySelfReadRegisterMultiplier(Boolean.parseBoolean(properties.getTypedProperty(PROPERTY_APPLY_SELF_READ_REGISTER_MULTIPLIER, "true")));
        setSupportsIdentifyCommand(Boolean.parseBoolean(properties.getTypedProperty(PROPERTY_SUPPORTS_IDENTIFY_COMMAND, "true")));
    }

    public boolean isApplySelfReadRegisterMultiplier() {
        return applySelfReadRegisterMultiplier;
    }

    public void setApplySelfReadRegisterMultiplier(boolean applySelfReadRegisterMultiplier) {
        this.applySelfReadRegisterMultiplier = applySelfReadRegisterMultiplier;
    }

    public boolean isApplyDemandRegisterMultiplier() {
        return applyDemandRegisterMultiplier;
    }

    public void setApplyDemandRegisterMultiplier(boolean applyDemandRegisterMultiplier) {
        this.applyDemandRegisterMultiplier = applyDemandRegisterMultiplier;
    }

    public boolean isApplyEnergyRegisterMultiplier() {
        return applyEnergyRegisterMultiplier;
    }

    public void setApplyEnergyRegisterMultiplier(boolean applyEnergyRegisterMultiplier) {
        this.applyEnergyRegisterMultiplier = applyEnergyRegisterMultiplier;
    }

    public boolean isSupportsIdentifyCommand() {
        return supportsIdentifyCommand;
    }

    public void setSupportsIdentifyCommand(boolean supportsIdentifyCommand) {
        this.supportsIdentifyCommand = supportsIdentifyCommand;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getDataDefinitionFactory().getMassMemoryConfiguration(getMassMemoryId()).getMassMemoryConfigType().getIntervalLength();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getDataDefinitionFactory().getMassMemoryConfiguration(getMassMemoryId()).getMassMemoryConfigType().getNumberOfChannels();
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setMiniDLMSConnection(new MiniDLMSConnection(inputStream, outputStream, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, timeoutProperty, getInfoTypeSecurityLevel(), clientAddress));
        setApplicationStateMachine(new ApplicationStateMachine(this, isSupportsIdentifyCommand()));
        setDataDefinitionFactory(new DataDefinitionFactory(this));
        setRemoteProcedureCallFactory(new RemoteProcedureCallFactory(this));
        setQuantum1000Profile(new Quantum1000Profile(this));
        return getMiniDLMSConnection();
    }

    @Override
    public Date getTime() throws IOException {
        return getDataDefinitionFactory().getRealTime().getCurrentDateTime();
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDataDefinitionFactory().getMeterIDS().getFullSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getMiniDLMSConnection().getMaxRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-07-11 09:48:56 +0300 (Mon, 11 Jul 2016)$";
    }

    @Override
    public void setTime() throws IOException {
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "Firmware revision: "+getDataDefinitionFactory().getGeneralDiagnosticInfo().getFirmwareRevision();
    }

    @Override
    public AbstractProtocol getProtocol() {
        return this;
    }

    public int getClientAddress() {
        return clientAddress;
    }

    private void setClientAddress(int clientAddress) {
        this.clientAddress = clientAddress;
    }

    @Override
    public MiniDLMSConnection getMiniDLMSConnection() {
        return miniDLMSConnection;
    }

    private void setMiniDLMSConnection(MiniDLMSConnection miniDLMSConnection) {
        this.miniDLMSConnection = miniDLMSConnection;
    }

    @Override
    public ApplicationStateMachine getApplicationStateMachine() {
        return applicationStateMachine;
    }

    public void setApplicationStateMachine(ApplicationStateMachine applicationStateMachine) {
        this.applicationStateMachine = applicationStateMachine;
    }

    @Override
    public DataDefinitionFactory getDataDefinitionFactory() {
        return dataDefinitionFactory;
    }

    private void setDataDefinitionFactory(DataDefinitionFactory dataDefinitionFactory) {
        this.dataDefinitionFactory = dataDefinitionFactory;
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("************* G E N E R A L  D I A G N O S T I C S  I N F O *************\n");
        builder.append(getDataDefinitionFactory().getGeneralDiagnosticInfo());
        builder.append("************* G E N E R A L  S E T U P *************\n");
        builder.append(getDataDefinitionFactory().getMeterSetup());
        builder.append(getDataDefinitionFactory().getMeterIDS());
        builder.append("************* V I E W *************\n");
        builder.append(getDataDefinitionFactory().getDefaultViewIdConfiguration());
        builder.append("************* D E M A N D *************\n");
        builder.append(getDataDefinitionFactory().getGeneralDemandConfiguration());
        builder.append(getDataDefinitionFactory().getDemandRegisterConfiguration());
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(0,0));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(0,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(1,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(1,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(2,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(2,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(3,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(4,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(5,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(6,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(7,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(8,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(9,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getDemandRegisterReadings(0,1));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getDemandRegisterReadings(0,1) unavailable\n"); else throw e;}
        builder.append("************* E N E R G Y *************\n");
        builder.append(getDataDefinitionFactory().getGeneralEnergyConfiguration());
        builder.append(getDataDefinitionFactory().getEnergyRegisterConfiguration());
        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(0));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(0) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(1));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(1) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(2));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(2) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(3));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(3) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(4));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(4) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(5));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(5) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(6));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(6) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(7));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(7) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(8));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(8) unavailable\n"); else throw e;}
//        try {builder.append(getDataDefinitionFactory().getEnergyRegistersReading(9));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEnergyRegistersReading(9) unavailable\n"); else throw e;}
        builder.append("************* S E L F  R E A D *************\n");
        builder.append(getDataDefinitionFactory().getSelfReadGeneralConfiguration());
        builder.append(getDataDefinitionFactory().getSelfReadRegisterConfiguration());
        builder.append(getDataDefinitionFactory().getSelfReadGeneralInformation());

        try {
            builder.append(getDataDefinitionFactory().getSelfReadDataUpload());
        } catch (ReplyException e) {
            if (e.getAbstractReplyDataError().isInvalidObject()) {
                builder.append("getSelfReadDataUpload() unavailable\n");
            } else {
                throw e;
            }
        }

        builder.append("************* M U L T I  T A R I F F *************\n");
        if (getDataDefinitionFactory().isTOUMeter()) {
            builder.append(getDataDefinitionFactory().getMultiTariffScheduleGeneralParameters());
            builder.append(getDataDefinitionFactory().getMultiTariffRateScheduleGeneralParameters());
        }
        else {
            builder.append("No MULTI TARIFF Meter!\n");
        }
        builder.append("************* M A S S  M E M O R Y *************\n");
        try {builder.append(getDataDefinitionFactory().getMassMemoryConfiguration(getMassMemoryId()));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getMassMemoryConfiguration("+getMassMemoryId()+") unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getMassMemoryInformation(getMassMemoryId()));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getMassMemoryInformation("+getMassMemoryId()+") unavailable\n"); else throw e;}
        builder.append("************* E V E N T  L O G *************\n");
        try {builder.append(getDataDefinitionFactory().getEventLogSummary());} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEventLogSummary() unavailable\n"); else throw e;}
        try {builder.append(getDataDefinitionFactory().getEventLogUpload());} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) builder.append("getEventLogSummary() unavailable\n"); else throw e;}
        builder.append("************* R E G I S T E R S  M A P P I N G *************\n");
        builder.append(getRegisterMapFactory().getRegisterInfo());

        return builder.toString();
    }

    public RemoteProcedureCallFactory getRemoteProcedureCallFactory() {
        return remoteProcedureCallFactory;
    }

    private void setRemoteProcedureCallFactory(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        this.remoteProcedureCallFactory = remoteProcedureCallFactory;
    }

    private Quantum1000Profile getQuantum1000Profile() {
        return quantum1000Profile;
    }

    private void setQuantum1000Profile(Quantum1000Profile quantum1000Profile) {
        this.quantum1000Profile = quantum1000Profile;
    }

    public int getMassMemoryId() {
        return massMemoryId;
    }

    public void setMassMemoryId(int massMemoryId) {
        this.massMemoryId = massMemoryId;
    }

    public RegisterMapFactory getRegisterMapFactory() throws IOException {
        if (registerMapFactory==null) {
            registerMapFactory = new RegisterMapFactory(this);
            registerMapFactory.init();
        }
        return registerMapFactory;
    }

}