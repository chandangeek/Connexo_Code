package com.energyict.protocolimpl.modbus.emco;

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
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadStatuses;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by cisac on 11/5/2015.
 */
public class FP93B extends Modbus {

    private static final String START_REGISTERS_ZERO_BASED = "StartRegistersZeroBased";
    private static final String CONNECTION = "Connection";
    private static final String TIMEZONE = "TimeZone";

    private boolean startRegistersZeroBased;
    private String timeZone;

    @Override
    protected void doTheConnect() throws IOException {

    }

    @Override
    protected void doTheDisConnect() throws IOException {

    }

    @Override
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setTimeZone(properties.getProperty(TIMEZONE, "GMT"));
        validateAndSetStartRegistesZeroBasedFlag(properties.getProperty(START_REGISTERS_ZERO_BASED, "1"));
    }

    @Override
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        result.add(START_REGISTERS_ZERO_BASED);
        result.add(CONNECTION);
        result.add(TIMEZONE);
        return result;
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new FP93BRegisterFactory(this));
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:02:16 +0200 (Thu, 26 Nov 2015)$";
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            Object value = getRegisterFactory().findRegister(obisCode).value();
            if (value instanceof BigDecimal) {
                return new RegisterValue(obisCode, new Quantity((BigDecimal) value, Unit.getUndefined()));
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

    @Override
    protected List doGetMessageCategories(List theCategories) {
        MessageCategorySpec cat = new MessageCategorySpec("Modbus general messages");
        MessageSpec msgSpec = addBasicMsg("Write multiple coils", "WriteMultipleCoils", false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Write single coil", "WriteSingleCoil", false);
        cat.addMessageSpec(msgSpec);
        theCategories.add(cat);
        return theCategories;
    }

    @Override
    public Date getTime() throws IOException {
        return DateTime.parseDateTime(getDateTimeRegister().values(), getTimeZone()).getMeterCalender().getTime();
    }

    @Override
    public void setTime() throws IOException {
        getDateTimeRegister().getWriteMultipleRegisters(DateTime.getCurrentDate(getTimeZone()));
    }

    private AbstractRegister getDateTimeRegister() throws IOException {
        return getRegisterFactory().findRegister(FP93BRegisterFactory.CurrentDateTime);
    }

    private void validateAndSetStartRegistesZeroBasedFlag(String zeroBasedFlag) {
        startRegistersZeroBased = ProtocolTools.getBooleanFromString(zeroBasedFlag);
    }

    private void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public TimeZone getTimeZone(){
        return TimeZone.getTimeZone(timeZone);
    }

    public boolean isStartRegistersZeroBased() {
        return startRegistersZeroBased;
    }

    //used for easy test with serial connection
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
            properties.setProperty(TIMEZONE, "GMT+3:00");
            properties.setProperty("PhysicalLayer", "1");
//            properties.setProperty(START_REGISTERS_ZERO_BASED, "0");
//            properties.setProperty("RegisterOrderFloatingPoint", "0");

            // ********************** FP93B modbus **********************
            FP93B protocol = new FP93B();

            protocol.setProperties(properties);
            protocol.setHalfDuplexController(dialer.getHalfDuplexController());
            protocol.init(dialer.getInputStream(), dialer.getOutputStream(), TimeZone.getTimeZone("GMT"), Logger.getLogger("name"));
            protocol.connect();

            System.out.println(protocol.getClass().getName() + " Protocol Time=" + protocol.getTime());

            protocol.setTime();
            System.out.println("Protocol Time=" + protocol.getTime());

            List<String> obisCodes= new ArrayList<>();
//            obisCodes.add("6.0.10.0.1.255");
//            obisCodes.add("7.0.61.0.1.255");
//            obisCodes.add("7.0.1.0.1.255");
//            obisCodes.add("7.0.1.0.1.255");
//            obisCodes.add("7.0.41.0.1.255");
//            obisCodes.add("7.1.41.0.1.255");
//            obisCodes.add("7.2.41.0.1.255");
//            obisCodes.add("7.0.42.0.1.255");
//            obisCodes.add("7.0.42.0.2.255");
//            obisCodes.add("7.0.45.0.1.255");
//            obisCodes.add("5.0.0.4.1.255");
//            obisCodes.add("6.0.10.0.2.255");
//            obisCodes.add("7.0.61.0.2.255");
//            obisCodes.add("7.0.1.0.3.255");
//            obisCodes.add("7.0.1.0.4.255");
//            obisCodes.add("6.0.10.0.3.255");
//            obisCodes.add("7.0.61.0.3.255");
//            obisCodes.add("7.0.1.0.5.255");
//            obisCodes.add("7.0.1.0.6.255");
            obisCodes.add("0.0.97.98.21.255");
            obisCodes.add("0.0.97.98.22.255");
//            obisCodes.add("0.0.97.98.23.255");


            System.out.println(protocol.readRegister(ObisCode.fromString("0.0.1.0.3.255")));
            protocol.getRegisterFactory().findRegister(ObisCode.fromString("0.0.1.0.3.255")).getWriteSingleRegister(13);
            System.out.println(protocol.readRegister(ObisCode.fromString("0.0.1.0.3.255")));
            System.out.println("Protocol Time=" + protocol.getTime());

            printOutReadings(protocol, obisCodes);
//            int on = 65280;
//            int off = 0;
//            System.out.println(protocol.readRegister(ObisCode.fromString("0.0.97.98.1.255")));
//            protocol.getRegisterFactory().findRegister(ObisCode.fromString("0.12.97.97.0.255")).getWriteSingleCoil(on);
//            System.out.println(protocol.readRegister(ObisCode.fromString("0.0.97.98.1.255")));
//            int nrOfCoilsToChange = 4;
//            byte [] coilValues = new byte[nrOfCoilsToChange];
//            for(int i = 0; i < nrOfCoilsToChange; i++){
//                coilValues[i] = (byte)on;
//            }
//            protocol.getRegisterFactory().findRegister(ObisCode.fromString("0.12.97.97.0.255")).getWriteMultipleCoils(coilValues);
//            System.out.println(protocol.readRegister(ObisCode.fromString("0.0.97.98.1.255")));
//            System.out.println(protocol.readRegister(ObisCode.fromString("0.0.97.98.2.255")));
//            System.out.println(protocol.readRegister(ObisCode.fromString("0.0.97.98.3.255")));
//            System.out.println(protocol.getRegistersInfo(0));
            System.out.println(protocol.getRegistersInfo(1));

            protocol.disconnect();

        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static void printOutReadings(FP93B protocol, List<String> obisCodes) throws IOException {
        for(String obis: obisCodes){
            System.out.println("---------"+protocol.translateRegister(ObisCode.fromString(obis))+"---------");
            System.out.println(protocol.readRegister(ObisCode.fromString(obis)));
            System.out.println("");
        }
    }

}
