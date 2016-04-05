/*
 * IQ200.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.energyict.protocolimpl.modbus.cutlerhammer.iq230;

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
public class IQ230 extends Modbus {

    ModbusConnection modbusConnection;
    private RegisterFactory registerFactory;
    private MultiplierFactory multiplierFactory = null;

    /**
     * Creates a new instance of IQ200 
     */
    public IQ230() {
    }

    protected void doTheConnect() throws IOException {

    }

    protected void doTheDisConnect() throws IOException {

    }

    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout", "50").trim()));
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        //return getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getSlaveId()+", "+getRegisterFactory().getFunctionCodeFactory().getReportSlaveId().getAdditionalDataAsString();
        return "" + (BigDecimal) getRegisterFactory().findRegister("productid").value();
    }

    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }

    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
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

    static public void start() {
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
            properties.setProperty(MeterProtocol.ADDRESS, "13");
            properties.setProperty("HalfDuplex", "1");
            properties.setProperty("RegisterOrderFixedPoint", "1");

            // ********************** EictRtuModbus **********************
            IQ230 eictRtuModbus = new IQ230();
//            System.out.println(eictRtuModbus.translateRegister(ObisCode.fromString("1.1.52.7.0.255")));

            eictRtuModbus.setProperties(properties);
            eictRtuModbus.setHalfDuplexController(dialer.getHalfDuplexController());
            eictRtuModbus.init(dialer.getInputStream(), dialer.getOutputStream(), TimeZone.getTimeZone("ECT"), Logger.getLogger("name"));
            eictRtuModbus.connect();

            //System.out.println(eictRtuModbus.getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification());

//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).getReadHoldingRegistersRequest());
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).quantityValue());
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(3034).dateValue());
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).quantityValueWithParser("BigDecimal"));
//            System.out.println(eictRtuModbus.getRegisterFactory().findRegister(1700).objectValueWithParser("powerfactor"));

            System.out.println(eictRtuModbus.getFirmwareVersion());
            //System.out.println(eictRtuModbus.getClass().getName());
            //System.out.println(eictRtuModbus.getTime());

            //System.out.println(eictRtuModbus.getRegistersInfo(1));
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.32.7.0.255")));
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.52.7.0.255")));
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.72.7.0.255")));
            //System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
            //System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.16.8.0.255")));
            //System.out.println(eictRtuModbus.getRegisterFactory().findRegister("productid").values()[0]&0xFF);
            //System.out.println(eictRtuModbus.getRegisterFactory().findRegister("fpwordorder").values()[0]);
//            System.out.println(eictRtuModbus.readRegister(ObisCode.fromString("1.1.1.7.0.255")));
//            System.out.println(eictRtuModbus.getRegistersInfo(0));
//            System.out.println(eictRtuModbus.getRegistersInfo(1));

            eictRtuModbus.disconnect();
            dialer.disConnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void main(String[] args) {
        IQ230.start();
        IQ230.start();
    }

    public BigDecimal getRegisterMultiplier(int address) throws IOException, UnsupportedException {
        return getMultiplierFactory().getMultiplier(address);
    }

    public MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
            multiplierFactory = new MultiplierFactory(this);
        }
        return multiplierFactory;
    }
}
