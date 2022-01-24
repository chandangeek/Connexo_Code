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

public abstract class AbstractRegisterClass<T, K extends  AbstractDlmsProtocol> extends AbstractObisReader<CollectedRegister, OfflineRegister, T, K> {

    protected final CollectedRegisterBuilder collectedRegisterBuilder;
    private final boolean readCaptureTime;

    public AbstractRegisterClass(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder) {
        super(matcher);
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.readCaptureTime = false;
    }

    public AbstractRegisterClass(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder, boolean readCaptureTime) {
        super(matcher);
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.readCaptureTime = readCaptureTime;
    }

    protected abstract RegisterValue map(AbstractDataType attributeValue, Unit unit, Date captureTime, OfflineRegister offlineRegister) throws MappingException;

    protected abstract ComposedRegister getComposedRegister(UniversalObject universalObject, ObisCode obisCode);

    @Override
    public CollectedRegister read(K protocol, OfflineRegister offlineRegister) {
        try {
            ObisCode cxoObisCode = offlineRegister.getObisCode();
            ObisCode deviceObisCode = super.map(cxoObisCode);
            UniversalObject universalObject = protocol.getDlmsSession().getMeterConfig().findObject(deviceObisCode);
            ComposedRegister composedRegister = getComposedRegister(universalObject, deviceObisCode);
            ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSession().getProperties().isBulkRequest(), composedRegister.getAllAttributes());
            Unit unit = getUnit(composedRegister, composedCosemObject);
            Date captureTime = getDate(composedRegister, composedCosemObject, protocol.getDlmsSession());
            AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, map(attributeValue, unit, captureTime, offlineRegister));
        } catch (IOException | MappingException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }

    private Date getDate(ComposedRegister composedRegister, ComposedCosemObject composedCosemObject, DlmsSession dlmsSession) throws IOException {
        Date captureTime = null;
        if (readCaptureTime && composedRegister.getRegisterCaptureTime() != null) {
            AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
            captureTime = captureTimeOctetString.getOctetString().getDateTime(dlmsSession.getTimeZone()).getValue().getTime();
        }
        return captureTime;
    }

    protected Unit getUnit(ComposedRegister composedRegister, ComposedCosemObject composedCosemObject) throws IOException {
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        if (composedRegister.getRegisterUnitAttribute() != null) {
            unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
        }
        return unit;
    }

}
