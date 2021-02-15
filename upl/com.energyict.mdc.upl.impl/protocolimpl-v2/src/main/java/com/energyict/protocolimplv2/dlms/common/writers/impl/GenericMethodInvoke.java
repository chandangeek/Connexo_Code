package com.energyict.protocolimplv2.dlms.common.writers.impl;

import com.energyict.dlms.cosem.DLMSClassId;
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
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.AttributeProvider;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;

import java.io.IOException;

public class GenericMethodInvoke extends AbstractMessage {

    private final AbstractDlmsProtocol dlmsProtocol;
    private final PropertySpecService propSpecService;
    private final NlsService nlsService;
    private final Converter converter;

    private final ObisCode obisCode;
    private final DLMSClassId classId;
    private final int method;
    private final AttributeProvider attributeProvider;
    private final DeviceMessageSpecSupplier deviceMessageSpecSupplier;

    public GenericMethodInvoke(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter, ObisCode obisCode, DLMSClassId classId, int method, AttributeProvider attributeProvider, DeviceMessageSpecSupplier deviceMessageSpecSupplier) {
        super(collectedDataFactory, issueFactory);
        this.dlmsProtocol = dlmsProtocol;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.obisCode = obisCode;
        this.classId = classId;
        this.method = method;
        this.attributeProvider = attributeProvider;
        this.deviceMessageSpecSupplier = deviceMessageSpecSupplier;
    }

    @Override
    public CollectedMessage execute(OfflineDeviceMessage message) {
        try {
            this.dlmsProtocol.getDlmsSession().getCosemObjectFactory().getGenericInvoke(obisCode, classId.getClassId(), method).invoke(attributeProvider.provide(dlmsProtocol, message));
            return super.createCollectedMessage(message);
        } catch (NotInObjectListException e) {
            return super.createNotSupportedMessage(message);
        } catch (IOException e) {
            return super.createErrorCollectedMessage(message, e);
        }
    }

    @Override
    public DeviceMessageSpec asMessageSpec() {
        return this.deviceMessageSpecSupplier.get(propSpecService, nlsService, converter);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}
