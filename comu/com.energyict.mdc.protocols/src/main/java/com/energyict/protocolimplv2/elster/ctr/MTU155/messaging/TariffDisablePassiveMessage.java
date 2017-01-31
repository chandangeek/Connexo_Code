/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.rawobjects.RawTariffScheme;


public class TariffDisablePassiveMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "11.0.B";
    private static final String OBJECT_ID_FUTURE = "17.0.1";

    public TariffDisablePassiveMessage(Messaging messaging, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(messaging, issueService, collectedDataFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getDeviceMessageId().equals(DeviceMessageId.ACTIVITY_CALENDAR_CLEAR_AND_DISABLE_PASSIVE_TARIFF);
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        disableTariff();
        return null;
    }

    private void disableTariff() throws CTRException {
        RawTariffScheme rawTariffScheme = new RawTariffScheme();
        byte[] rawData = ProtocolTools.concatByteArrays(new CTRObjectID(OBJECT_ID_FUTURE).getBytes(), rawTariffScheme.getBytes());
        getFactory().executeRequest(ReferenceDate.getReferenceDate(2), WriteDataBlock.getRandomWDB(), new CTRObjectID(OBJECT_ID), rawData);
    }
}