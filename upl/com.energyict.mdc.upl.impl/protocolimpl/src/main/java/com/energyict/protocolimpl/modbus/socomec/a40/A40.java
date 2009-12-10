/*
 * A40.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.socomec.a40;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
/**
 *
 * @author Koen
 */
public class A40 extends Modbus {
    
    private MultiplierFactory multiplierFactory=null;
    private String socomecType;
    private SocomecProfile profile;
    
    /**
     * Creates a new instance of A20 
     */
    public A40() {
    }

    /**
     * {@inheritDoc}
     */
    protected void doTheConnect() throws IOException {
    }
    
    /**
     * {@inheritDoc}
     */
    protected void doTheDisConnect() throws IOException {
    }
    
    /**
     * {@inheritDoc}
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
    	
    	if(getProfile().isSupported()){
    		ProfileData profileData = new ProfileData();
    		profileData.setChannelInfos(getProfile().getChannelInfos());
    		profileData.setIntervalDatas(getProfile().getIntervalDatas(lastReading));
    		profileData.sort();
    		return profileData;
    	} else {
    		throw new UnsupportedException("ProfileData is not supported by the meter.");
    	}
    	
    }
    
    /**
     * {@inheritDoc}
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
    	return getProfile().getProfileInterval();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","50").trim()));
        setSocomecType(properties.getProperty("SocomecType"));
    }
    
    /**
     * {@inheritDoc}
     */
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "unknown";
    }
    
    /**
     * {@inheritDoc}
     */
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        result.add("SocomecType");
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getProtocolVersion() {
        return "$Date$";
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    /**
     * {@inheritDoc}
     */
    public Date getTime() throws IOException {
    	getRegisterFactory().findRegister(RegisterFactory.currentDateTime).getReadHoldingRegistersRequest().getRegisters();
        return new Date();
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    } 
    
    static public void main(String[] args) {
        try {
            // ********************** Dialer **********************
            Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM1");
            dialer.getSerialCommunicationChannel().setParams(9600,
                                                             SerialCommunicationChannel.DATABITS_8,
                                                             SerialCommunicationChannel.PARITY_NONE,
                                                             SerialCommunicationChannel.STOPBITS_1);
            dialer.connect();
            
            // ********************** Properties **********************
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "900");
            //properties.setProperty(MeterProtocol.NODEID,"0");
            properties.setProperty(MeterProtocol.ADDRESS,"5");
            properties.setProperty("HalfDuplex", "1");

            // ********************** EictRtuModbus **********************
            A40 eictRtuModbus = new A40();
            //System.out.println(eictRtuModbus.translateRegister(ObisCode.fromString("1.1.1.8.0.255")));
            
            eictRtuModbus.setProperties(properties);
            eictRtuModbus.setHalfDuplexController(dialer.getHalfDuplexController());
            eictRtuModbus.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            eictRtuModbus.connect();
            
            //System.out.println(eictRtuModbus.getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification());
            
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).getReadHoldingRegistersRequest());
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).quantityValue());
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(3034).dateValue());
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).quantityValueWithParser("BigDecimal"));
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).objectValueWithParser("powerfactor"));
            
            //System.out.println(eictRtuModbus.getFirmwareVersion());
            //System.out.println(eictRtuModbus.getClass().getName());
            //System.out.println(eictRtuModbus.getTime());
            
            
            //System.out.println(eictRtuModbus.getRegisterFactory().findRegister("versie").values()[0]);
            System.out.println(eictRtuModbus.getRegistersInfo(1));
            //System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.12.7.0.255")));
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.16.8.0.255")));
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.3.7.0.255")));
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.9.7.0.255")));
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.13.7.0.255")));
            //System.out.println(Integer.toHexString(((BigDecimal)eictRtuModbus.getRegisterFactory().findRegister("slotinfo").value()).intValue()));
            //System.out.println(eictRtuModbus.getRegisterFactory().findRegister("fpwordorder").values()[0]);
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
//            System.out.println(eictRtuModbus.getRegistersInfo(0));
//            System.out.println(eictRtuModbus.getRegistersInfo(1));
            
            eictRtuModbus.disconnect();
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    } 

    /**
     * @param address - the given address
     * @return the mulitplier for the given address
     */
    public BigDecimal getRegisterMultiplier(int address) throws IOException, UnsupportedException {
        return getMultiplierFactory().getMultiplier(address);
    }    
    
    /**
     * Getter for the {@link MultiplierFactory}
     * 
     * @return the MulitpliereFactory
     */
    public MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
			multiplierFactory = new MultiplierFactory(this);
		}
        return multiplierFactory;
    }

    /**
     * Getter for the SocomecType (A20/A40)
     * 
     * @return
     */
    public String getSocomecType() {
        return socomecType;
    }

    /**
     * Setter for the SocomecType (A20/A40)
     * 
     * @param socomecType - the type
     */
    private void setSocomecType(String socomecType) {
        this.socomecType = socomecType;
    }

    /**
     * Setter for the {@link ModbusConnection}
     * 
     * @param modbusConnection - the used modbusConnection
     */
    protected void setModbusConnection(ModbusConnection modbusConnection){
    	this.modbusConnection = modbusConnection;
    }
    
    /**
     * Setter for the {@link Logger}
     * 
     * @param logger - the desired logger
     */
    protected void setLogger(Logger logger){
    	setAbstractLogger(logger);
    }
    
    /**
     * @return the current SocomecProfile
     */
    protected SocomecProfile getProfile(){
    	if(this.profile == null){
    		this.profile = new SocomecProfile(this);
    	}
    	return this.profile;
    }
    
    /**
     * Read the raw registers from the MobBus device
     * 
     * @param address - startAddress
     * @param length - the required data length
     * @return the registers from the device
     * @throws IOException if we couldn't read the data
     */
    int[] readRawValue(int address, int length)  throws IOException {
    	HoldingRegister r = new HoldingRegister(address, length);
        r.setRegisterFactory(getRegisterFactory());
        return r.getReadHoldingRegistersRequest().getRegisters();
    }
    
    /**
     * @return the number of channels
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getProfile().getNumberOfChannels();
    }
}
