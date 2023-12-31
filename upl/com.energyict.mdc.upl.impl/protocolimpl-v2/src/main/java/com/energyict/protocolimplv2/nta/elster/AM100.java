package com.energyict.protocolimplv2.nta.elster;

import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

import java.util.List;

/**
 * The AM100 implementation of the NTA spec
 *
 * @author sva
 * @since 30/10/12 (9:58)
 */
public final class AM100 extends WebRTUKP {

    private AM100DlmsProperties dlmsProperties;
    private LoadProfileBuilder loadProfileBuilder;

    public AM100(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
        setHasBreaker(true);
    }

    @Override
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();

        if (getDlmsCache() == null || getDlmsCache().getObjectList() == null || readCache) {
            journal( readCache ? "ReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
            setDlmsCache(new DLMSCache());
            readObjectList();
            getDlmsCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            journal( "Cache exist, will not be read!");
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsCache().getObjectList());

        }
    }

    private DLMSCache getDlmsCache() {
        return dlmsCache;
    }

    private void setDlmsCache(DLMSCache dlmsCache) {
        this.dlmsCache = dlmsCache;
    }

    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new AM100ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    public AM100DlmsProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new AM100DlmsProperties();
        }
        return dlmsProperties;
    }

    protected Dsmr23Messaging getDsmr23Messaging() {
        if (dsmr23Messaging == null) {
            dsmr23Messaging =
                    new Dsmr23Messaging(
                            new Dsmr23MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), keyAccessorTypeExtractor),
                            this.getPropertySpecService(), this.nlsService, this.converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
        }
        return dsmr23Messaging;
    }

    @Override
    protected LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return loadProfileBuilder;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS220/AS1440 AM100 DLMS (PRE-NTA)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-06-12 10:49:37 +0200 (Fri, 12 Jun 2015) $";
    }

    /**
     * The AM100 also supports the AT modem
     */
    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = super.getSupportedConnectionTypes();
        result.add(new SioAtModemConnectionType(this.getPropertySpecService()));
        result.add(new RxTxAtModemConnectionType(this.getPropertySpecService()));
        return result;
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }
}