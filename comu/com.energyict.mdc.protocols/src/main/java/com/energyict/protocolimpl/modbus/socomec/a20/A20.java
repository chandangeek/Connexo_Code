/*
 * A20.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.socomec.a20;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;
/**
 *
 * @author Koen
 */
public class A20 extends Modbus {
    
    ModbusConnection modbusConnection;
    private RegisterFactory registerFactory;
    private MultiplierFactory multiplierFactory=null;
    private String socomecType;
    /**
     * Creates a new instance of A20 
     */
    public A20() {
    }

    
    
    
    protected void doTheConnect() throws IOException {
        
    }
    
    protected void doTheDisConnect() throws IOException {
        
    }
    
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","50").trim()));
        setSocomecType(properties.getProperty("SocomecType"));
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "unknown";
    }
    
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        result.add("SocomecType");
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Socomec Diris A20";
    }
    
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
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
            properties.setProperty(MeterProtocol.ADDRESS,"9");
            properties.setProperty("HalfDuplex", "1");

            // ********************** EictRtuModbus **********************
            A20 eictRtuModbus = new A20();
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

    public BigDecimal getRegisterMultiplier(int address) throws IOException, UnsupportedException {
        return getMultiplierFactory().getMultiplier(address);
    }    
    
    public MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null)
            multiplierFactory = new MultiplierFactory(this);
        return multiplierFactory;
    }

    public String getSocomecType() {
        return socomecType;
    }

    private void setSocomecType(String socomecType) {
        this.socomecType = socomecType;
    }


    
}
