package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;

/**
 * Logical slave protocol that does not read out any data, it's merely a placeholder for the supported messages, properties, ...
 * The read out of the MBus registers, logbook, load profile, etc is implemented in the AM540 E-meter (master) protocol.
 *
 * @author sva
 * @since 11/08/2015 - 14:49
 */
public class MBusDevice extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol;
    private final IDISMBusMessaging idisMBusMessaging;
    private final NlsService nlsService;

    public MBusDevice(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        this.masterProtocol = new T210D(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor);
        this.idisMBusMessaging = new IDISMBusMessaging(masterProtocol, propertySpecService, nlsService, converter, keyAccessorTypeExtractor);
        this.nlsService = nlsService;
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T/S210-D DLMS (IDIS P2) MBus slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2017-03-20 16:29:40 +0200 (Mon, 20 Mar 2017)$";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }

    @Override
    protected NlsService getNlsService() {
        return nlsService;
    }
}
