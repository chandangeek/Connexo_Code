package com.energyict.protocolimpl.modbus.socomec.countis.e44;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.socomec.countis.e44.profile.ProfileBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * ProtocolImplementation for the Socomec Countis E44 protocol
 * <p/>
 * <b>Manufacturor description:</b>
 * The COUNTIS E44 is a three-phase energy meter (both active and reactive)
 * communicating via an RS485 link using JBUS/MODBUS protocol.
 *
 * @author sva
 * @since 8/10/2014 - 9:58
 */
public class E44 extends Modbus {

    private static final String CONNECTION = "Connection";

    private ProfileBuilder profileBuilder;

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    protected List doTheGetOptionalKeys() {
        List result = new ArrayList();
        result.add(CONNECTION);
        return result;
    }

    @Override
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeResponseTimeout(Integer.parseInt(properties.getProperty("ResponseTimeout", "400").trim()));
    }

    @Override
    protected void initRegisterFactory() {
        super.setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public Date getTime() throws IOException {
        return DateTime.parseDateTime(getDateTimeRegister().values()).getMeterCalender().getTime();
    }

    @Override
    public void setTime() throws IOException {
        getDateTimeRegister().getWriteMultipleRegisters(DateTime.getCurrentDate());
    }

    private AbstractRegister getDateTimeRegister() throws IOException {
        return getRegisterFactory().findRegister(RegisterFactory.CurrentDateTime);
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
            }
            throw new NoSuchRegisterException();
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            String jbusTableVersion = Integer.toString(((BigDecimal) getRegisterFactory().findRegister(RegisterFactory.JbusTableVersion).value()).intValue());
            String productSoftwareVersion = Integer.toString(((BigDecimal) getRegisterFactory().findRegister(RegisterFactory.ProductSoftwareVersion).value()).intValue());
            return "JBUS Table Version: " + jbusTableVersion + ", Product Software Version: " + productSoftwareVersion;
        } catch (ModbusException e) {
            getLogger().warning("Failed to read firmware version" + " - " + e.getMessage());
            throw new UnsupportedException();
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
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getProfileBuilder().getProfileData(from, to, includeEvents);
    }

    public ProfileBuilder getProfileBuilder() {
        if (this.profileBuilder == null) {
            this.profileBuilder = new ProfileBuilder(this);
        }
        return this.profileBuilder;
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-05-24 10:57:07 +0300 (Tue, 24 May 2016)$";
    }
}