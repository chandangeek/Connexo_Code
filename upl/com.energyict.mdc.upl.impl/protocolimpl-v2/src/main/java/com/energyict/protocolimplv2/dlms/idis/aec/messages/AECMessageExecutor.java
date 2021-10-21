package com.energyict.protocolimplv2.dlms.idis.aec.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.dlms.cosem.Clock;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540MessageExecutor;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;

import java.io.IOException;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SetDSTAttributeName;

public class AECMessageExecutor extends AM540MessageExecutor {
    public AECMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SetDST)) {
            enableDST(pendingMessage);
        } else {
            collectedMessage = super.executeMessage(pendingMessage, collectedMessage);
        }
        return collectedMessage;
    }

    private void enableDST(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(getDeviceMessageAttributeValue(pendingMessage, SetDSTAttributeName));
        Clock clock = getProtocol().getDlmsSession().getCosemObjectFactory().getClock();
        clock.enableDisableDs(enable);
    }
}
