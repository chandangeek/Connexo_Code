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
 * JME	|27042009|	Added instantaneous power registers, per phase.
 * 
 */

package com.energyict.protocolimpl.modbus.northerndesign.cube350;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.Unit;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.northerndesign.NDBaseRegisterFactory;

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
    
    protected List<String> doTheGetOptionalKeys() {
        return new ArrayList<String>();
        
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "" + getRegisterFactory().findRegister("firmwareVersion").objectValueWithParser("value0");
        
    }
    
    public String getProtocolVersion() {
        return "$Date$";
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
    
    @SuppressWarnings("unchecked")
    protected List<MessageCategorySpec> doGetMessageCategories(List theCategories) {
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
    
    /**
     * Register factory for the {@link Cube350} protocol. This adds harmonics and model specifics.
     * 
     * @author alex
     */
    @SuppressWarnings("unchecked")
    private static final class RegisterFactory extends NDBaseRegisterFactory {

    	/**
    	 * Create a new instance.
    	 * 
    	 * @param protocol
    	 */
    	private RegisterFactory(final Modbus protocol) {
    		super(protocol);
    	}
    	
    	/**
    	 * {@inheritDoc}
    	 */
		@Override
		protected final void init() {
			super.init();
			
	        /* V1 Harmonics registers */
	        getRegisters().add(new HoldingRegister(7936, 1, toObis("1.1.32.7.2.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7937, 1, toObis("1.1.32.7.3.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7938, 1, toObis("1.1.32.7.4.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7939, 1, toObis("1.1.32.7.5.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7940, 1, toObis("1.1.32.7.6.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7941, 1, toObis("1.1.32.7.7.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7942, 1, toObis("1.1.32.7.8.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7943, 1, toObis("1.1.32.7.9.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7944, 1, toObis("1.1.32.7.10.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7945, 1, toObis("1.1.32.7.11.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7946, 1, toObis("1.1.32.7.12.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7947, 1, toObis("1.1.32.7.13.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7948, 1, toObis("1.1.32.7.14.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(7949, 1, toObis("1.1.32.7.15.255"), Unit.get("%")));

	        /* V2 Harmonics registers */
	        getRegisters().add(new HoldingRegister(8192, 1, toObis("1.1.52.7.2.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8193, 1, toObis("1.1.52.7.3.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8194, 1, toObis("1.1.52.7.4.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8195, 1, toObis("1.1.52.7.5.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8196, 1, toObis("1.1.52.7.6.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8197, 1, toObis("1.1.52.7.7.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8198, 1, toObis("1.1.52.7.8.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8199, 1, toObis("1.1.52.7.9.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8200, 1, toObis("1.1.52.7.10.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8201, 1, toObis("1.1.52.7.11.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8202, 1, toObis("1.1.52.7.12.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8203, 1, toObis("1.1.52.7.13.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8204, 1, toObis("1.1.52.7.14.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8205, 1, toObis("1.1.52.7.15.255"), Unit.get("%")));

	        /* V3 Harmonics registers */
	        getRegisters().add(new HoldingRegister(8448, 1, toObis("1.1.72.7.2.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8449, 1, toObis("1.1.72.7.3.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8450, 1, toObis("1.1.72.7.4.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8451, 1, toObis("1.1.72.7.5.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8452, 1, toObis("1.1.72.7.6.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8453, 1, toObis("1.1.72.7.7.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8454, 1, toObis("1.1.72.7.8.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8455, 1, toObis("1.1.72.7.9.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8456, 1, toObis("1.1.72.7.10.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8457, 1, toObis("1.1.72.7.11.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8458, 1, toObis("1.1.72.7.12.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8459, 1, toObis("1.1.72.7.13.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8460, 1, toObis("1.1.72.7.14.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8461, 1, toObis("1.1.72.7.15.255"), Unit.get("%")));

	        /* I1 Harmonics registers */
	        getRegisters().add(new HoldingRegister(8704, 1, toObis("1.1.31.7.2.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8705, 1, toObis("1.1.31.7.3.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8706, 1, toObis("1.1.31.7.4.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8707, 1, toObis("1.1.31.7.5.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8708, 1, toObis("1.1.31.7.6.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8709, 1, toObis("1.1.31.7.7.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8710, 1, toObis("1.1.31.7.8.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8711, 1, toObis("1.1.31.7.9.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8712, 1, toObis("1.1.31.7.10.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8713, 1, toObis("1.1.31.7.11.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8714, 1, toObis("1.1.31.7.12.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8715, 1, toObis("1.1.31.7.13.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8716, 1, toObis("1.1.31.7.14.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8717, 1, toObis("1.1.31.7.15.255"), Unit.get("%")));

	        /* I2 Harmonics registers */
	        getRegisters().add(new HoldingRegister(8960, 1, toObis("1.1.51.7.2.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8961, 1, toObis("1.1.51.7.3.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8962, 1, toObis("1.1.51.7.4.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8963, 1, toObis("1.1.51.7.5.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8964, 1, toObis("1.1.51.7.6.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8965, 1, toObis("1.1.51.7.7.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8966, 1, toObis("1.1.51.7.8.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8967, 1, toObis("1.1.51.7.9.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8968, 1, toObis("1.1.51.7.10.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8969, 1, toObis("1.1.51.7.11.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8970, 1, toObis("1.1.51.7.12.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8971, 1, toObis("1.1.51.7.13.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8972, 1, toObis("1.1.51.7.14.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(8973, 1, toObis("1.1.51.7.15.255"), Unit.get("%")));

	        /* I3 Harmonics registers */
	        getRegisters().add(new HoldingRegister(9216, 1, toObis("1.1.71.7.2.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9217, 1, toObis("1.1.71.7.3.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9218, 1, toObis("1.1.71.7.4.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9219, 1, toObis("1.1.71.7.5.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9220, 1, toObis("1.1.71.7.6.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9221, 1, toObis("1.1.71.7.7.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9222, 1, toObis("1.1.71.7.8.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9223, 1, toObis("1.1.71.7.9.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9224, 1, toObis("1.1.71.7.10.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9225, 1, toObis("1.1.71.7.11.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9226, 1, toObis("1.1.71.7.12.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9227, 1, toObis("1.1.71.7.13.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9228, 1, toObis("1.1.71.7.14.255"), Unit.get("%")));
	        getRegisters().add(new HoldingRegister(9229, 1, toObis("1.1.71.7.15.255"), Unit.get("%")));

	        getRegisters().add(new HoldingRegister(3592, 1, "firmwareVersion"));
	        getRegisters().add(new HoldingRegister(3590, 1, "MeterModel"));
		}
    }
}
