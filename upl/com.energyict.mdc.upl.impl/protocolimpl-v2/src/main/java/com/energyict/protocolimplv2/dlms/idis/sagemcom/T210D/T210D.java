package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message.T210DMessaging;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.registers.T210DRegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 * Created by cisac on 6/27/2016.
 */
public class T210D extends AM130 {

    public T210D(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor);
    }

    @Override
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();

        if(readCache){ //If readCache is true, we will always read the objectList from device
            readObjectList();
            getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else if(getDeviceCache().getObjectList() == null){
            //if we don't have a cache and we dont want to read the objectList from device, then use a hardcoded copy of the device objectList to reduce the communication overhead
            getLogger().info("Cache does not exist, using hardcoded copy of object list");
            UniversalObject[] objectList = new T210DObjectList().getObjectList();
            getDeviceCache().saveObjectList(objectList);
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());

    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new T210DMessaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor());
        }
        return idisMessaging;
    }

    @Override
    protected AM130RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new T210DRegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public void setTime(Date newMeterTime) {
        //This device does not support setting "Hundredths of a seconds" byte
        try {
            AXDRDateTime dateTime = new AXDRDateTime(newMeterTime, getTimeZone());
            dateTime.setSetHSByte(false);
            dateTime.useUnspecifiedAsDeviation(getDlmsSessionProperties().useUndefinedAsTimeDeviation());
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(dateTime);
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
        }
    }

    //TODO: remove this when the device will offer propper support for realeasing the association
    //For now disconnect only the TCP connection
    @Override
    public void logOff() {
        if (getDlmsSession() != null) {
            getDlmsSession().getDlmsV2Connection().disconnectMAC();
        }
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T210-D DLMS (IDIS P2) GPRS";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-09-07 16:54:19 +0300 (Wed, 07 Sep 2016)$";
    }
}
