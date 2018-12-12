package com.energyict.protocolimplv2.eict.webrtuz3;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.eict.webrtuz3.messages.mbus.WebRTUZ3MBusMessaging;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/04/2015 - 16:29
 */
public class MBusDevice extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol;
    private final WebRTUZ3MBusMessaging mBusMessaging;
    private final NlsService nlsService;

    public MBusDevice(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        masterProtocol = new WebRTUZ3(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, numberLookupExtractor, keyAccessorTypeExtractor);
        mBusMessaging = new WebRTUZ3MBusMessaging(masterProtocol, propertySpecService, nlsService, converter, keyAccessorTypeExtractor);
        this.nlsService = nlsService;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 DLMS MBus slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-01-25 15:02:12 +0100 (Mon, 25 Jan 2016)$";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return mBusMessaging;
    }

    @Override
    protected NlsService getNlsService() {
        return nlsService;
    }
}