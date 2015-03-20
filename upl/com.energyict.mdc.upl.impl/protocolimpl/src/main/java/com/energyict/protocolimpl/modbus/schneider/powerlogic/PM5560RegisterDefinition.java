package com.energyict.protocolimpl.modbus.schneider.powerlogic;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.generic.RegisterDefinition;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.common.PM5560DataTypeSelector;

/**
 * @author sva
 * @since 18/03/2015 - 15:52
 */
public class PM5560RegisterDefinition extends RegisterDefinition {

    public PM5560RegisterDefinition(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public void setDataTypeSelector(int dataTypeSelectorCode) {
        super.setDataTypeSelector(PM5560DataTypeSelector.getDataTypeSelector(dataTypeSelectorCode));
    }
}