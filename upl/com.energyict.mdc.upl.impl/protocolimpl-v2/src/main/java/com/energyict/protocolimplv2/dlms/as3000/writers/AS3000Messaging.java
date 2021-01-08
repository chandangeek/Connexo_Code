package com.energyict.protocolimplv2.dlms.as3000.writers;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.issue.IssueFactory;
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
import com.energyict.protocolimplv2.dlms.common.writers.providers.OctetStringProvider;
import com.energyict.protocolimplv2.dlms.common.writers.providers.U16Provider;
import com.energyict.protocolimplv2.dlms.common.writers.impl.GenericAttributeWrite;
import com.energyict.protocolimplv2.dlms.common.writers.impl.GenericNoParamMethodInvoke;
import com.energyict.protocolimplv2.dlms.common.writers.impl.RemoteDisconnect;
import com.energyict.protocolimplv2.dlms.common.writers.impl.RemoteReConnect;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;

import java.util.ArrayList;

public class AS3000Messaging  {


    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propSpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final AbstractDlmsProtocol dlmsProtocol;


    public AS3000Messaging(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propSpecService, NlsService nlsService, Converter converter, AbstractDlmsProtocol dlmsProtocol) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.dlmsProtocol = dlmsProtocol;
    }

    public MessageHandler getMessageHandler() {
        ArrayList<Message> messages = new ArrayList<>();
        // Invoke method section
        messages.add(new RemoteDisconnect(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter));
        messages.add(new RemoteReConnect(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter));
        messages.add(new GenericNoParamMethodInvoke(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter,
                ObisCode.fromString("1.1.135.1.0.255"), DLMSClassId.SCRIPT_TABLE, 1, AlarmConfigurationMessage.RESET_ALL_ALARM_BITS));
        messages.add(new GenericMethodInvoke(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter,
                ObisCode.fromString("0.0.10.0.1.255"),DLMSClassId.SCRIPT_TABLE, 1, new ConstantValueProvider(new Unsigned8(0)), DeviceActionMessage.DEMAND_RESET));
        // Write attributes section
        messages.add(new GenericAttributeWrite(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter,
                new DLMSAttribute(ObisCode.fromString("1.1.130.8.1.255"), 2, DLMSClassId.DATA),new U16Provider(DeviceMessageConstants.SetPowerQualityMeasurePeriodAttributeName), PowerConfigurationDeviceMessage.SetPowerQualityMeasurePeriod));
        messages.add(new GenericAttributeWrite(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter,
                new DLMSAttribute(ObisCode.fromString("1.1.132.1.1.255"), 2, DLMSClassId.DATA),new U16Provider(DeviceMessageConstants.limitationActionDelay), DeviceActionMessage.LimitationActionDelay));
        messages.add(new GenericAttributeWrite(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter,
                new DLMSAttribute(ObisCode.fromString("1.1.132.1.2.255"), 2, DLMSClassId.DATA),new OctetStringProvider(DeviceMessageConstants.limitationMeasurementQuantity, 1), DeviceActionMessage.LimitationQuantityMeasure));
        messages.add(new GenericAttributeWrite(collectedDataFactory, issueFactory, dlmsProtocol, propSpecService, nlsService, converter,
                new DLMSAttribute(ObisCode.fromString("1.1.134.1.0.255"), 2, DLMSClassId.DATA),new U16Provider(DeviceMessageConstants.engineerPin), ConfigurationChangeDeviceMessage.SET_ENGINEER_PIN_4DIGITS_NO_TIMEOUT));

        return new MessageHandler(collectedDataFactory, issueFactory, messages);
    }

}
