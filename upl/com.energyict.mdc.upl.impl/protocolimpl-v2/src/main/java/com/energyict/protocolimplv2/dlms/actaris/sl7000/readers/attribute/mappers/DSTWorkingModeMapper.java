package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.mappers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;

import java.math.BigDecimal;
import java.util.Date;

public class DSTWorkingModeMapper implements AttributeMapper<AbstractDataType> {

    @Override
    public RegisterValue map(AbstractDataType data, OfflineRegister offlineRegister) throws MappingException {
        BigDecimal bigDecimal = data.toBigDecimal();
        if (bigDecimal != null) {
            return map(offlineRegister, bigDecimal.longValue());
        }
        throw new MappingException("Data, getValue(), invalid data value type...");
    }

    @Override
    public Class<AbstractDataType> dataType() {
        return AbstractDataType.class;
    }

    private RegisterValue map(OfflineRegister offlineRegister, long mode) {
        String text;
        if (mode == 0) {
            text = "DST switching disabled.";
        } else if (mode == 1) {
            text = "DST switching enabled - generic mode";
        } else if (mode == 2) {
            text = "DST switching enabled - programmed mode";
        } else if (mode == 3) {
            text = "DST switching enabled - generic mode with season";
        } else if (mode == 4) {
            text = "DST switching enabled - programmed mode with season";
        } else {
            text = "Invalid mode";
        }
        return new RegisterValue(offlineRegister, new Quantity(mode, Unit.getUndefined()), null, null, null, new Date(), 0, text);

    }
}
