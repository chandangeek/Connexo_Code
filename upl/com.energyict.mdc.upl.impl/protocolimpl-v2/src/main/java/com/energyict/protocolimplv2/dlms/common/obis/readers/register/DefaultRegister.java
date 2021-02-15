package com.energyict.protocolimplv2.dlms.common.obis.readers.register;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;

import java.util.Date;

public class DefaultRegister<T, K extends AbstractDlmsProtocol> extends AbstractRegisterClass<T, K> {

    public DefaultRegister(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder) {
        super(matcher, collectedRegisterBuilder);
    }

    @Override
    protected RegisterValue map(AbstractDataType attributeValue, Unit unit, Date captureTime, OfflineRegister offlineRegister)  {
        return  new RegisterValue(offlineRegister, new Quantity(attributeValue.toBigDecimal(), unit), captureTime);
    }

    @Override
    protected ComposedRegister getComposedRegister(UniversalObject universalObject, ObisCode obisCode) {
        ComposedRegister composedRegister = new ComposedRegister();
        DLMSAttribute valueAttribute = new DLMSAttribute(obisCode, RegisterAttributes.VALUE.getAttributeNumber(), universalObject.getClassID());
        DLMSAttribute scalerUnitAttribute = new DLMSAttribute(obisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), universalObject.getClassID());
        composedRegister.setRegisterValue(valueAttribute);
        composedRegister.setRegisterUnit(scalerUnitAttribute);
        return composedRegister;
    }

}
