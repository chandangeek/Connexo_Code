/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;

import com.energyict.dlms.DlmsSession;
import com.energyict.protocolimpl.dlms.prime.PrimeProperties;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.TariffControl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PrimeMessaging {

    private final DlmsSession session;
    private final DisconnectControl disconnectControl;
    private final ClockControl clockControl;
    private final FirmwareUpgrade firmwareUpgrade;
    private final TariffControl tariffControl;
    private final PowerQuality powerQuality;
    private final DemandResponse demandResponse;
    private final PowerFailure powerFailure;
    private final CommunicationSetup communicationSetup;
    private final PasswordSetup passwordSetup;
    private final BasicIntervalSetup basicIntervalSetup;

    public PrimeMessaging(DlmsSession session, PrimeProperties properties, DeviceMessageFileService DeviceMessageFileService) {
        this.session = session;
        this.disconnectControl = new DisconnectControl(session);
        this.clockControl = new ClockControl(session);
        this.firmwareUpgrade = new FirmwareUpgrade(session, properties, DeviceMessageFileService);
        this.tariffControl = new TariffControl(session);
        this.powerQuality = new PowerQuality(session);
        this.demandResponse = new DemandResponse(session);
        this.powerFailure = new PowerFailure(session);
        this.communicationSetup = new CommunicationSetup(session);
        this.passwordSetup = new PasswordSetup(session);
        this.basicIntervalSetup = new BasicIntervalSetup(session);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        if (disconnectControl.canHandle(messageEntry)) {
            return disconnectControl.execute(messageEntry);
        } else if (clockControl.canHandle(messageEntry)) {
            return clockControl.execute(messageEntry);
        } else if (firmwareUpgrade.canHandle(messageEntry)) {
            return firmwareUpgrade.execute(messageEntry);
        } else if (tariffControl.canHandle(messageEntry)) {
            return tariffControl.execute(messageEntry);
        } else if (this.powerQuality.canHandle(messageEntry)) {
        	return this.powerQuality.execute(messageEntry);
        } else if (this.demandResponse.canHandle(messageEntry)) {
        	return this.demandResponse.execute(messageEntry);
        } else if (this.powerFailure.canHandle(messageEntry)) {
        	return this.powerFailure.execute(messageEntry);
        } else if (this.communicationSetup.canHandle(messageEntry)) {
        	return this.communicationSetup.execute(messageEntry);
        } else if (this.passwordSetup.canHandle(messageEntry)) {
        	return this.passwordSetup.execute(messageEntry);
        } else if (this.basicIntervalSetup.canHandle(messageEntry)) {
        	return this.basicIntervalSetup.execute(messageEntry);
        }

        session.getLogger().severe("Unable to handle message [" + messageEntry.getContent() + "]!");
        return MessageResult.createFailed(messageEntry);
    }

    public static List<MessageCategorySpec> getMessageCategories() {
        return Arrays.asList(
                    DisconnectControl.getCategorySpec(),
                    ClockControl.getCategorySpec(),
                    TariffControl.getCategorySpec(),
                    PowerQuality.getCategorySpec(),
                    DemandResponse.getCategorySpec(),
                    PowerFailure.getCategorySpec(),
                    CommunicationSetup.getCategorySpec(),
                    PasswordSetup.getCategorySpec());
    }

}