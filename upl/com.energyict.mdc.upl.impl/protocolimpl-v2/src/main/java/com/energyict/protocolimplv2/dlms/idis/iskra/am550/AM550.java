package com.energyict.protocolimplv2.dlms.idis.iskra.am550;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.iskra.am550.events.Am550LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.Mx382;

public class AM550 extends Mx382 {

    public AM550(PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                 CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor,
                 DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new Am550LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return idisLogBookFactory;
    }

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco AM550 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-01-27$";
    }

}
