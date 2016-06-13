package com.energyict.protocolimpl.modbus.schneider.powerlogic;


import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadStatuses;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.profile.ProfileBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

public class PM5561 extends PM5560 implements SerialNumberSupport {
    private static final String APPLY_CTRATIO = "ApplyCTRatio";
    public static final int SETCLOCK			= 0x0104;
    private ProfileBuilder profileBuilder;
    private boolean applyCtRatio;

    @Override
    public String getSerialNumber() {
        try {
            return getRegisterFactory().findRegister(PM5561RegisterFactory.SERIAL_NUMBER).value().toString();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        applyCtRatio = Integer.parseInt(properties.getProperty(APPLY_CTRATIO, "0").trim()) == 1;
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


    public boolean isApplyCtRatio() {
        return applyCtRatio;
    }
}
