/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Beacon3100Messaging extends AbstractMessageExecutor implements DeviceMessageSupport {

    private static final ObisCode MULTICAST_FIRMWARE_UPGRADE_OBISCODE = ObisCode.fromString("0.0.44.0.128.255");
    private static final ObisCode MULTICAST_METER_PROGRESS = ProtocolTools.setObisCodeField(MULTICAST_FIRMWARE_UPGRADE_OBISCODE, 1, (byte) (-1 * ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS));
    private static final String TEMP_DIR = "java.io.tmpdir";
    private static final ObisCode DEVICE_NAME_OBISCODE = ObisCode.fromString("0.0.128.0.9.255");
    private static final String SEPARATOR = ";";
    private static final String SEPARATOR2 = ",";

    private final Set<DeviceMessageId> supportedMessages = EnumSet.of(
            DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS,

            DeviceMessageId.DEVICE_ACTIONS_SYNC_MASTERDATA
    );

    public Beacon3100Messaging(AbstractDlmsProtocol protocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, CollectedDataFactory collectedDataFactory) {
        super(protocol, issueService, readingTypeUtilService, collectedDataFactory);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return Collections.emptySet();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getCollectedDataFactory().createCollectedMessageList(pendingMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";
    }
}
