package com.energyict.protocolimplv2.dlms.idis.am132;

import com.energyict.common.framework.CryptoDlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.am132.properties.AM132Properties;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.messaging.CryptoAM540Messaging;

/**
 * Created by cisac on 6/6/2017.
 */
public class CryptoAM132 extends AM132 {

    public CryptoAM132(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM130-v2 DLMS (IDIS P2) crypto-protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2018-05-08 16:50:42 +0300 (Tue, 08 May 2018) $";
    }

    @Override
    protected AM132Properties getNewInstanceOfProperties() {
        return new CryptoAM132Properties(getPropertySpecService(), getNlsService());
    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new CryptoAM540Messaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return idisMessaging;
    }

    @Override
    protected void initDlmsSession(ComChannel comChannel) {
        setDlmsSession(getCryptoDlmsSession(comChannel));
    }

    @Override
    protected DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return getCryptoDlmsSession(comChannel);
    }

    private DlmsSession getCryptoDlmsSession(ComChannel comChannel) {
        //Uses the HSM to encrypt requests and decrypt responses, we don't have the plain keys
        return new CryptoDlmsSession(comChannel, getDlmsSessionProperties());
    }
}
