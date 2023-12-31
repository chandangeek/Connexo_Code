package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events.T210DLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message.T210DMessaging;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.properties.T210DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.properties.T210DProperties;
import com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.registers.T210DRegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 * Created by cisac on 6/27/2016.
 */
public class T210D extends AM130 {

    private final CertificateWrapperExtractor certificateWrapperExtractor;

    public T210D(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
        this.certificateWrapperExtractor = certificateWrapperExtractor;
    }

    @Override
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();

        if (readCache) { //If readCache is true, we will always read the objectList from device
            readObjectList();
            getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else if (getDeviceCache().getObjectList() == null) {
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
            idisMessaging = new T210DMessaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
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
    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new T210DLogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return idisLogBookFactory;
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

    @Override
    protected IDISProperties getNewInstanceOfProperties() {
        return new T210DProperties(certificateWrapperExtractor);
    }

    @Override
    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new T210DConfigurationSupport(getPropertySpecService());
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
    public boolean useDsmr4SelectiveAccessFormat() {
        return false;
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T/S210-D DLMS (IDIS P2) GPRS";
    }

    @Override
    public String getVersion() {
        return "$Date: 2017-03-20 16:29:41 +0200 (Mon, 20 Mar 2017)$";
    }
}
