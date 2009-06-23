/*
 * Hawkeye.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.veris.hawkeye;

import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.modbus.core.connection.*;
import com.energyict.protocolimpl.modbus.core.discover.*;
import com.energyict.protocolimpl.modbus.core.functioncode.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.*;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
/**
 *
 * @author Koen
 */
public class Hawkeye extends Modbus  {
    
    /**
     * Creates a new instance of Hawkeye 
     */
    public Hawkeye() {
    }
    
    protected void doTheConnect() throws IOException {
        
    }
    
    protected void doTheDisConnect() throws IOException {
        
    }
    
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","25").trim()));
    }
    
    
    
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        //return getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification().toString();
        return getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getSlaveId()+", "+getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.5 $";
    }
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    public Date getTime() throws IOException {
        //return getRegisterFactory().findRegister("clock").dateValue();
        return new Date();
    }
    
   

    
    public DiscoverResult discover(DiscoverTools discoverTools) {
        DiscoverResult discoverResult = new DiscoverResult();
        discoverResult.setProtocolMODBUS();
        
        try {
            setProperties(discoverTools.getProperties());
            if (getInfoTypeHalfDuplex() != 0)
                setHalfDuplexController(discoverTools.getDialer().getHalfDuplexController());
            init(discoverTools.getDialer().getInputStream(),discoverTools.getDialer().getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            connect();

            String fwVersion = getFirmwareVersion();
            
            if (fwVersion.toLowerCase().indexOf("veris format")>=0) {
                discoverResult.setDiscovered(true);
                discoverResult.setProtocolName("com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.EictVeris");
                discoverResult.setAddress(discoverTools.getAddress());
            }
            else if (fwVersion.toLowerCase().indexOf("veris h8036")>=0) {
                discoverResult.setDiscovered(true);
                discoverResult.setProtocolName("com.energyict.protocolimpl.modbus.veris.hawkeye.Hawkeye");
                discoverResult.setAddress(discoverTools.getAddress());
            }
            else {
                discoverResult.setDiscovered(false);
            }


            
            discoverResult.setResult(fwVersion);
            return discoverResult;
        }
        catch(Exception e) {
            discoverResult.setDiscovered(false);
            discoverResult.setResult(e.toString());
            return discoverResult;
        }
        finally {
           try { 
              disconnect();
           }
           catch(IOException e) {
               // absorb
           }
        }
        
    }    
    
    static public void main(String[] args) {
        
        try {
int count=0;
while(count++<1) {
            
            // ********************** Dialer **********************
            Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            String comport;
            if ((args==null) || (args.length<=1))
                comport="COM1";
            else
                comport=args[1]; //"/dev/ttyXR0";
            dialer.init(comport);
            dialer.getSerialCommunicationChannel().setParams(9600,
                                                             SerialCommunicationChannel.DATABITS_8,
                                                             SerialCommunicationChannel.PARITY_NONE,
                                                             SerialCommunicationChannel.STOPBITS_1);
            dialer.connect();
            
            // ********************** Properties **********************
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "60");
            //properties.setProperty(MeterProtocol.NODEID,"0");
            properties.setProperty(MeterProtocol.ADDRESS,"1");
            properties.setProperty("HalfDuplex", "-1");
            
//            int ift;
//            if ((args==null) || (args.length==0))
//                ift=25;
//            else
//                ift=Integer.parseInt(args[0]);
//            
//            properties.setProperty("InterframeTimeout", ""+ift);
            
            
            // ********************** EictRtuModbus **********************
            Hawkeye hawkeye = new Hawkeye();
            
            hawkeye.setProperties(properties);
            hawkeye.setHalfDuplexController(dialer.getHalfDuplexController());
            hawkeye.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            hawkeye.connect();
            
            
            System.out.println(hawkeye.getFirmwareVersion());
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.32.7.0.255")));
 
//            System.out.println(hawkeye.getRegistersInfo(0));
            System.out.println(hawkeye.getRegistersInfo(1));
            
            
            dialer.disConnect();
            hawkeye.disconnect();
}            
            //System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.52.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.72.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.12.7.0.255")));
//
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.132.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.152.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.172.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.112.7.0.255")));
//            
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.31.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.51.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.71.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.11.7.0.255")));
//            
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.1.6.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.3.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.9.7.0.255")));
//            
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.21.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.41.7.0.255")));
//            System.out.println(hawkeye.readRegister(ObisCode.fromString("1.1.61.7.0.255")));
//            
//            System.out.println(hawkeye.getRegisterFactory().getFunctionCodeFactory().getReportSlaveId());
                    
                    
            //System.out.println(hawkeye.translateRegister(ObisCode.fromString("1.1.1.8.0.255")));
            //System.out.println(hawkeye.getRegistersInfo(1));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
