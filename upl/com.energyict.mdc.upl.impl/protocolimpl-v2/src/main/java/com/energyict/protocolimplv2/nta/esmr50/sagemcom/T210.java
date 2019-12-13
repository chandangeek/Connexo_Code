package com.energyict.protocolimplv2.nta.esmr50.sagemcom;

import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

public class T210 extends ESMR50Protocol implements SerialNumberSupport {


    public T210(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService,
                NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor,
                TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor,
                LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor,
                DeviceMasterDataExtractor deviceMasterDataExtractor) {
        super(collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, messageFileExtractor,
                calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor,
                deviceMasterDataExtractor);
    }

    @Override
    public DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new ESMR50RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T210 protocol V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2019-09-26 12:00:00 +0300 (Thu, 16 Sep 2019)$";
    }

    @Override
    public void setTime(Date newMeterTime) {
        //This device does not support setting "Hundredths of a seconds" byte
        try {
            AXDRDateTime dateTime = new AXDRDateTime(newMeterTime, getTimeZone());
            dateTime.setSetHSByte(false);
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(dateTime);
        } catch (IOException e) {
            journal(Level.WARNING, e.getMessage());
            throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
        }
    }
}
