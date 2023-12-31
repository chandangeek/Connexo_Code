package com.energyict.protocolimplv2.dlms.actaris.sl7000.writers;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.Message;
import com.energyict.protocolimplv2.dlms.common.writers.MessageHandler;
import com.energyict.protocolimplv2.dlms.common.writers.impl.GenericMethodInvoke;
import com.energyict.protocolimplv2.dlms.common.writers.providers.ConstantValueProvider;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;

import java.util.ArrayList;

public class ActarisSl7000Messaging {

    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propSpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final AbstractDlmsProtocol dlmsProtocol;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;


    public ActarisSl7000Messaging(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propSpecService, NlsService nlsService, Converter converter, AbstractDlmsProtocol dlmsProtocol, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.dlmsProtocol = dlmsProtocol;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    public MessageHandler getMessageHandler() {
        ArrayList<Message> messages = new ArrayList<>();
        // Invoke method section
        messages.add(new GenericMethodInvoke(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter,
                ObisCode.fromString("0.0.10.0.1.255"), DLMSClassId.SCRIPT_TABLE, 1, new ConstantValueProvider(new Unsigned16(1)), DeviceActionMessage.BILLING_RESET));
        // write attributes
        messages.add(new ActivityCalendar(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter, deviceMessageFileExtractor));
        messages.add(new BatteryExpiryDate(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter));
        messages.add(new EnableDST(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter));
        messages.add(new SetStartDSTTime(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter));
        messages.add(new SetEndDSTTime(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter));

        return new MessageHandler(collectedDataFactory, issueFactory, messages);
    }

}
