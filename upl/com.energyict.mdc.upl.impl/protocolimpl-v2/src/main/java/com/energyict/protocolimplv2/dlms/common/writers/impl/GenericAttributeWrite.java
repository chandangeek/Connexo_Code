package com.energyict.protocolimplv2.dlms.common.writers.impl;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.AttributeProvider;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;

import java.io.IOException;

public class GenericAttributeWrite extends AbstractMessage {

    private final AbstractDlmsProtocol dlmsProtocol;
    private final PropertySpecService propSpecService;
    private final NlsService nlsService;
    private final Converter converter;

    private final DLMSAttribute dlmsAttribute;
    private final AttributeProvider attributeProvider;
    private final DeviceMessageSpecSupplier deviceMessageSpecSupplier;

    public GenericAttributeWrite(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter, DLMSAttribute dlmsAttribute, AttributeProvider attributeProvider, DeviceMessageSpecSupplier deviceMessageSpecSupplier) {
        super(collectedDataFactory, issueFactory);
        this.dlmsProtocol = dlmsProtocol;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.dlmsAttribute = dlmsAttribute;
        this.attributeProvider = attributeProvider;
        this.deviceMessageSpecSupplier = deviceMessageSpecSupplier;
    }

    @Override
    public CollectedMessage execute(OfflineDeviceMessage message) {
        try {
            this.dlmsProtocol.getDlmsSession().getCosemObjectFactory().writeObject(dlmsAttribute.getObisCode(), dlmsAttribute.getDLMSClassId().getClassId(), dlmsAttribute.getAttribute(), attributeProvider.provide(dlmsProtocol, message));
            return super.createCollectedMessage(message);
        } catch (IOException e) {
            return super.createErrorCollectedMessage(message, e);
        }
    }

    @Override
    public DeviceMessageSpec asMessageSpec() {
        return deviceMessageSpecSupplier.get(propSpecService, nlsService, converter);
    }
}
