package com.energyict.protocolimpl.modbus.spiraxsarco;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
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

/**
 * Created by cisac on 11/16/2015.
 */
public class VLM20 extends Modbus{

    private static final String START_REGISTERS_ZERO_BASED = "StartRegistersZeroBased";
    private boolean startRegistersZeroBased;

    public VLM20(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doTheConnect() throws IOException {
    }

    @Override
    protected void doTheDisConnect() throws IOException {
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.stringSpec(START_REGISTERS_ZERO_BASED, false));
        return propertySpecs;
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        validateAndSetStartRegistesZeroBasedFlag(properties.getTypedProperty(START_REGISTERS_ZERO_BASED, "1"));
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

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();

        MessageCategorySpec cat = new MessageCategorySpec("Modbus general messages");
        MessageSpec msgSpec = addBasicMsg("Write single coil", "WriteSingleCoil", false);
        cat.addMessageSpec(msgSpec);
        categories.add(cat);
        return categories;
    }

    private void validateAndSetStartRegistesZeroBasedFlag(String zeroBasedFlag) {
        startRegistersZeroBased = ProtocolTools.getBooleanFromString(zeroBasedFlag);
    }

    boolean isStartRegistersZeroBased() {
        return startRegistersZeroBased;
    }

}