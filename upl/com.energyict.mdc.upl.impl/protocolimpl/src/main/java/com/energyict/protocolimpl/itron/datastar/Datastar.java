/*
 * Fulcrum.java
 *
 * Created on 8 september 2006, 9:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar;

import com.energyict.dialer.core.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.itron.datastar.basepages.*;
import com.energyict.protocolimpl.itron.protocol.*;
import java.io.IOException;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements Itron Datastar logger. 
 * <BR>
 * <B>@beginchanges</B><BR>
KV|04072007|Add additional multipliers
 * @endchanges
 */

public class Datastar extends SchlumbergerProtocol {
    
    private BasePagesFactory basePagesFactory=null;
    
    private DatastarProfile datastarProfile=null;
    boolean allowClockSet;
    
    /** Creates a new instance of Datastar */
    public Datastar() {
    }
    
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }    
    
    protected void hangup() throws IOException { 
        getBasePagesFactory().writeBasePage(0x0052, new byte[]{(byte)0xFF});
    }
    
    protected void offLine() throws IOException {
        getBasePagesFactory().writeBasePage(0x0053, new byte[]{(byte)0xFF});
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
        setFulcrumProfile(new DatastarProfile(this));
    }
    
    protected void doTheDoValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        allowClockSet = Integer.parseInt(properties.getProperty("AllowClockSet","0").trim()) == 1;
        setDelayAfterConnect(Integer.parseInt(properties.getProperty("DelayAfterConnect","2000").trim()));
    }
    
    protected List doTheDoGetOptionalKeys() {
        List list = new ArrayList();
        list.add("AllowClockSet");
        return list;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        return getBasePagesFactory().getOperatingSetUpBasePage().getProfileInterval()*60;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getBasePagesFactory().getOperatingSetUpBasePage().getNrOfChannels();
    }
    
    public Date getTime() throws IOException {
        return getBasePagesFactory().getRealTimeBasePage().getCalendar().getTime();
    }
    
    public void setTime() throws IOException {
        if (allowClockSet) {
            //getBasePagesFactory().writeBasePage(0x0063, new byte[]{(byte)0xFF}); // WARMST
            getBasePagesFactory().setRealTimeBasePage();
            //getBasePagesFactory().writeBasePage(0x0061, new byte[]{(byte)0xFF}); // RUN
        }
        else throw new UnsupportedException("setTime() is supported on the Datastar meter but you have to make sure that the firmware version you have allows a clocksync without clearing the load profile. Therefor, the 'AllowClockSet' (set to 1 to enable) property adds an extra level of security to the timeset functionality.");
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.4 $";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "firmware revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getFwVersion();
               //", software revision="+getBasePagesFactory().getFirmwareAndSoftwareRevision().getSwVersion()+
               //", options=0x"+Integer.toHexString(getBasePagesFactory().getFirmwareOptionsBasePage().getOptions())+
               //", front end firmware revision="+getBasePagesFactory().getFrontEndFirmwareVersionBasePage().getVersion();
    }
    
    public String getSerialNumber() throws IOException {
        return "getSerialNumber() not implemented yet";
    }    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Datastar datastar = new Datastar();
        Dialer dialer=null;
        try {
            
            String[] phones = new String[]{"00016202253220","00018304014086"};
            String[] passwords = new String[]{"IBEW814",""};
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
            
            properties.setProperty(MeterProtocol.PASSWORD,passwords[phoneId]);
            //properties.setProperty("UnitType","QTM");
            //properties.setProperty(MeterProtocol.NODEID,"T412    ");
            
// transfer the properties to the protocol
            datastar.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(1200,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            datastar.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("MST"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            datastar.connect();
            
            
            //System.out.println(datastar.readRegister(ObisCode.fromString("1.1.9.16.0.0")));
//            System.out.println(datastar.getSerialNumber());
            System.out.println(datastar.getFirmwareVersion());
            System.out.println(datastar.getCommandFactory().getIdentifyCommand());
            System.out.println(datastar.getTime());
            
            
            System.out.println(datastar.getBasePagesFactory().getOperatingSetUpBasePage());
            System.out.println(datastar.getBasePagesFactory().getMassMemoryBasePages());
            System.out.println(datastar.getBasePagesFactory().getKYZDividersBasePage());
            
//            System.out.println("Meter:  "+datastar.getTime());
//            System.out.println("System: "+new Date());
//            datastar.setTime();
            
            Calendar from = ProtocolUtils.getCalendar(datastar.getTimeZone());
            from.add(Calendar.DAY_OF_MONTH,-4);
            System.out.println(datastar.getProfileData(from.getTime(),true));
            
            
//System.out.println(datastar.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
        
            datastar.disconnect();
            
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



    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        
        // tables
        strBuff.append(getBasePagesFactory().getMassMemoryBasePages());
        strBuff.append(getBasePagesFactory().getCurrentMassMemoryRecordBasePage());
        strBuff.append(getBasePagesFactory().getKYZDividersBasePage());
        //strBuff.append(getBasePagesFactory().getSelfreadIndexBasePage());
        //strBuff.append(getBasePagesFactory().getModelTypeBasePage());
        //strBuff.append(getBasePagesFactory().getMeterKhBasePage());
        //strBuff.append(getBasePagesFactory().getRegisterConfigurationBasePage());
        //strBuff.append(getBasePagesFactory().getRegisterMultiplierBasePage());
        strBuff.append(getBasePagesFactory().getOperatingSetUpBasePage());
        strBuff.append(getBasePagesFactory().getDataBuffersBasePage());
        strBuff.append(getBasePagesFactory().getPulseMultiplierAndDisplayUnits());
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

    public DatastarProfile getFulcrumProfile() {
        return datastarProfile;
    }

    public void setFulcrumProfile(DatastarProfile datastarProfile) {
        this.datastarProfile = datastarProfile;
    }
    
} // public class Fulcrum extends SchlumbergerProtocol
