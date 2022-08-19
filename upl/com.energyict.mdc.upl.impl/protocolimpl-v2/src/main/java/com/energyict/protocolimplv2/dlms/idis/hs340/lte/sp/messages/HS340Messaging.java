package com.energyict.protocolimplv2.dlms.idis.hs340.lte.sp.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.hs3300.messages.HS3300Messaging;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;

import java.util.Arrays;
import java.util.List;

public class HS340Messaging extends HS3300Messaging  {

    protected HS340MessageExecutor messageExecutor;

    public HS340Messaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, CertificateWrapperExtractor certificateWrapperExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, certificateWrapperExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                DeviceActionMessage.ReadDLMSAttribute.get(this.getPropertySpecService(), this.getNlsService(),  this.getConverter()),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER.get(this.getPropertySpecService(), this.getNlsService(),  this.getConverter())
        );
    }

    protected HS340MessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            this.messageExecutor = new HS340MessageExecutor(getProtocol(), this.getCollectedDataFactory(), this.getKeyAccessorTypeExtractor(), this.getIssueFactory());
        }
        return messageExecutor;
    }
}
