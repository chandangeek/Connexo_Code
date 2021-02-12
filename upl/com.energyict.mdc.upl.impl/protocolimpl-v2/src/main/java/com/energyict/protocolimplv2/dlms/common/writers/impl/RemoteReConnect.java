package com.energyict.protocolimplv2.dlms.common.writers.impl;

import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;

import java.io.IOException;

public class RemoteReConnect extends AbstractMessage {

    private final AbstractDlmsProtocol dlmsProtocol;
    private final PropertySpecService propSpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public RemoteReConnect(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter) {
        super(collectedDataFactory, issueFactory);
        this.dlmsProtocol = dlmsProtocol;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public CollectedMessage execute(OfflineDeviceMessage message) {
        try {
            dlmsProtocol.getDlmsSession().getCosemObjectFactory().getDisconnector().remoteReconnect();
            return super.createCollectedMessage(message);
        } catch (NotInObjectListException e) {
            return super.createNotSupportedMessage(message);
        } catch (IOException e) {
            return super.createErrorCollectedMessage(message,e);
        }
    }

    @Override
    public DeviceMessageSpec asMessageSpec() {
        return ContactorDeviceMessage.CONTACTOR_CLOSE.get(propSpecService, nlsService, converter);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}
