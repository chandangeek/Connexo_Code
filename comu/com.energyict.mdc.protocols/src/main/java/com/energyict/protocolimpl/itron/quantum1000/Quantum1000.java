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

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ApplicationStateMachine;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.DataDefinitionFactory;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.MiniDLMSConnection;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ProtocolLink;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.RegisterMapFactory;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.RemoteProcedureCallFactory;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ReplyException;

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
 */
public class Quantum1000 extends AbstractProtocol implements ProtocolLink {

    @Override
    public String getProtocolDescription() {
        return "Itron/Schlumberger Quantum1000";
    }

    private MiniDLMSConnection miniDLMSConnection=null;
    private ApplicationStateMachine applicationStateMachine=null;
    private int clientAddress;
    private DataDefinitionFactory dataDefinitionFactory=null;
    private RemoteProcedureCallFactory remoteProcedureCallFactory=null;

    private Quantum1000Profile quantum1000Profile=null;

    private int massMemoryId;

    private RegisterMapFactory registerMapFactory=null;

    @Inject
    public Quantum1000(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getQuantum1000Profile().getProfileData(lastReading,includeEvents);
    }

    protected void doConnect() throws IOException {
        String identify = getApplicationStateMachine().identify();
        getLogger().info("identify="+identify.trim());
        logon();
    }

    protected void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = getDataDefinitionFactory().getMeterIDS().getFullSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
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



