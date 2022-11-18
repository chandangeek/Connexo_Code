/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.hs340.plc.sp.messages;

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
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;

import java.util.Arrays;
import java.util.List;


public class HS340PLCMessaging extends HS3300Messaging {

    public HS340PLCMessaging(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                             PropertySpecService propertySpecService, NlsService nlsService, Converter converter,
                             TariffCalendarExtractor calendarExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                             DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, calendarExtractor, certificateWrapperExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                PLCConfigurationDeviceMessage.SetToneMaskAttributeName.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()),
                PLCConfigurationDeviceMessage.WRITE_G3_PLC_BANDPLAN.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()),
                PLCConfigurationDeviceMessage.WritePlcG3Timeout.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()),
                PLCConfigurationDeviceMessage.SetAdpLBPAssociationSetup_5_Parameters.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()),
                PLCConfigurationDeviceMessage.WRITE_ADP_LQI_RANGE.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()),
                DeviceActionMessage.ReadDLMSAttribute.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter()),
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER.get(this.getPropertySpecService(), this.getNlsService(), this.getConverter())
        );
    }
}
