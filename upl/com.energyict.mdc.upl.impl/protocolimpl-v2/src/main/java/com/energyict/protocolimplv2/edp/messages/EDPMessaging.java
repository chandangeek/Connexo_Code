package com.energyict.protocolimplv2.edp.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Class that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 11:32
 * Author: khe
 */
public class EDPMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;

    static {
        supportedMessages = new ArrayList<>();

        // relay control
        supportedMessages.add(ContactorDeviceMessage.CLOSE_RELAY);
        supportedMessages.add(ContactorDeviceMessage.OPEN_RELAY);
        supportedMessages.add(ContactorDeviceMessage.SET_RELAY_CONTROL_MODE);

        // public lighting
        supportedMessages.add(PublicLightingDeviceMessage.SET_RELAY_OPERATING_MODE);
        supportedMessages.add(PublicLightingDeviceMessage.SET_TIME_SWITCHING_TABLE);
        supportedMessages.add(PublicLightingDeviceMessage.SET_THRESHOLD_OVER_CONSUMPTION);
        supportedMessages.add(PublicLightingDeviceMessage.SET_OVERALL_MINIMUM_THRESHOLD);
        supportedMessages.add(PublicLightingDeviceMessage.SET_OVERALL_MAXIMUM_THRESHOLD);
        supportedMessages.add(PublicLightingDeviceMessage.SET_RELAY_TIME_OFFSETS_TABLE);
        supportedMessages.add(PublicLightingDeviceMessage.WRITE_GPS_COORDINATES);

        // firmware upgrade related
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);

        // activity calendar related
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_CONTRACT);
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_WITH_CONTRACT_AND_DATETIME);

        // billing
        supportedMessages.add(DeviceActionMessage.BILLING_RESET);
        supportedMessages.add(DeviceActionMessage.BILLING_RESET_CONTRACT_1);
        supportedMessages.add(DeviceActionMessage.BILLING_RESET_CONTRACT_2);
        supportedMessages.add(DeviceActionMessage.SET_PASSIVE_EOB_DATETIME);
    }

    private final AbstractMessageExecutor messageExecutor;

    public EDPMessaging(AbstractMessageExecutor messageExecutor) {
        super(messageExecutor.getProtocol());
        this.messageExecutor = messageExecutor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case activityCalendarCodeTableAttributeName:
                EDPActivityCalendarParser parser = new EDPActivityCalendarParser((Code) messageAttribute);
                return convertCodeTableToAXDR(parser);
            case specialDaysCodeTableAttributeName:
                return parseSpecialDays((Code) messageAttribute);
            case configUserFileAttributeName:
            case firmwareUpdateUserFileAttributeName:
                return new String(((UserFile) messageAttribute).loadFileInByteArray());
            case activityCalendarActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());     //Epoch
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessageExecutor().updateSentMessages(sentMessages);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    public AbstractMessageExecutor getMessageExecutor() {
        return messageExecutor;
    }
}
