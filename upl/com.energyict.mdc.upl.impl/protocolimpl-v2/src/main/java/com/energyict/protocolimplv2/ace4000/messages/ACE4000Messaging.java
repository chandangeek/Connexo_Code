package com.energyict.protocolimplv2.ace4000.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.ace4000.ACE4000MessageExecutor;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.common.objectserialization.codetable.CodeTableBase64Builder;
import com.energyict.protocolimplv2.messages.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CODE_TABLE_ID;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;

public class ACE4000Messaging implements DeviceMessageSupport {

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    protected final SimpleDateFormat europeanDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private List<DeviceMessageSpec> supportedMessages;
    private ACE4000MessageExecutor messageExecutor;
    private ACE4000Outbound protocol;

    public ACE4000Messaging(ACE4000Outbound protocol) {
        this.protocol = protocol;
    }

    public ACE4000MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new ACE4000MessageExecutor(protocol);
        }
        return messageExecutor;
    }

    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();
            //Load Profile messages
            supportedMessages.add(LoadProfileMessage.READ_PROFILE_DATA);

            //Events message
            supportedMessages.add(LogBookDeviceMessage.ReadLogBook);

            //Configuration messages
            supportedMessages.add(ConfigurationChangeDeviceMessage.SendShortDisplayMessage);
            supportedMessages.add(ConfigurationChangeDeviceMessage.SendLongDisplayMessage);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ResetDisplayMessage);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureLCDDisplay);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureLoadProfileDataRecording);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureSpecialDataMode);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureMaxDemandSettings);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureConsumptionLimitationsSettings);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureEmergencyConsumptionLimitation);
            supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureTariffSettings);

            //General messages
            supportedMessages.add(FirmwareDeviceMessage.FirmwareUpgradeWithUrlJarJadFileSize);
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
            supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        }


        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE)) {
            return dateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.ACTIVATION_DATE) ||
                propertySpec.getName().equals(DeviceMessageConstants.fromDateAttributeName) ||
                propertySpec.getName().equals(DeviceMessageConstants.toDateAttributeName)) {
            return europeanDateTimeFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(contactorActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime());
        } else if (propertySpec.getName().equals(CODE_TABLE_ID)) {
            Code codeTable = ((Code) messageAttribute);
            return CodeTableBase64Builder.getXmlStringFromCodeTable(codeTable);
        } else {
            return messageAttribute.toString();     //Works for BigDecimal, boolean and (hex)string property specs
        }
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }
}