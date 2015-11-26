/*
 * EictRtuModbus.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.eictmodbusrtu;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCodeFactory;
import com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;
/**
 *
 * @author Koen
 */
public class EictModbusRtu extends Modbus {
    
    ModbusConnection modbusConnection;
    FunctionCodeFactory functionCodeFactory;
    
    /** Creates a new instance of EictRtuModbus */
    public EictModbusRtu() {
    }
    
    protected void doTheConnect() throws IOException {
        
    }
    
    protected void doTheDisConnect() throws IOException {
        
    }
    
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        
    }
    
    
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }
    
    protected void initRegisterFactory(){
        setRegisterFactory(new RegisterFactory(this));
    }

    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
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
            properties.setProperty(MeterProtocol.NODEID,"0");
            properties.setProperty(MeterProtocol.ADDRESS,"5");
            properties.setProperty("HalfDuplex", "1");
            
            // ********************** EictRtuModbus **********************
            EictModbusRtu eictRtuModbus = new EictModbusRtu();
            eictRtuModbus.setProperties(properties);
            eictRtuModbus.setHalfDuplexController(dialer.getHalfDuplexController());
            eictRtuModbus.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            eictRtuModbus.connect();
            //System.out.println(eictRtuModbus.getRegistersInfo(1));
            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.16.8.0.255")));
//            System.out.println(eictRtuModbus.getFunctionCodeFactory().getReadHoldingRegistersRequest(1700-1,3)); // lees 3 16 bit registers
//            
//            System.out.println(eictRtuModbus.getFunctionCodeFactory().getReadHoldingRegistersRequest(1724-1,3));
//            
//            System.out.println(eictRtuModbus.getFunctionCodeFactory().getReadInputRegistersRequest(0, 0));
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }

}
