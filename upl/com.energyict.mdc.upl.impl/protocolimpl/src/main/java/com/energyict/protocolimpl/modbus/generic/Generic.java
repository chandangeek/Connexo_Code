package com.energyict.protocolimpl.modbus.generic;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
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
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadStatuses;
import com.energyict.protocolimpl.modbus.core.functioncode.ReportSlaveId;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>A generic implementation of modbus protocol <br></br>
 * The modbus register mapping is not hard-coded, but is fully configurable in EIServer - by means of register obisCode.
 * </p>
 *
 * Register obisCode format: <i>AA.BB.CC.DD.EE.255</i>
 * <br></br>
 * <ul>
 * <li>AA = Register ID</li>
 * <li>BB = Fucntion</li>
 * <li>CC.DD = Start register</li>
 * <li>EE = Data type</li>
 * </ul>
 *
 * @author sva
 * @since 18/11/13 - 14:06
 */
public class Generic extends Modbus {

    private static final String START_REGISTERS = "StartRegisters";
    private static final String START_REGISTERS_ZERO_BASED = "StartRegistersZeroBased";

    private boolean startRegistersZeroBased;
    private Map<Integer, Integer> customStartRegisterMap = new HashMap<>();

    public Generic(PropertySpecService propertySpecService) {
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
        propertySpecs.add(this.stringSpec(START_REGISTERS, false));
        propertySpecs.add(this.stringSpec(START_REGISTERS_ZERO_BASED, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        validateAndSetCustomStartRegisterMap(properties.getTypedProperty(START_REGISTERS));
        validateAndSetStartRegistesZeroBasedFlag(properties.getTypedProperty(START_REGISTERS_ZERO_BASED, "1"));
    }

    private void validateAndSetCustomStartRegisterMap(String registerMap) throws InvalidPropertyException {
        if (registerMap == null || "".equals(registerMap)) {
            return;
        } else {
            registerMap = registerMap.trim();
            String[] split = registerMap.split(";");
            for (int i = 0; i < split.length; i++) {
                String[] innerSplit = split[i].split("-");
                if (innerSplit.length != 2) {
                    throw new InvalidPropertyException("Value of custom property StartRegisters is not in the correct format.");
                }

                try {
                    Integer obisStartRegister = new Integer(innerSplit[0].trim());
                    Integer overruleStartRegister = new Integer(innerSplit[1].trim());

                    customStartRegisterMap.put(obisStartRegister, overruleStartRegister);
                } catch (NumberFormatException e) {
                    throw new InvalidPropertyException("Failed to parse custom property StartRegisters, due to a NumberFormatException.");
                }
            }
        }
    }

    private void validateAndSetStartRegistesZeroBasedFlag(String zeroBasedFlag) {
        startRegistersZeroBased = ProtocolTools.getBooleanFromString(zeroBasedFlag);
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            Object value = getRegisterFactory().findRegister(obisCode).value();
            if (value instanceof BigDecimal) {
                return new RegisterValue(obisCode, new Quantity((BigDecimal) value, Unit.getUndefined()));
            } else if (value instanceof String) {
                return new RegisterValue(obisCode, (String) value);
            } else if (value instanceof ReportSlaveId) {
                ReportSlaveId rsi = (ReportSlaveId) value;
                return new RegisterValue(obisCode,
                        new Quantity(rsi.getSlaveId(), Unit.getUndefined()), null, null, null, new Date(), 0, Boolean.toString(rsi.isRun()));
            }  else if (value instanceof ReadStatuses) {
                ReadStatuses readStatuses = (ReadStatuses) value;
                byte[] statuses = readStatuses.getStatuses();
                Quantity quantity = new Quantity(ProtocolTools.getUnsignedBigIntegerFromBytes(statuses).longValue(), Unit.getUndefined());
                String hexRepresentation = "0x" + ProtocolTools.getHexStringFromBytes(statuses, "");
                return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, hexRepresentation);
            }
            throw new NoSuchRegisterException();
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

    Map<Integer, Integer> getCustomStartRegisterMap() {
        return customStartRegisterMap;
    }

    boolean isStartRegistersZeroBased() {
        return startRegistersZeroBased;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-04-21 13:49:04 +0200 (Tue, 21 Apr 2015) $";
    }

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {
        DiscoverResult discover = new DiscoverResult();
        return discover;
    }

}