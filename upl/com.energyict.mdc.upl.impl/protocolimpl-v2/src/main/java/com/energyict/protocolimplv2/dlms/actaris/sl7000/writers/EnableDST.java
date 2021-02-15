package com.energyict.protocolimplv2.dlms.actaris.sl7000.writers;

import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.Data;
import com.energyict.mdc.upl.ProtocolException;
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
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.Message;
import com.energyict.protocolimplv2.dlms.common.writers.impl.AbstractMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;

public class EnableDST extends AbstractMessage implements Message {

    private static final String ATT_NAME = DeviceMessageConstants.enableDSTAttributeName;
    private static final ObisCode DST_WORKING_MODE_OBIS = ObisCode.fromString("0.0.131.0.4.255");

    private final AbstractDlmsProtocol dlmsProtocol;
    private final PropertySpecService propSpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public EnableDST(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter) {
        super(collectedDataFactory, issueFactory);
        this.dlmsProtocol = dlmsProtocol;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public CollectedMessage execute(OfflineDeviceMessage message) {
        try {
            String stringValue = message.getDeviceMessageAttributes().stream().filter(f -> f.getName().equals(ATT_NAME)).findFirst().
                    orElseThrow(() -> new ProtocolException("DeviceMessage didn't contain a value found for MessageAttribute " + ATT_NAME)).getValue();
            boolean value = ProtocolTools.getBooleanFromString(stringValue);
            Data data = dlmsProtocol.getDlmsSession().getCosemObjectFactory().getData(DST_WORKING_MODE_OBIS);
            Unsigned8 newMode = new Unsigned8(0);
            if (value) {
                newMode = new Unsigned8(1);
            }
            data.setValueAttr(newMode);

        } catch (IOException e) {
            super.createErrorCollectedMessage(message, e);
        }
        return super.createCollectedMessage(message);
    }

    @Override
    public DeviceMessageSpec asMessageSpec() {
        return ClockDeviceMessage.EnableOrDisableDST.get(propSpecService, nlsService, converter);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }
}
