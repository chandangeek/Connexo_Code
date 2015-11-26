/*
 * PQM2.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.ge.pqm2;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
/**
 *
 * @author Koen
 */
public class PQM2 extends Modbus implements SerialNumberSupport {
    
    /** Creates a new instance of PQM2 */
    public PQM2() {
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
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return (String)getRegisterFactory().findRegister("firmware version").objectValueWithParser("firmware version");
    }

    @Override
    public String getSerialNumber() {
        try {
            return (String)getRegisterFactory().findRegister("SerialNumber").value();
        } catch (IOException e){
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:42 +0200 (Thu, 26 Nov 2015)$";
    }
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    public Date getTime() throws IOException {
        return getRegisterFactory().findRegister("clock").dateValue();
        //return new Date();
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
            //properties.setProperty(MeterProtocol.NODEID,"0");
            properties.setProperty(MeterProtocol.ADDRESS,"11");
            properties.setProperty("HalfDuplex", "1");
            
            // ********************** EictRtuModbus **********************
            PQM2 pmq2 = new PQM2();
            
            pmq2.setProperties(properties);
            pmq2.setHalfDuplexController(dialer.getHalfDuplexController());
            pmq2.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            pmq2.connect();
//            System.out.println(pmq2.getRegisterFactory().findRegister("clock").dateValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("clock").getReadHoldingRegistersRequest());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse1 input high").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse1 input low").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse2 input high").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse2 input low").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse3 input high").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse3 input low").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse4 input high").quantityValue());
//            System.out.println(pmq2.getRegisterFactory().findRegister("Pulse4 input low").quantityValue());
            
//            System.out.println(pmq2.getRegisterFactory().findRegister(0).quantityValue());
//            
            //byte[] registerValues = new byte[]{(byte)0xa,(byte)0x2b,(byte)0xc3,(byte)0x1e,(byte)0x5,(byte)0x03,(byte)0x7,(byte)0xd7};
            //pmq2.getRegisterFactory().findRegister("clock").getWriteMultipleRegisters(registerValues);
            
            
            //System.out.println(pmq2.getRegisterFactory().findRegister("UserDefined1").quantityValue());
            //pmq2.getRegisterFactory().findRegister("UserDefined1").getWriteSingleRegister(0x241);
            //System.out.println(pmq2.getRegisterFactory().findRegister("UserDefined1").quantityValue());
            
            //System.out.println(pmq2.getTime());
            
           // System.out.println(pmq2.getRegistersInfo(1));
            
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.2.8.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.2.8.0.255")));
            
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.2.7.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.2.8.0.255")));
            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.1.6.0.255")));
            System.out.println(pmq2.getRegisterFactory().findRegister("ProductDeviceCode").quantityValue());
            System.out.println(pmq2.getRegisterFactory().findRegister("SerialNumber").value());
            
            
//            System.out.println(pmq2.readRegister(ObisCode.fromString("1.1.14.7.0.255")));
            
            
//            System.out.println(pmq2.translateRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(pmq2.getRegistersInfo(1));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
