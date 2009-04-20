/*
 * Cube350.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 * 
 * Changes:
 * JME	|06042009|	Added harmonics registers 2 to 15 for V1, V2, V3, I1, I2 and I3.
 * 
 */

package com.energyict.protocolimpl.modbus.northerndesign.cube350;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import com.energyict.dialer.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.modbus.core.Modbus;

/**
 * @author fbo
 */

public class Cube350 extends Modbus {
	
    public Cube350() { }
    
    protected void doTheConnect() throws IOException { /* relax */ }
    
    protected void doTheDisConnect() throws IOException { /* relax */ }
    
    protected void doTheValidateProperties(Properties properties) 
        throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout", "25").trim()));
        setInfoTypeMeterFirmwareVersion(properties.getProperty("MeterFirmwareVersion", "1.07"));
        if (Float.parseFloat(getInfoTypeMeterFirmwareVersion())>=1.07)
            setInfoTypeFirstTimeDelay(Integer.parseInt(properties.getProperty("FirstTimeDelay", "0").trim()));
        else
        	setInfoTypeFirstTimeDelay(Integer.parseInt(properties.getProperty("FirstTimeDelay", "400").trim()));
    }
    
    protected List doTheGetOptionalKeys() {
        
        return new ArrayList();
        
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "" + getRegisterFactory().findRegister("firmwareVersion").objectValueWithParser("value0");
        
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.4 $";
    }
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
  
    public Date getTime() throws IOException {
        return new Date();
    }
    
    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    }

    /*******************************************************************************************
    M e s s a g e P r o t o c o l  i n t e r f a c e  d e l e g a t e d  m e t h o d s   
    *******************************************************************************************/    
    protected MessageResult doQueryMessage(MessageEntry messageEntry) throws IOException {
		try {
			if (messageEntry.getContent().indexOf("<TestMessage")>=0) {
				getLogger().info(messageEntry.getContent());
				return MessageResult.createSuccess(messageEntry);
			}
			else return MessageResult.createUnknown(messageEntry);
		}
		catch(Exception e) {
			return MessageResult.createFailed(messageEntry);
		}
    }
    protected List doGetMessageCategories(List theCategories) {
        MessageCategorySpec cat = new MessageCategorySpec("Cube 350 messages");
        MessageSpec msgSpec = addBasicMsg("Test message", "TestMessage", false);
        cat.addMessageSpec(msgSpec);
        theCategories.add(cat);
        return theCategories;
    }
    
    
    static public void main(String[] args) {
        
        try {
            int count = 0;
            //while (count++ < 1) {
                
                // ********************** Dialer **********************
                Dialer dialer = DialerFactory.getDirectDialer().newDialer();
                String comport;
                if ((args == null) || (args.length <= 1))
                    comport = "COM1";
                else
                    comport = args[1]; // "/dev/ttyXR0";
                dialer.init(comport);
                dialer.getSerialCommunicationChannel().setParams(
                        9600, 
                        SerialCommunicationChannel.DATABITS_8,
                        SerialCommunicationChannel.PARITY_NONE, 
                        SerialCommunicationChannel.STOPBITS_1);
                dialer.connect();
                
                int t=0;
                //while(t++<1) {
                	
                // ********************** Properties **********************
                Properties properties = new Properties();
                properties.setProperty("ProfileInterval", "60");
                // properties.setProperty(MeterProtocol.NODEID,"0");
                
                if ((t%2)==0) properties.setProperty(MeterProtocol.ADDRESS, "1");
                else properties.setProperty(MeterProtocol.ADDRESS, "1");
                
//                properties.setProperty(MeterProtocol.ADDRESS, "1");
                
                properties.setProperty("HalfDuplex", "-1");
                
                //properties.setProperty("Timeout", "600000");
                //properties.setProperty("ResponseTimeout", "600000");
                properties.setProperty("PhysicalLayer", "0");
                //properties.setProperty("InterframeTimeout", "" + 25);
                
                // ********************** EictRtuModbus **********************
                Cube350 cube = new Cube350();
                
                cube.setProperties(properties);
                
                cube.setHalfDuplexController(
                        dialer.getHalfDuplexController());
                
                cube.init(
                        dialer.getInputStream(), 
                        dialer.getOutputStream(), 
                        TimeZone.getTimeZone("ECT"), 
                        Logger.getLogger("name"));
                
                cube.connect();
                
                
               
//                System.out.println(t+" *************************\n"+cube.getRegistersInfo(1));
                
//                System.out.println(cube.getFirmwareVersion());
//                
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
                
                
                //System.out.println(cube.readRegister(ObisCode.fromString("1.1.3.7.0.255")));
                
                
                //System.out.println(cube.getRegisterFactory().getFunctionCodeFactory().getReadHoldingRegistersRequest(3584, 2));
                
                //cube.getRegisterFactory().getFunctionCodeFactory().getWriteMultipleRegisters(0x0e00, 2, new byte[]{0x12,(byte)0x34,(byte)0x56,(byte)0x78});
                //cube.getRegisterFactory().getFunctionCodeFactory().getWriteSingleRegister(0x0e00, 0x100);
                
                System.out.println(cube.getRegisterFactory().getFunctionCodeFactory().getReadHoldingRegistersRequest(0x0e00, 1));
                System.out.println(cube.getRegisterFactory().getFunctionCodeFactory().getReadHoldingRegistersRequest(0x0e01, 1));
                
                
                //cube.setRegister(name, value)
                
                
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.9.7.0.255")));
                //System.out.println(cube.readRegister(ObisCode.fromString("1.1.13.7.0.255")));
//
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.21.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.41.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.61.7.0.255")));
//                
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.33.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.53.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.73.7.0.255")));
//                
//
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.32.7.0.255")));
                //System.out.println(cube.readRegister(ObisCode.fromString("1.1.52.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.72.7.0.255")));
//                
//
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.132.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.152.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.172.7.0.255")));
//                
//
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.31.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.51.7.0.255")));
//                System.out.println(cube.readRegister(ObisCode.fromString("1.1.71.7.0.255")));
                
                //System.out.println(cube.getRegisterFactory().findRegister("MeterModel").values()[0]);
                cube.disconnect();
//                }
                dialer.disConnect();
            //}
             

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
