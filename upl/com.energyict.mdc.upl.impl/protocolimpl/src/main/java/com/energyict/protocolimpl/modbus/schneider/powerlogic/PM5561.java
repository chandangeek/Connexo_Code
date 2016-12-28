package com.energyict.protocolimpl.modbus.schneider.powerlogic;


import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCodeFactory;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadHoldingRegistersRequest;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadStatuses;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.profile.ProfileBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

public class PM5561 extends PM5560 implements SerialNumberSupport {
    private ProfileBuilder profileBuilder;
    private String timeZone = "UTC";

    public PM5561(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getSerialNumber() {
        try {
            return getRegisterFactory().findRegister(PM5561RegisterFactory.SERIAL_NUMBER).value().toString();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        setTimeZone(properties.getTypedProperty(LegacyProtocolProperties.DEVICE_TIMEZONE_PROPERTY_NAME, "UTC"));
        setConnectionMode(Integer.parseInt(properties.getTypedProperty("Connection", "1").trim()));
        setInfoTypePhysicalLayer(Integer.parseInt(properties.getTypedProperty("PhysicalLayer", "1").trim()));
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty("Timeout","10000").trim()));
        setForcedDelay(Integer.parseInt(properties.getTypedProperty("ForcedDelay","30").trim()));
    }

    private void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new PM5561RegisterFactory(this));
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            AbstractRegister register = getRegisterFactory().findRegister(obisCode);
            Object value = register.value();
            if (value instanceof BigDecimal) {
                return new RegisterValue(obisCode, new Quantity((BigDecimal) value, register.getUnit()));
            } else if (value instanceof Double){
                return new RegisterValue(obisCode, new Quantity((Double) value, register.getUnit()));
            }
            else if (value instanceof String) {
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
    public int getNumberOfChannels() throws IOException {
        return getProfileBuilder().getNumberOfChannels();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getProfileBuilder().getProfileInterval();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return super.getProfileData(includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        if(getProfileBuilder().isSupported()) {
            return getProfileData(lastReading, new Date(), includeEvents);
        }else{
            throw new UnsupportedException("ProfileData is not supported by the meter.");
        }
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if(getProfileBuilder().isSupported()) {
            return getProfileBuilder().getProfileData(from, to, includeEvents);
        }else{
            throw new UnsupportedException("ProfileData is not supported by the meter.");
        }
    }

    public ProfileBuilder getProfileBuilder() {
        if (this.profileBuilder == null) {
            this.profileBuilder = new ProfileBuilder(this);
        }
        return this.profileBuilder;
    }

    /**
     * Setter for the {@link ModbusConnection}
     *
     * @param modbusConnection - the used modbusConnection
     */
    public void setModbusConnection(ModbusConnection modbusConnection){
        this.modbusConnection = modbusConnection;
    }

    /**
     * Setter for the {@link Logger}
     *
     * @param logger - the desired logger
     */
    public void setLogger(Logger logger){
        setAbstractLogger(logger);
    }

    /**
     * The protocol version date
     */
    public String getProtocolVersion() {
        return "$Date: 2016-07-19 10:58:46 +0300 (Tu, 19 Jul 2016)$";
    }

    @Override
    public TimeZone getTimeZone(){
        return TimeZone.getTimeZone(timeZone);
    }

    @Override
    public Date getTime() throws IOException {
        return new DateTime().parseDateTime(getDateTimeRegister().values()).getMeterCalender().getTime();
    }

    @Override
    public void setTime() throws IOException {
        Calendar instTime = Calendar.getInstance( gettimeZone() );
        FunctionCodeFactory fcf = new FunctionCodeFactory(this);
        ReadHoldingRegistersRequest registersRequest = fcf.getReadHoldingRegistersRequest(getCommandSemaphoreRegister().getReg(), 1);
        int result[] = registersRequest.getRegisters();
        String sResult = Integer.toString(result[0], 16);
        byte[] rawDate = getBytesFromDate(instTime.getTime(), sResult, gettimeZone());
        fcf.getWriteMultipleRegisters(getChangeDateTimeRegister().getReg(), 9, rawDate);
        ReadHoldingRegistersRequest commandParameterRequest = fcf.getReadHoldingRegistersRequest(getCommandParameterRegister().getReg(), 1);
        commandParameterRequest.getRegisters();
        if(sResult.length() == 4) {
            fcf.getWriteMultipleRegisters(getCommandSemaphoreRegister().getReg(), 1,
                    new byte[]{(byte) Integer.parseInt(sResult.substring(0, 2), 16),
                            (byte) Integer.parseInt(sResult.substring(2, 4), 16)});
        }
    }


    public static byte[] getBytesFromDate(Date date, String commandCode, TimeZone timezone) {
        Calendar calendar = Calendar.getInstance(timezone);
        calendar.setTime(date);
        byte[] returnValue = new byte[18];
        int index = 0;
        returnValue[index++] = 0x03;
        returnValue[index++] = (byte) 0xeb;
        if(commandCode.length() == 4) {
            returnValue[index++] = (byte) Integer.parseInt(commandCode.substring(0, 2), 16);
            returnValue[index++] = (byte) Integer.parseInt(commandCode.substring(2, 4), 16);
        }else{
            returnValue[index++] = (byte) Integer.parseInt(commandCode.substring(0, 2), 16);
            returnValue[index++] = (byte) Integer.parseInt(commandCode.substring(2, 3), 16);
        }
        String year = Integer.toString(calendar.get(Calendar.YEAR), 16);
        returnValue[index++] = (byte) Integer.parseInt(year.substring(0,1), 16);
        returnValue[index++] = (byte) Integer.parseInt(year.substring(1,3), 16);
        String month = Integer.toString(calendar.get(Calendar.MONTH) + 1);
        returnValue[index++] = 0x0;
        returnValue[index++] = Byte.parseByte(month.length() == 2? month.substring(0, 2): month.substring(0, 1));
        String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        returnValue[index++] = 0x0;
        returnValue[index++] = Byte.parseByte(day.length() == 2? day.substring(0, 2): day.substring(0, 1));
        String hours = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        returnValue[index++] = 0x0;
        returnValue[index++] = Byte.parseByte(hours.length() == 2? hours.substring(0, 2): hours.substring(0, 1));
        String minutes = Integer.toString(calendar.get(Calendar.MINUTE));
        returnValue[index++] = 0x0;
        returnValue[index++] = Byte.parseByte(minutes.length() == 2? minutes.substring(0, 2): minutes.substring(0, 1));
        String seconds = Integer.toString(calendar.get(Calendar.SECOND));
        returnValue[index++] = 0x0;
        returnValue[index++] = Byte.parseByte(seconds.length() == 2? seconds.substring(0, 2): seconds.substring(0, 1));
        returnValue[index++] = 0x0;
        returnValue[index++] = 0x0;
        return returnValue;
    }

    private AbstractRegister getDateTimeRegister() throws IOException {
        return getRegisterFactory().findRegister(PM5561RegisterFactory.CurrentDateTime);
    }

    private AbstractRegister getChangeDateTimeRegister() throws IOException {
        return getRegisterFactory().findRegister(PM5561RegisterFactory.ChangeDateTime);
    }

    private AbstractRegister getCommandSemaphoreRegister() throws IOException {
        return getRegisterFactory().findRegister(PM5561RegisterFactory.CommandSemaphore);
    }

    private AbstractRegister getCommandParameterRegister() throws IOException {
        return getRegisterFactory().findRegister(PM5561RegisterFactory.CommandParameter);
    }
}
