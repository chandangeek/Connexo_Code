package com.energyict.protocolimpl.modbus.generic;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.generic.common.DataTypeSelector;
import com.energyict.protocolimpl.modbus.generic.common.Function;

/**
 * @author sva
 * @since 18/11/13 - 14:36
 */
public class RegisterDefinition {

    private int registerID;
    private DataTypeSelector dataTypeSelector;
    private Function function;

    public RegisterDefinition(ObisCode obisCode) {
        setFunction(obisCode.getB());
        setRegister(obisCode.getC(), obisCode.getD());
        setDataTypeSelector(obisCode.getE());
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public void setFunction(int functionCode) {
        this.function = Function.getFunction(functionCode);
    }

    public int getRegister() {
        return registerID;
    }

    public void setRegister(int registerID) {
        this.registerID = registerID;
    }

    public void setRegister(int highPart, int lowPart) {
        this.registerID = Integer.parseInt(String.format("%02x", highPart) + String.format("%02x", lowPart), 16);
    }

    public DataTypeSelector getDataTypeSelector() {
        return dataTypeSelector;
    }

    public void setDataTypeSelector(DataTypeSelector dataTypeSelector) {
        this.dataTypeSelector = dataTypeSelector;
    }

    public void setDataTypeSelector(int dataTypeSelectorCode) {
        this.dataTypeSelector = DataTypeSelector.getDataTypeSelector(dataTypeSelectorCode);
    }
}
