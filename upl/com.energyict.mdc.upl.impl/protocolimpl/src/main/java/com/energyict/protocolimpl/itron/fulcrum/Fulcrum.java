/*
 * Fulcrum.java
 *
 * Created on 8 september 2006, 9:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum;

import com.energyict.dialer.core.*;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.itron.fulcrum.basepages.*;
import com.energyict.protocolimpl.itron.protocol.*;
import com.energyict.protocolimpl.itron.protocol.schlumberger.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

/**
 *
 * @author Koen
 */
public class Fulcrum extends SchlumbergerProtocol {
    
    private BasePagesFactory basePagesFactory=null;
    RegisterFactory registerFactory=null;
    private FulcrumProfile fulcrumProfile=null;
   
    
    /** Creates a new instance of Fulcrum */
    public Fulcrum() {
    }
    
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getFulcrumProfile().getProfileData(lastReading,includeEvents);
    }    
    

    
    protected void doTheConnect() throws IOException {
        // absorb, addresses in the protocoldoc are absolute addresses...
        
        

        
    }    
    
    protected void doTheDisConnect() throws IOException {
        // absorb, addresses in the protocoldoc are absolute addresses...
        
        

        
    }    
    
    protected void doTheInit() {
        // specific initialization for the protocol
        setBasePagesFactory(new BasePagesFactory(this));
        setFulcrumProfile(new FulcrumProfile(this));
    }
    
    protected void doTheDoValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","5000").trim()));
    }
    
    protected List doTheDoGetOptionalKeys() {
        List list = new ArrayList();
        
        
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
    
    protected void hangup() throws IOException {
        getBasePagesFactory().writeBasePage(0x2111, new byte[]{(byte)0xFF});
    }
    
    protected void offLine() throws IOException {
        getBasePagesFactory().writeBasePage(0x2112, new byte[]{(byte)0xFF});
    }
    
    public void setTime() throws IOException {
        if (isAllowClockSet()) {
            getBasePagesFactory().writeBasePage(0x2113, new byte[]{(byte)0xFF}); // STOP METERING FLAG
            getBasePagesFactory().writeBasePage(0x2118, new byte[]{0}); // CLOCK OPTION RUN FLAG
            getBasePagesFactory().setRealTimeBasePage();
            getBasePagesFactory().writeBasePage(0x2116, new byte[]{(byte)0xFF}); // CLOCK OPTION RECONFIGURE FLAG
            getBasePagesFactory().writeBasePage(0x2113, new byte[]{0}); // STOP METERING FLAG
        }
        else throw new UnsupportedException("setTime() is not supported on the Fulcrum meter because is clears all the memory. However, when 'AllowClockSet' property is set to 1, a setTime() can be forced but all memory will be cleared!");
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.8 $";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return getBasePagesFactory().getMeterIdentificationBasePages().toString2();
    }
    
    public String getSerialNumber() throws IOException {
        return "getSerialNumber() not implemented yet";
    }    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Fulcrum fulcrum = new Fulcrum();
        Dialer dialer=null;
        try {
            
            String[] phones = new String[]{"9,17346753630,,,,,,02","9,15867783390","00016604293360","00018168809574"};
            int phoneId=1;
            
            dialer =DialerFactory.getDefault().newDialer();
            //dialer =DialerFactory.getDirectDialer().newDialer();
            //dialer = DialerFactory.get("EMDIALDIALER").newDialer();
            dialer.init("COM1");
            
            
            dialer.getSerialCommunicationChannel().setBaudrate(9600);
            
            dialer.connect(phones[phoneId],60000); 
            
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            //properties.setProperty(MeterProtocol.PASSWORD,"gudma44");
            //properties.setProperty(MeterProtocol.ADDRESS,"RETAILR");
//            properties.setProperty("Retries", "20");
//            properties.setProperty("Timeout", "1000");
            properties.setProperty("DaisyChain", "1");
            properties.setProperty("ProfileInterval", "900"); 
            //properties.setProperty("UnitType","X20");
            //properties.setProperty(MeterProtocol.NODEID,"8986785");

//            properties.setProperty("AllowClockSet","1");
            
// transfer the properties to the protocol
            fulcrum.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            fulcrum.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            
            System.out.println("*********************** connect() 1 ***********************");
            fulcrum.connect();
            System.out.println(fulcrum.getTime());
            
            // changing to the next slave
            fulcrum.disconnect();
            
            System.out.println("*********************** connect() 2 ***********************");
            fulcrum.connect();
            System.out.println(fulcrum.getTime());
            
            // changing to the next slave
            fulcrum.disconnect();
            
            System.out.println("*********************** connect() 3 ***********************");
            fulcrum.connect();
            System.out.println(fulcrum.getTime());
            
            // changing to the next slave
            fulcrum.disconnect();
            
            System.out.println("*********************** connect() 4 ***********************");
            fulcrum.connect();
            System.out.println(fulcrum.getTime());
            
            fulcrum.disconnect();
            
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
        strBuff.append(getBasePagesFactory().getMeterIdentificationBasePages());
        ObisCodeMapper ocm = new ObisCodeMapper(this);
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

    public FulcrumProfile getFulcrumProfile() {
        return fulcrumProfile;
    }

    public void setFulcrumProfile(FulcrumProfile fulcrumProfile) {
        this.fulcrumProfile = fulcrumProfile;
    }


    protected void validateDeviceId() throws IOException {
        boolean check = true;
        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID().trim())==0)) return;
        if (getBasePagesFactory().getMeterIdentificationBasePages().getUnitId().trim().compareTo(getInfoTypeDeviceID().trim()) != 0) {
            String msg =
                    "DeviceId mismatch! meter DeviceId=" + getBasePagesFactory().getMeterIdentificationBasePages().getUnitId().trim() +
                    ", configured DeviceId=" + getInfoTypeDeviceID().trim();
            throw new IOException(msg);
        }

    }
    
    
} // public class Fulcrum extends SchlumbergerProtocol
