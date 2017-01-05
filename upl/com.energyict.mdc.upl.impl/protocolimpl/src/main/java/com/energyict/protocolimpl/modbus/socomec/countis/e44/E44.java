package com.energyict.protocolimpl.modbus.socomec.countis.e44;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
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

    private static final String APPLY_CTRATIO = "ApplyCTRatio";
    private boolean applyCtRatio = false;

    private ProfileBuilder profileBuilder;

    public E44(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec(APPLY_CTRATIO, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeResponseTimeout(Integer.parseInt(properties.getTypedProperty(PK_RESPONSE_TIMEOUT, "400").trim()));
        applyCtRatio = Integer.parseInt(properties.getTypedProperty(APPLY_CTRATIO, "0").trim()) == 1;
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

    private ProfileBuilder getProfileBuilder() {
        if (this.profileBuilder == null) {
            this.profileBuilder = new ProfileBuilder(this);
        }
        return this.profileBuilder;
    }

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-05-26 09:53:22 +0300 (Thu, 26 May 2016)$";
    }

    public boolean isApplyCtRatio() {
        return applyCtRatio;
    }

}