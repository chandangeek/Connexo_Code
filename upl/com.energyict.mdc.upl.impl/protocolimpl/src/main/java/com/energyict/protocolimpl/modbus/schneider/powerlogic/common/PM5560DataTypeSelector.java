package com.energyict.protocolimpl.modbus.schneider.powerlogic.common;

import com.energyict.protocolimpl.modbus.generic.common.DataType;
import com.energyict.protocolimpl.modbus.generic.common.DataTypeSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 18/03/2015 - 11:32
 */
public class PM5560DataTypeSelector extends DataTypeSelector {

    private static final List<DataType> pm5560DataTypes = new ArrayList<DataType>();
    public static final DataType POWER_FACTOR_DATA_TYPE = new DataType("POWER_FACTOR", 11, 2);

    static {
        pm5560DataTypes.addAll(DataTypeSelector.dataTypes);
        pm5560DataTypes.remove(DataTypeSelector.MODULO10_32_BIT_DATA_TYPE);
        pm5560DataTypes.remove(DataTypeSelector.MODULO10_64_BIT_DATA_TYPE);
        pm5560DataTypes.add(POWER_FACTOR_DATA_TYPE);
    }

    protected PM5560DataTypeSelector(int dataTypeSelector) {
        super(dataTypeSelector);
    }

    @Override
    protected DataType searchCorrespondingDataType(int dataTypeSelector) {
        for (DataType dataType : pm5560DataTypes) {
            if (dataType.getDataTypeCode() == dataTypeSelector) {
                return dataType;
            }
        }
        return UNKNOWN_DATA_TYPE;
    }

    public static DataTypeSelector getDataTypeSelector(int dataTypeSelectorCode) {
        return new PM5560DataTypeSelector(dataTypeSelectorCode);
    }
}