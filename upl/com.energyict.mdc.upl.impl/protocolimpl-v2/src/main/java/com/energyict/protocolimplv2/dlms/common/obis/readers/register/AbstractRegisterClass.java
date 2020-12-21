package com.energyict.protocolimplv2.dlms.common.obis.readers.register;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.AbstractObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

import java.io.IOException;
import java.util.Date;

public abstract class AbstractRegisterClass<T> extends AbstractObisReader<CollectedRegister, OfflineRegister, T> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;

    public AbstractRegisterClass(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder) {
        super(matcher);
        this.collectedRegisterBuilder = collectedRegisterBuilder;
    }

    protected abstract RegisterValue map(AbstractDataType attributeValue, Unit unit, Date captureTime, ObisCode obisCode) throws MappingException;

    protected abstract ComposedRegister getComposedRegister(UniversalObject universalObject, ObisCode obisCode);

    @Override
    public CollectedRegister read(AbstractDlmsProtocol dlmsProtocol, OfflineRegister offlineRegister) {
        try {
            ObisCode cxoObisCode = offlineRegister.getObisCode();
            ObisCode deviceObisCode = super.map(cxoObisCode);
            UniversalObject universalObject = dlmsProtocol.getDlmsSession().getMeterConfig().findObject(deviceObisCode);
            ComposedRegister composedRegister = getComposedRegister(universalObject, deviceObisCode);
            ComposedCosemObject composedCosemObject = new ComposedCosemObject(dlmsProtocol.getDlmsSession(), dlmsProtocol.getDlmsSession().getProperties().isBulkRequest(), composedRegister.getAllAttributes());
            Unit unit = getUnit(composedRegister, composedCosemObject);
            Date captureTime = getDate(composedRegister, composedCosemObject, dlmsProtocol.getDlmsSession());
            AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, map(attributeValue, unit, captureTime, cxoObisCode));
        } catch (IOException | MappingException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }

    private Date getDate(ComposedRegister composedRegister, ComposedCosemObject composedCosemObject, DlmsSession dlmsSession) throws IOException {
        Date captureTime = null;
        if (composedRegister.getRegisterCaptureTime() != null) {
            AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
            captureTime = captureTimeOctetString.getOctetString().getDateTime(dlmsSession.getTimeZone()).getValue().getTime();
        }
        return captureTime;
    }

    private Unit getUnit(ComposedRegister composedRegister, ComposedCosemObject composedCosemObject) throws IOException {
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        if (composedRegister.getRegisterUnitAttribute() != null) {
            unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
        }
        return unit;
    }

}
