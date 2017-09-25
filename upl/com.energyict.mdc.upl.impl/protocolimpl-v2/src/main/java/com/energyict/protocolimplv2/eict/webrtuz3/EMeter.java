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
import com.energyict.protocolimplv2.eict.webrtuz3.messages.emeter.WebRTUZ3EMeterMessaging;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/04/2015 - 16:29
 */
public class EMeter extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol;
    private final WebRTUZ3EMeterMessaging eMeterMessaging;
    private final NlsService nlsService;

    public EMeter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        this.masterProtocol = new WebRTUZ3(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, numberLookupExtractor, keyAccessorTypeExtractor);
        this.eMeterMessaging = new WebRTUZ3EMeterMessaging(masterProtocol, propertySpecService, nlsService, converter);
        this.nlsService = nlsService;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 DLMS E-meter slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-01-25 15:02:12 +0100 (Mon, 25 Jan 2016)$";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return eMeterMessaging;
    }

    @Override
    protected NlsService getNlsService() {
        return nlsService;
    }
}