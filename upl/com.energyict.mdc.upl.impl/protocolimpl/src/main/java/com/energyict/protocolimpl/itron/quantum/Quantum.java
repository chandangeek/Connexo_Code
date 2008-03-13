/*
 * Fulcrum.java
 *
 * Created on 8 september 2006, 9:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum;

import com.energyict.dialer.core.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.itron.quantum.basepages.*;
import com.energyict.protocolimpl.itron.protocol.*;
import java.io.IOException;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

/**
 *
 * @author Koen 
 */
public class Quantum extends SchlumbergerProtocol {
    
    private BasePagesFactory basePagesFactory=null;
    RegisterFactory registerFactory=null;
    private QuantumProfile quantumProfile=null;
    boolean allowClockSet;
    private int loadProfileUnitScale;
    
    /** Creates a new instance of Quantum */
    public Quantum() {
    }
    
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }    
    
    protected void hangup() throws IOException {
        //getBasePagesFactory().writeBasePage(0x2111, new byte[]{(byte)0xFF});
    }
    
    protected void offLine() throws IOException {
        //getBasePagesFactory().writeBasePage(0x2112, new byte[]{(byte)0xFF});
    }
    
    protected void doTheDisConnect() throws IOException {
        
    }

    // The Quantuum meter uses only offset addresses in its protocoldoc. S, we need to set the base memory start address...
    protected void doTheConnect() throws IOException {
        getBasePagesFactory().setMemStartAddress(getCommandFactory().getIdentifyCommand().getMemStart());
    }
    
    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new QuantumProfile(this));
    }
    
    protected void doTheDoValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        allowClockSet = Integer.parseInt(properties.getProperty("AllowClockSet","0").trim()) == 1;
        setDelayAfterConnect(Integer.parseInt(properties.getProperty("DelayAfterConnect","2000").trim()));
        setLoadProfileUnitScale(Integer.parseInt(properties.getProperty("LoadProfileUnitScale","3").trim()));
    }
    
    protected List doTheDoGetOptionalKeys() {
        List list = new ArrayList();
        list.add("AllowClockSet");
        list.add("LoadProfileUnitScale");
        return list;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getRecordingIntervalLength()*60;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getNumberOfChannels();
    }
    
    public Date getTime() throws IOException {
        return getBasePagesFactory().getRealTimeBasePage().getCalendar().getTime();
    }
    
    public void setTime() throws IOException {
//        if (allowClockSet) {
//            getBasePagesFactory().writeBasePage(0x2113, new byte[]{(byte)0xFF});
//            getBasePagesFactory().writeBasePage(0x2118, new byte[]{0});
//            getBasePagesFactory().setRealTimeBasePage();
//            getBasePagesFactory().writeBasePage(0x2116, new byte[]{(byte)0xFF});
//            getBasePagesFactory().writeBasePage(0x2113, new byte[]{0});
//        }
//        else throw new UnsupportedException("setTime() is not supported on the Fulcrum meter because is clears all the memory. However, when 'AllowClockSet' property is set to 1, a setTime() can be forced but all memory will be cleared!");
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.5 $";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "revision nr "+getBasePagesFactory().getFirmwareRevisionBasePage().getFirmwareRevision();
    }
    
    public String getSerialNumber() throws IOException {
        return "getSerialNumber() not implemented yet";
    }    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Quantum quantum = new Quantum();
        Dialer dialer=null;
        try {
            
            String[] phones = new String[]{"00017857383234","00018705345024","00016206352164"};
            int phoneId=1;
            
            //dialer =DialerFactory.getDirectDialer().newDialer();
            dialer =DialerFactory.getDefault().newDialer();
            dialer.init("COM1");
            
            
            dialer.getSerialCommunicationChannel().setBaudrate(1200);
            
            dialer.connect(phones[phoneId],60000); 
            
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "900");
            
            //properties.setProperty(MeterProtocol.PASSWORD,"IBEW814");
            //properties.setProperty("UnitType","QTM");
            //properties.setProperty(MeterProtocol.NODEID,"T412    ");
            
// transfer the properties to the protocol
            quantum.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(1200,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            quantum.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("CST"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            quantum.connect();
            
            
            //System.out.println(quantum.readRegister(ObisCode.fromString("1.1.9.16.0.0")));
//            System.out.println(quantum.getSerialNumber());
            System.out.println(quantum.getFirmwareVersion());
            System.out.println(quantum.getCommandFactory().getIdentifyCommand());
            System.out.println(quantum.getTime());
            
            
            System.out.println(quantum.getBasePagesFactory().getMassMemoryBasePages());
//            System.out.println(quantum.getBasePagesFactory().getProgramTableBasePage(false));
//            System.out.println(quantum.getBasePagesFactory().getProgramTableBasePage(true));
            System.out.println(quantum.getBasePagesFactory().getMultipliersBasePage());
//            System.out.println(quantum.getBasePagesFactory().getInstantaneousRegMultipliers());
//            System.out.println(quantum.getBasePagesFactory().getPointerTimeDateRegisterReadingBasePage());
//            System.out.println(quantum.getBasePagesFactory().getRegisterDataBasePage());
//            System.out.println(quantum.getBasePagesFactory().getRegisterDataLastSeasonBasePage());
//            System.out.println(quantum.getBasePagesFactory().getRegisterDataSelfReadBasePage());
            System.out.println(quantum.getBasePagesFactory().getVoltageAndCurrentBasePage());
            
//            System.out.println("Meter:  "+quantum.getTime());
//            System.out.println("System: "+new Date());
//            quantum.setTime();
            
            

            
            
//            Calendar from = ProtocolUtils.getCalendar(quantum.getTimeZone());
//            from.add(Calendar.DAY_OF_MONTH,-4);
//            System.out.println(quantum.getProfileData(from.getTime(),true));
            
            
//System.out.println(quantum.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
        
            quantum.disconnect();
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        
    }

    public BasePagesFactory getBasePagesFactory() {
        return basePagesFactory;
    }

    public void setBasePagesFactory(BasePagesFactory basePagesFactory) {
        this.basePagesFactory = basePagesFactory;
    }

    public RegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(this);
            registerFactory.init();
        }
        return registerFactory;
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        
        // registers
        strBuff.append(ocm.getRegisterInfo());
        
        // tables
        strBuff.append(getBasePagesFactory().getMassMemoryBasePages());
        strBuff.append(getBasePagesFactory().getProgramTableBasePage(false));
        strBuff.append(getBasePagesFactory().getProgramTableBasePage(true));
        strBuff.append(getBasePagesFactory().getInstantaneousRegMultipliers());
        
        strBuff.append(getBasePagesFactory().getMultipliersBasePage());
        
        strBuff.append(getBasePagesFactory().getPointerTimeDateRegisterReadingBasePage());
        strBuff.append(getBasePagesFactory().getVoltageAndCurrentBasePage());
        
        strBuff.append(getBasePagesFactory().getGeneralSetUpBasePage());
        strBuff.append(getBasePagesFactory().getMassMemoryBasePages());
        
        return strBuff.toString();
    }
    
    // RegisterProtocol Interface implementation
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }    

    public QuantumProfile getFulcrumProfile() {
        return quantumProfile;
    }

    public void setFulcrumProfile(QuantumProfile quantumProfile) {
        this.quantumProfile = quantumProfile;
    }

    public int getLoadProfileUnitScale() {
        return loadProfileUnitScale;
    }

    public void setLoadProfileUnitScale(int loadProfileUnitScale) {
        this.loadProfileUnitScale = loadProfileUnitScale;
    }
    
} // public class Fulcrum extends SchlumbergerProtocol
