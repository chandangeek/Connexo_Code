/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.as3000.readers.register;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultRegister;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class AS3000BillingRegister<T, K extends AbstractDlmsProtocol> extends DefaultRegister<T, K> {
    private static String DATE = "1.1.0.1.2.*";
    private static String TIME = "1.1.0.1.3.*";

    public AS3000BillingRegister(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder) {
        super(matcher, collectedRegisterBuilder);
    }

    @Override
    public CollectedRegister read(K protocol, OfflineRegister offlineRegister) {
        try {
            ObisCode deviceObisCode = offlineRegister.getObisCode();
            // ObisCode  = super.map(cxoObisCode);
            UniversalObject universalObject = protocol.getDlmsSession().getMeterConfig().findObject(deviceObisCode);
            ComposedRegister composedRegister = getComposedRegister(universalObject, deviceObisCode);
            ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSession().getProperties().isBulkRequest(), composedRegister.getAllAttributes());
            Unit unit = getUnit(composedRegister, composedCosemObject);
            Date captureTime = readDate(protocol, Integer.toString(offlineRegister.getObisCode().getF()));
            AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, map(attributeValue, unit, captureTime, offlineRegister));
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }

    private Date readDate(K protocol, String f) throws IOException {
        Date date;
        ObisCode dateObisCode = ObisCode.fromString(DATE.replace("*", f));
        ObisCode timeObisCode = ObisCode.fromString(TIME.replace("*", f));
        UniversalObject universalObjectDate = protocol.getDlmsSession().getMeterConfig().findObject(dateObisCode);
        UniversalObject universalObjectTime = protocol.getDlmsSession().getMeterConfig().findObject(timeObisCode);
        ComposedRegister composedRegisterDate = getComposedRegister(universalObjectDate, dateObisCode);
        ComposedRegister composedRegisterTime = getComposedRegister(universalObjectTime, timeObisCode);
        ComposedCosemObject composedCosemObjectDate = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSession().getProperties().isBulkRequest(), composedRegisterDate.getAllAttributes());
        ComposedCosemObject composedCosemObjectTime = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSession().getProperties().isBulkRequest(), composedRegisterTime.getAllAttributes());
        AbstractDataType attributeValueDate = composedCosemObjectDate.getAttribute(composedRegisterDate.getRegisterValueAttribute());
        AbstractDataType attributeValueTime = composedCosemObjectTime.getAttribute(composedRegisterTime.getRegisterValueAttribute());
        Calendar calendar = Calendar.getInstance(protocol.getTimeZone());
        byte[] dateArray = attributeValueDate.getContentByteArray();
        byte[] timeArray = attributeValueTime.getContentByteArray();
        calendar.set(ByteBuffer.wrap(Arrays.copyOfRange(dateArray, 0, 2)).getShort(),
                dateArray[2] - 1,
                dateArray[3],
                timeArray[0],
                timeArray[1],
                0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        return date;
    }
}
