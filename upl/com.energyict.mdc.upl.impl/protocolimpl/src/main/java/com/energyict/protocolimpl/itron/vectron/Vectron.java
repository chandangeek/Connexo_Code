/*
 * Fulcrum.java
 *
 * Created on 8 september 2006, 9:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;
import com.energyict.protocolimpl.itron.vectron.basepages.BasePagesFactory;
import com.energyict.protocolimpl.itron.vectron.basepages.RegisterFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 */
public class Vectron extends SchlumbergerProtocol {
    
    public static final String WAIT_UNTIL_TIME_VALID = "waitUntilTimeValid";
    public static final String WAITING_TIME = "waitingTime";
    private BasePagesFactory basePagesFactory=null;
    RegisterFactory registerFactory=null;
    private VectronProfile vectronProfile=null;
    boolean allowClockSet;
    boolean waitUntilTimeValid;
    private int waitingTime = 5;

    public Vectron() {
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
        //getBasePagesFactory().setMemStartAddress(getCommandFactory().getIdentifyCommand().getMemStart());
    }
    
    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new VectronProfile(this));
    }
    
    protected void doTheDoValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        allowClockSet = Integer.parseInt(properties.getProperty("AllowClockSet","0").trim()) == 1;
        setDelayAfterConnect(Integer.parseInt(properties.getProperty("DelayAfterConnect","2000").trim()));
        waitUntilTimeValid = Integer.parseInt(properties.getProperty(WAIT_UNTIL_TIME_VALID,"1")) == 1;
        waitingTime = Integer.parseInt(properties.getProperty(WAITING_TIME,"5").trim());
    }
    
    protected List doTheDoGetOptionalKeys() {
        List list = new ArrayList();
        list.add("AllowClockSet");
        list.add(WAIT_UNTIL_TIME_VALID);
        list.add(WAITING_TIME);
        return list;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getProfileInterval()*60;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getBasePagesFactory().getMassMemoryBasePages().getNrOfChannels();
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
        return "$Date: 2017-01-27 13:23:41 +0200 (Fr, 27 Jan 2017)$";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "firmware revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getFwVersion()+
               ", software revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getSwVersion()+
               ", options=0x"+Integer.toHexString(getBasePagesFactory().getFirmwareOptionsBasePage().getOptions())+
               ", front end firmware revision="+getBasePagesFactory().getFrontEndFirmwareVersionBasePage().getVersion();
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Vectron vectron = new Vectron();
        Dialer dialer=null;
        try {
            
            String[] phones = new String[]{"00012254734958","00017149909878","0014156811226"};
            String[] passwords = new String[]{"EXKV","","5F296E00"};
            int phoneId=2;
            
            //dialer =DialerFactory.getDirectDialer().newDialer();
            dialer =DialerFactory.getDefault().newDialer();
            dialer.init("COM5");
            
            
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
            vectron.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(1200,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            vectron.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("PST"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            vectron.connect();
            
            
            //System.out.println(vectron.readRegister(ObisCode.fromString("1.1.9.16.0.0")));
//            System.out.println(vectron.getSerialNumber());
            System.out.println(vectron.getFirmwareVersion());
            System.out.println(vectron.getCommandFactory().getIdentifyCommand());
            System.out.println(vectron.getTime());
            
            
            System.out.println(vectron.getBasePagesFactory().getMassMemoryBasePages());
//            System.out.println(vectron.getBasePagesFactory().getProgramTableBasePage(false));
//            System.out.println(vectron.getBasePagesFactory().getProgramTableBasePage(true));
//            System.out.println(vectron.getBasePagesFactory().getMultipliersBasePage());
//            System.out.println(vectron.getBasePagesFactory().getInstantaneousRegMultipliers());
//            System.out.println(vectron.getBasePagesFactory().getPointerTimeDateRegisterReadingBasePage());
//            System.out.println(vectron.getBasePagesFactory().getRegisterDataBasePage());
//            System.out.println(vectron.getBasePagesFactory().getRegisterDataLastSeasonBasePage());
//            System.out.println(vectron.getBasePagesFactory().getRegisterDataSelfReadBasePage());
//            System.out.println(vectron.getBasePagesFactory().getVoltageAndCurrentBasePage());
            System.out.println(vectron.getBasePagesFactory().getFrontEndFirmwareVersionBasePage());
            System.out.println(vectron.getBasePagesFactory().getSelfreadIndexBasePage());
            System.out.println(vectron.getBasePagesFactory().getFirmwareOptionsBasePage());
            System.out.println(vectron.getBasePagesFactory().getModelTypeBasePage());
            System.out.println(vectron.getBasePagesFactory().getMeterKhBasePage());
            System.out.println(vectron.getBasePagesFactory().getRegisterConfigurationBasePage());
            System.out.println(vectron.getBasePagesFactory().getRegisterMultiplierBasePage());
            
//            System.out.println("Meter:  "+vectron.getTime());
//            System.out.println("System: "+new Date());
//            vectron.setTime();
            
            

            
            
            Calendar from = ProtocolUtils.getCalendar(vectron.getTimeZone());
            from.add(Calendar.DAY_OF_MONTH,-4);
            System.out.println(vectron.getProfileData(from.getTime(),true));
            
            
//System.out.println(vectron.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
        
            vectron.disconnect();
            
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
        
        // tables
        strBuff.append(getBasePagesFactory().getMassMemoryBasePages());
        strBuff.append(getBasePagesFactory().getFrontEndFirmwareVersionBasePage());
        strBuff.append(getBasePagesFactory().getSelfreadIndexBasePage());
        strBuff.append(getBasePagesFactory().getFirmwareOptionsBasePage());
        strBuff.append(getBasePagesFactory().getModelTypeBasePage());
        strBuff.append(getBasePagesFactory().getMeterKhBasePage());
        strBuff.append(getBasePagesFactory().getRegisterConfigurationBasePage());
        strBuff.append(getBasePagesFactory().getRegisterMultiplierBasePage());
        strBuff.append(getBasePagesFactory().getOperatingSetUpBasePage());
        // registers
        strBuff.append(ocm.getRegisterInfo());
        
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

    public VectronProfile getFulcrumProfile() {
        return vectronProfile;
    }

    public void setFulcrumProfile(VectronProfile vectronProfile) {
        this.vectronProfile = vectronProfile;
    }
    
    public boolean waitUntilTimeValid() {
        return waitUntilTimeValid;
    }

    public long getWaitingTime() {
        return waitingTime;
    }
} // public class Fulcrum extends SchlumbergerProtocol
