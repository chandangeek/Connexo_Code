package com.energyict.protocolimpl.modbus.spiraxsarco;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.connection.ModbusTCPConnection;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadStatuses;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by cisac on 11/16/2015.
 */
public class VLM20 extends Modbus{

    private static final String START_REGISTERS_ZERO_BASED = "StartRegistersZeroBased";
    private static final String CONNECTION = "Connection";
    private boolean startRegistersZeroBased;

    @Override
    protected void doTheConnect() throws IOException {

    }

    @Override
    protected void doTheDisConnect() throws IOException {

    }

    @Override
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateAndSetStartRegistesZeroBasedFlag(properties.getProperty(START_REGISTERS_ZERO_BASED, "1"));
    }

    @Override
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        result.add(START_REGISTERS_ZERO_BASED);
        result.add(CONNECTION);
        return result;
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new VLM20RegisterFactory(this));
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:02:17 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            AbstractRegister register = getRegisterFactory().findRegister(obisCode);
            Object value = register.value();
            if (value instanceof BigDecimal) {
                return new RegisterValue(obisCode, new Quantity((BigDecimal) value, register.getUnit()));
            } else if (value instanceof String) {
                return new RegisterValue(obisCode, (String) value);
            }  else if (value instanceof ReadStatuses) {
                ReadStatuses readStatuses = (ReadStatuses) value;
                byte[] statuses = readStatuses.getStatuses();

                Quantity quantity = new Quantity(statuses[0] & 0x01, Unit.getUndefined());
                String hexRepresentation = quantity.intValue() == 1 ? "On" : "Off";
                return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, hexRepresentation);
            }
            throw new NoSuchRegisterException();
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();

        MessageCategorySpec cat = new MessageCategorySpec("Modbus general messages");
        MessageSpec msgSpec = addBasicMsg("Write single coil", "WriteSingleCoil", false);
        cat.addMessageSpec(msgSpec);
        categories.add(cat);
        return categories;
    }

    private void validateAndSetStartRegistesZeroBasedFlag(String zeroBasedFlag) {
        startRegistersZeroBased = ProtocolTools.getBooleanFromString(zeroBasedFlag);
    }

    public boolean isStartRegistersZeroBased() {
        return startRegistersZeroBased;
    }

    static public void main(String[] args) {
        try {
            // ********************** Dialer **********************
            Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM5");
            dialer.getSerialCommunicationChannel().setParams(9600,
                    SerialCommunicationChannel.DATABITS_8,
                    SerialCommunicationChannel.PARITY_NONE,
                    SerialCommunicationChannel.STOPBITS_1);
            dialer.connect();

            // ********************** Properties **********************
            Properties properties = new Properties();
//            properties.setProperty("ProfileInterval", "900");
            //properties.setProperty(MeterProtocol.NODEID,"0");
            properties.setProperty(MeterProtocol.ADDRESS,"1");
            properties.setProperty("HalfDuplex", "1");
//            properties.setProperty(CONNECTION, "1");
//            properties.setProperty("PhysicalLayer", "1");
//            properties.setProperty(START_REGISTERS_ZERO_BASED, "0");

            // ********************** FP93B modbus **********************
            VLM20 protocol = new VLM20();

            protocol.setProperties(properties);
            protocol.setHalfDuplexController(dialer.getHalfDuplexController());
            protocol.init(dialer.getInputStream(), dialer.getOutputStream(), TimeZone.getTimeZone("GMT"), Logger.getLogger("name"));
            protocol.connect();


//            System.out.println(protocol.getClass().getName() + " Protocol Time=" + protocol.getTime());
//
////            RequestData requestData = new RequestData(7);
////            ResponseData responseData = protocol.getModbusConnection().sendRequest(requestData);
////            System.out.println(responseData);
//
            List<String> obisCodes= new ArrayList<>();

//            obisCodes.add("0.0.96.0.1.255");
            obisCodes.add("7.0.96.5.1.255");
            obisCodes.add("7.0.96.5.0.255");
            obisCodes.add("7.1.96.5.1.255");
            obisCodes.add("7.1.96.5.0.255");
            obisCodes.add("7.0.61.0.1.255");
            obisCodes.add("7.0.61.0.0.255");
            obisCodes.add("7.0.1.0.2.255");
            obisCodes.add("7.0.1.0.0.255");
            obisCodes.add("7.0.42.0.1.255");
            obisCodes.add("7.0.42.0.0.255");
            obisCodes.add("7.0.41.0.1.255");
            obisCodes.add("7.1.41.0.1.255");
            obisCodes.add("7.0.41.0.0.255");
            obisCodes.add("7.0.44.0.1.255");
            obisCodes.add("7.0.45.0.1.255");
            obisCodes.add("7.0.45.0.0.255");
            obisCodes.add("7.0.0.13.8.255");
            obisCodes.add("7.0.52.0.1.255");
            obisCodes.add("1.0.0.6.2.255");
            obisCodes.add("1.0.0.6.3.255");
            obisCodes.add("1.0.0.6.4.255");
            obisCodes.add("1.0.0.6.5.255");
            obisCodes.add("7.0.31.0.1.255");
            obisCodes.add("7.0.31.0.0.255");
            obisCodes.add("0.0.97.98.21.255");
            obisCodes.add("0.0.97.98.22.255");
            obisCodes.add("0.0.97.98.23.255");

            printOutReadings(protocol, obisCodes);
//            int on = 65280;
//            int off = 0;
////            protocol.getRegisterFactory().findRegister(ObisCode.fromString("0.0.97.98.23.255")).getWriteSingleCoil(on);
//            System.out.println(protocol.readRegister(ObisCode.fromString("0.0.96.10.2.255")));
////            System.out.println(protocol.getRegistersInfo(0));
            System.out.println(protocol.getRegistersInfo(1));

        protocol.disconnect();

        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static void printOutReadings(VLM20 protocol, List<String> obisCodes) throws IOException {
        for(String obis: obisCodes){
            System.out.println("---------"+protocol.translateRegister(ObisCode.fromString(obis))+"---------");
            System.out.println(protocol.readRegister(ObisCode.fromString(obis)));
            System.out.println("Unit = "+protocol.getRegisterFactory().findRegister(ObisCode.fromString(obis)).getUnit());
            System.out.println("");
        }
    }
}
