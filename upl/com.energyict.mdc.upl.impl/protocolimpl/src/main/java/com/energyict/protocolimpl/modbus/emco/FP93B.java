package com.energyict.protocolimpl.modbus.emco;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.RegisterValue;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

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

    private static void printOutReadings(FP93B protocol, List<String> obisCodes) throws IOException {
        for(String obis: obisCodes){
            System.out.println("---------"+protocol.translateRegister(ObisCode.fromString(obis))+"---------");
            System.out.println(protocol.readRegister(ObisCode.fromString(obis)));
            System.out.println("");
        }
    }

}
