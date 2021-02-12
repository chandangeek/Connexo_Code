package com.energyict.protocolimplv2.dlms.actaris.sl7000.writers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.Message;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;

public class SetEndDSTTime extends DSTTime implements Message {


    public SetEndDSTTime(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter) {
        super(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter);
    }

    @Override
    public CollectedMessage execute(OfflineDeviceMessage message) {
        return super.execute(message, false);
    }

    @Override
    public DeviceMessageSpec asMessageSpec() {
        return ClockDeviceMessage.SetEndOfDST.get(propSpecService, nlsService, converter);
    }

}
