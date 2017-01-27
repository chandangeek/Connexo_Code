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

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 */
public class Quantum1000 extends AbstractProtocol implements ProtocolLink, SerialNumberSupport {

    public final static String PROPERTY_APPLY_DEMAND_REGISTER_MULTIPLIER = "ApplyDemandRegisterMultiplier";
    public final static String PROPERTY_APPLY_ENERGY_REGISTER_MULTIPLIER = "ApplyEnergyRegisterMultiplier";
    public final static String PROPERTY_APPLY_SELF_READ_REGISTER_MULTIPLIER = "ApplySelfReadRegisterMultiplier";
    public final static String PROPERTY_SUPPORTS_IDENTIFY_COMMAND = "SupportsIdentifyCommand";

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

    /** Creates a new instance of Quantum1000 */
    public Quantum1000() {

    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getQuantum1000Profile().getProfileData(lastReading,includeEvents);
    }

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

    protected void doDisConnect() throws IOException {
        getRemoteProcedureCallFactory().endSession();
    }

    public List<String> getOptionalKeys() {
        List<String> retVal = super.getOptionalKeys();
        retVal.add(PROPERTY_APPLY_DEMAND_REGISTER_MULTIPLIER);
        retVal.add(PROPERTY_APPLY_ENERGY_REGISTER_MULTIPLIER);
        retVal.add(PROPERTY_APPLY_SELF_READ_REGISTER_MULTIPLIER);
        retVal.add(PROPERTY_SUPPORTS_IDENTIFY_COMMAND);
        return retVal;
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        clientAddress = Integer.parseInt(properties.getProperty("ClientAddress", "254"));
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID, "01"));
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        setMassMemoryId(Integer.parseInt(properties.getProperty("MassMemoryId","0").trim()));
        if (getMassMemoryId()>1) setMassMemoryId(1);
        setApplyDemandRegisterMultiplier(Boolean.parseBoolean(properties.getProperty(PROPERTY_APPLY_DEMAND_REGISTER_MULTIPLIER, "true")));
        setApplyEnergyRegisterMultiplier(Boolean.parseBoolean(properties.getProperty(PROPERTY_APPLY_ENERGY_REGISTER_MULTIPLIER, "true")));
        setApplySelfReadRegisterMultiplier(Boolean.parseBoolean(properties.getProperty(PROPERTY_APPLY_SELF_READ_REGISTER_MULTIPLIER, "true")));
        setSupportsIdentifyCommand(Boolean.parseBoolean(properties.getProperty(PROPERTY_SUPPORTS_IDENTIFY_COMMAND, "true")));
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

    public int getProfileInterval() throws UnsupportedException, IOException {
        return getDataDefinitionFactory().getMassMemoryConfiguration(getMassMemoryId()).getMassMemoryConfigType().getIntervalLength();
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {

        return getDataDefinitionFactory().getMassMemoryConfiguration(getMassMemoryId()).getMassMemoryConfigType().getNumberOfChannels();

    }

    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        list.add("MassMemoryId");
        return list;
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setMiniDLMSConnection(new MiniDLMSConnection(inputStream, outputStream, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, timeoutProperty, getInfoTypeSecurityLevel(), clientAddress));
        setApplicationStateMachine(new ApplicationStateMachine(this, isSupportsIdentifyCommand()));
        setDataDefinitionFactory(new DataDefinitionFactory(this));
        setRemoteProcedureCallFactory(new RemoteProcedureCallFactory(this));
        setQuantum1000Profile(new Quantum1000Profile(this));
        return getMiniDLMSConnection();
    }

    public Date getTime() throws IOException {

        return getDataDefinitionFactory().getRealTime().getCurrentDateTime();
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDataDefinitionFactory().getMeterIDS().getFullSerialNumber().trim();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getMiniDLMSConnection().getMaxRetries() + 1);
        }
    }

    public String getProtocolVersion() {
        return "$Date: 2016-07-11 09:48:56 +0300 (Mon, 11 Jul 2016)$";
    }

    public void setTime() throws IOException {

    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
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

        try {
            strBuff.append(getDataDefinitionFactory().getSelfReadDataUpload());
        } catch (ReplyException e) {
            if (e.getAbstractReplyDataError().isInvalidObject())
                strBuff.append("getSelfReadDataUpload() unavailable\n");
            else throw e;
        }

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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Quantum1000 quantum = new Quantum1000();
        Dialer dialer=null;
        try {

            String[] phones = new String[]{"00019569836440,,22,+22,22"};
            String[] passwords = new String[]{"000012400"};

            int phoneId=0;

            //dialer =DialerFactory.getDirectDialer().newDialer();
            dialer =DialerFactory.getDefault().newDialer();
            dialer.init("COM1");


            dialer.getSerialCommunicationChannel().setBaudrate(9600);

            dialer.connect(phones[phoneId],60000);

// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "900");

            properties.setProperty(MeterProtocol.PASSWORD,passwords[phoneId]);
            //properties.setProperty("UnitType","QTM");
            //properties.setProperty(MeterProtocol.NODEID,"T412    ");

// transfer the properties to the protocol
            quantum.setProperties(properties);

// initialize the protocol
            quantum.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("CST"),Logger.getLogger("name"));

// if optical head dialer, enable the HHU signon mechanism

            System.out.println("*********************** connect() ***********************");

// connect to the meter            
            quantum.connect();


            System.out.println(quantum.getFirmwareVersion());
            System.out.println(quantum.getTime());

//            System.out.println(quantum.getDataDefinitionFactory().getGeneralDiagnosticInfo());
//            System.out.println(quantum.getDataDefinitionFactory().getGeneralDemandConfiguration());
//            System.out.println(quantum.getDataDefinitionFactory().getDemandRegisterConfiguration());
//            System.out.println(quantum.getDataDefinitionFactory().getGeneralEnergyConfiguration());
//            System.out.println(quantum.getDataDefinitionFactory().getEnergyRegisterConfiguration());
//            System.out.println(quantum.getDataDefinitionFactory().getMeterIDS());
//            System.out.println(quantum.getDataDefinitionFactory().getSelfReadGeneralConfiguration());
//            System.out.println(quantum.getDataDefinitionFactory().getSelfReadGeneralInformation());
//            System.out.println(quantum.getDataDefinitionFactory().getSelfReadRegisterConfiguration());
//            
//            System.out.println(quantum.getDataDefinitionFactory().getDefaultViewIdConfiguration());
//            



//            System.out.println("Meter:  "+quantum.getTime());
//            System.out.println("System: "+new Date());
//            quantum.setTime();

            Calendar from = ProtocolUtils.getCalendar(quantum.getTimeZone());
            from.add(Calendar.DAY_OF_MONTH,-4);
            System.out.println(quantum.getProfileData(from.getTime(),true));



//System.out.println(quantum.readRegister(ObisCode.fromString("1.1.1.8.0.255")));

            quantum.disconnect();

        }
        catch(Exception e) {
            e.printStackTrace();
        }


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