    protected void doDisConnect() throws IOException {
        getRemoteProcedureCallFactory().endSession();
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        clientAddress = Integer.parseInt(properties.getProperty("ClientAddress", "254"));
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID, "01"));
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        setMassMemoryId(Integer.parseInt(properties.getProperty("MassMemoryId","0").trim()));
        if (getMassMemoryId()>1) setMassMemoryId(1);

    }

    public int getProfileInterval() throws IOException {
        return getDataDefinitionFactory().getMassMemoryConfiguration(getMassMemoryId()).getMassMemoryConfigType().getIntervalLength();
    }

    public int getNumberOfChannels() throws IOException {
        return getDataDefinitionFactory().getMassMemoryConfiguration(getMassMemoryId()).getMassMemoryConfigType().getNumberOfChannels();
    }

    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        list.add("MassMemoryId");
        return list;
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setMiniDLMSConnection(new MiniDLMSConnection(inputStream, outputStream, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, timeoutProperty, getInfoTypeSecurityLevel(), clientAddress));
        setApplicationStateMachine(new ApplicationStateMachine(this));
        setDataDefinitionFactory(new DataDefinitionFactory(this));
        setRemoteProcedureCallFactory(new RemoteProcedureCallFactory(this));
        setQuantum1000Profile(new Quantum1000Profile(this));
        return getMiniDLMSConnection();
    }

    public Date getTime() throws IOException {
        return getDataDefinitionFactory().getRealTime().getCurrentDateTime();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public void setTime() throws IOException {

    }

    public String getFirmwareVersion() throws IOException {
        return "Firmware revision: "+getDataDefinitionFactory().getGeneralDiagnosticInfo().getFirmwareRevision();
    }

    public AbstractProtocol getProtocol() {
        return this;
    }

    public int getClientAddress() {
        return clientAddress;
    }

    private void setClientAddress(int clientAddress) {
        this.clientAddress = clientAddress;
    }



    public MiniDLMSConnection getMiniDLMSConnection() {
        return miniDLMSConnection;
    }

    private void setMiniDLMSConnection(MiniDLMSConnection miniDLMSConnection) {
        this.miniDLMSConnection = miniDLMSConnection;
    }

    public ApplicationStateMachine getApplicationStateMachine() {
        return applicationStateMachine;
    }

    public void setApplicationStateMachine(ApplicationStateMachine applicationStateMachine) {
        this.applicationStateMachine = applicationStateMachine;
    }

    public DataDefinitionFactory getDataDefinitionFactory() {
        return dataDefinitionFactory;
    }

    private void setDataDefinitionFactory(DataDefinitionFactory dataDefinitionFactory) {
        this.dataDefinitionFactory = dataDefinitionFactory;
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("************* G E N E R A L  D I A G N O S T I C S  I N F O *************\n");
        strBuff.append(getDataDefinitionFactory().getGeneralDiagnosticInfo());
        strBuff.append("************* G E N E R A L  S E T U P *************\n");
        strBuff.append(getDataDefinitionFactory().getMeterSetup());
        strBuff.append(getDataDefinitionFactory().getMeterIDS());
        strBuff.append("************* V I E W *************\n");
        strBuff.append(getDataDefinitionFactory().getDefaultViewIdConfiguration());
        strBuff.append("************* D E M A N D *************\n");
        strBuff.append(getDataDefinitionFactory().getGeneralDemandConfiguration());
        strBuff.append(getDataDefinitionFactory().getDemandRegisterConfiguration());
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(0,0));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(0,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(1,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(1,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(2,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(2,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(3,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(4,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(5,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(6,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(7,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(8,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(9,0));} catch(ReplyException e) { if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(3,0) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getDemandRegisterReadings(0,1));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getDemandRegisterReadings(0,1) unavailable\n"); else throw e;}
        strBuff.append("************* E N E R G Y *************\n");
        strBuff.append(getDataDefinitionFactory().getGeneralEnergyConfiguration());
        strBuff.append(getDataDefinitionFactory().getEnergyRegisterConfiguration());
        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(0));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(0) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(1));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(1) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(2));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(2) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(3));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(3) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(4));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(4) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(5));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(5) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(6));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(6) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(7));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(7) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(8));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(8) unavailable\n"); else throw e;}
//        try {strBuff.append(getDataDefinitionFactory().getEnergyRegistersReading(9));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEnergyRegistersReading(9) unavailable\n"); else throw e;}
        strBuff.append("************* S E L F  R E A D *************\n");
        strBuff.append(getDataDefinitionFactory().getSelfReadGeneralConfiguration());
        strBuff.append(getDataDefinitionFactory().getSelfReadRegisterConfiguration());
        strBuff.append(getDataDefinitionFactory().getSelfReadGeneralInformation());
        try {strBuff.append(getDataDefinitionFactory().getSelfReadDataUpload());} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getSelfReadDataUpload() unavailable\n"); else throw e;}

        strBuff.append("************* M U L T I  T A R I F F *************\n");
        if (getDataDefinitionFactory().isTOUMeter()) {
            strBuff.append(getDataDefinitionFactory().getMultiTariffScheduleGeneralParameters());
            strBuff.append(getDataDefinitionFactory().getMultiTariffRateScheduleGeneralParameters());
        }
        else strBuff.append("No MULTI TARIFF Meter!\n");
        strBuff.append("************* M A S S  M E M O R Y *************\n");
        try {strBuff.append(getDataDefinitionFactory().getMassMemoryConfiguration(getMassMemoryId()));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getMassMemoryConfiguration("+getMassMemoryId()+") unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getMassMemoryInformation(getMassMemoryId()));} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getMassMemoryInformation("+getMassMemoryId()+") unavailable\n"); else throw e;}
        strBuff.append("************* E V E N T  L O G *************\n");
        try {strBuff.append(getDataDefinitionFactory().getEventLogSummary());} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEventLogSummary() unavailable\n"); else throw e;}
        try {strBuff.append(getDataDefinitionFactory().getEventLogUpload());} catch(ReplyException e) {if (e.getAbstractReplyDataError().isInvalidObject()) strBuff.append("getEventLogSummary() unavailable\n"); else throw e;}
        strBuff.append("************* R E G I S T E R S  M A P P I N G *************\n");
        strBuff.append(getRegisterMapFactory().getRegisterInfo());

        return strBuff.toString();
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