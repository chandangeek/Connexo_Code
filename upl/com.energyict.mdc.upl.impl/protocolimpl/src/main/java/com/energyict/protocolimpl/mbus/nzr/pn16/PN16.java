/*
 * PN16.java
 *
 * Created on 2 oktober 2007, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.nzr.pn16;

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
import com.energyict.protocolimpl.mbus.core.MBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author kvds
 */
public class PN16 extends MBus {
    
    
    RegisterFactory registerFactory=null;
    
    /** Creates a new instance of PN16 */
    public PN16() {
    }
 
    
    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    }
        
    protected void doTheConnect() throws IOException {
        getMBusConnection().sendSND_NKE(); 
    }
    protected void doTheDisConnect() throws IOException {
        
    }
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        
    }
    protected List doTheGetOptionalKeys() {
        List list = new ArrayList();
        return list;
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "NOT YET IMPLEMENTED";
    }

    /**
     * The protocol version date
     */
    public String getProtocolVersion() {
        return "$Date$";
    }    
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }
    
    
    static public void main(String[] args) {
        try {
            // ********************** Dialer **********************
            Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM1"); // "/dev/ttyXR0";
            dialer.getSerialCommunicationChannel().setParams(2400,
                                                             SerialCommunicationChannel.DATABITS_8,
                                                             SerialCommunicationChannel.PARITY_EVEN,
                                                             SerialCommunicationChannel.STOPBITS_1);
            dialer.connect();
            
            // ********************** Properties **********************
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "60");
            properties.setProperty(MeterProtocol.ADDRESS,"109");
            //properties.setProperty("HalfDuplex", "-1");
            // ********************** EictRtuModbus **********************
            PN16 pN16 = new PN16();
            
            pN16.setProperties(properties); 
            //pN16.setHalfDuplexController(dialer.getHalfDuplexController());
            pN16.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            pN16.connect();
            
            
            
            //pN16.getMBusConnection().sendOut(new byte[]{0x68,0x04,0x04,0x68,0x53,0x6c,0x50,0x00,(byte)0xbf,0x16});
            pN16.getMBusConnection().sendSND_NKE(); 
//            pN16.getMBusConnection().sendApplicationReset(5);
            //pN16.getMBusConnection().sendREQ_UD1();
            System.out.println("************************************************************************************************");

            
             
            //IEC870Frame frame = pN16.getMBusConnection().sendREQ_UD2();
            System.out.println(pN16.getTime());
            System.out.println(pN16.getRegistersInfo(0));
            //System.out.println(pN16.readRegister(ObisCode.fromString("1.1.52.7.0.255")));
            System.out.println(pN16.readRegister(ObisCode.fromString("0.0.96.99.0.0")));
            System.out.println(pN16.readRegister(ObisCode.fromString("0.0.96.99.0.1")));
            
            
            //System.out.println(frame.getASDU().buildAbstractCIFieldObject(pN16.getTimeZone()));
            //System.out.println(ProtocolUtils.outputHexString(asdu.getInformationObject().getObjData()));
            
//            System.out.println("************************************************************************************************");
//            frame = pN16.getMBusConnection().sendREQ_UD2();
//            System.out.println(frame);
//            asdu = frame.getASDU();
//            System.out.println(asdu.toString(pN16.getTimeZone()));
//            //System.out.println(ProtocolUtils.outputHexString(frame.getData()));
//            System.out.println(ProtocolUtils.outputHexString(asdu.getInformationObject().getObjData()));
//
//            System.out.println("************************************************************************************************");
//            frame = pN16.getMBusConnection().sendREQ_UD2();
//            System.out.println(frame);
//            asdu = frame.getASDU();
//            System.out.println(asdu.toString(pN16.getTimeZone()));
//            //System.out.println(ProtocolUtils.outputHexString(frame.getData()));
//            System.out.println(ProtocolUtils.outputHexString(asdu.getInformationObject().getObjData()));
            
            
            //System.out.println(pN16.getFirmwareVersion());

//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.32.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.132.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
            dialer.disConnect();
            pN16.disconnect();

            //System.out.println(pN16.readRegister(ObisCode.fromString("1.1.52.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.72.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.12.7.0.255")));
//
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.152.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.172.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.112.7.0.255")));
//            
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.31.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.51.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.71.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.11.7.0.255")));
//            
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.1.6.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.3.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.9.7.0.255")));
//            
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.21.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.41.7.0.255")));
//            System.out.println(pN16.readRegister(ObisCode.fromString("1.1.61.7.0.255")));
//            
//            System.out.println(pN16.getRegisterFactory().getFunctionCodeFactory().getReportSlaveId());
                    
                    
            //System.out.println(pN16.getRegistersInfo(1));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }    
}
