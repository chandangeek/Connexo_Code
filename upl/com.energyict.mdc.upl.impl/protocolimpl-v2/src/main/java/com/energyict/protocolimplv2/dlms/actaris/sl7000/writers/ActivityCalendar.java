package com.energyict.protocolimplv2.dlms.actaris.sl7000.writers;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.writers.Message;
import com.energyict.protocolimplv2.dlms.common.writers.impl.AbstractMessage;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;

public class ActivityCalendar extends AbstractMessage implements Message {

    private static final String ATT_NAME = DeviceMessageConstants.XmlUserFileAttributeName;

    private final AbstractDlmsProtocol dlmsProtocol;
    private final PropertySpecService propSpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final DeviceMessageFileExtractor messageFileExtractor;

    public ActivityCalendar(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, AbstractDlmsProtocol dlmsProtocol, PropertySpecService propSpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(collectedDataFactory, issueFactory);
        this.dlmsProtocol = dlmsProtocol;
        this.propSpecService = propSpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageFileExtractor = messageFileExtractor;
    }

    @Override
    public CollectedMessage execute(OfflineDeviceMessage message) {
        try {
            String xml = message.getDeviceMessageAttributes().stream().filter(f -> f.getName().equals(ATT_NAME)).findFirst().
                    orElseThrow(() -> new ProtocolException("DeviceMessage didn't contain a value found for MessageAttribute " + ATT_NAME)).getValue();
            ActivityCalendarHelper activityCalendarController = new ActivityCalendarHelper(dlmsProtocol, xml);
            activityCalendarController.writeCalendarNamePassive();
            activityCalendarController.writeCalendar();
        } catch (IOException e) {
            super.createErrorCollectedMessage(message, e);
        }
        return super.createConfirmedCollectedMessage(message);
    }

    @Override
    public DeviceMessageSpec asMessageSpec() {
        return ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE.get(propSpecService, nlsService, converter);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        DeviceMessageFile deviceMessageFile = (DeviceMessageFile) messageAttribute;
        return this.messageFileExtractor.contents(deviceMessageFile);
    }
}
