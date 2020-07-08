/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigFaultMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public abstract class AbstractMasterDataLinkageTest extends AbstractMockActivator {

    protected  MasterDataLinkageMessageBuilder getValidMessage() {
        return MasterDataLinkageMessageBuilder.createEmptyMessage()
                .withCreatedDateTime(Instant.now())
                .withEffectiveDateTime(Instant.now())
                .withMeterMRID("mtmr")
                .withMeterName("mtnm")
                .withMeterRole("mtrl")
                .withUsagePointMRID("upmr")
                .withUsagePointName("upnm");
    }

    protected void verifyFaultMessage(FaultMessage e, MessageSeeds expectedBasicSeed, String errorCode, String expectedDetails) {
        assertThat(e.getMessage()).isEqualTo(expectedBasicSeed.translate(thesaurus));
        MasterDataLinkageConfigFaultMessageType faultInfo = e.getFaultInfo();
        assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
        assertThat(faultInfo.getReply().getError()).hasSize(1);
        ErrorType error = faultInfo.getReply().getError().get(0);
        assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
        assertThat(error.getCode()).isEqualTo(errorCode);
        assertThat(error.getDetails()).isEqualTo(expectedDetails);
    }

    protected void failNoException() {
        fail("Expected exception was not thrown");
    }

}
