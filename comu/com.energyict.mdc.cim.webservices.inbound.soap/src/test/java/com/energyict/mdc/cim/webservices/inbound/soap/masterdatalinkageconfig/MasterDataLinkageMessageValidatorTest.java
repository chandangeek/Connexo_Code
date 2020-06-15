/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;

import org.junit.Test;

public class MasterDataLinkageMessageValidatorTest extends AbstractMasterDataLinkageTest {
    private MasterDataLinkageConfigRequestMessageType message;

    @Test
    public void testValidate_createLinkage_payloadIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().dropPayload().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                    "Element 'Payload' is required.");
        }
    }

    @Test
    public void testValidate_closeLinkage_payloadIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().dropPayload().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                    "Element 'Payload' is required.");
        }
    }

    @Test
    public void testValidate_createLinkage_ConfigEventIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().dropConfigEvent().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                    "Element 'MasterDataLinkageConfig.ConfigurationEvent' is required.");
        }
    }

    @Test
    public void testValidate_closeLinkage_ConfigEventIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().dropConfigEvent().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                    "Element 'MasterDataLinkageConfig.ConfigurationEvent' is required.");
        }
    }

    @Test
    public void testValidate_createLinkage_emptyMeterList() throws Exception {
        // Prepare
        message = getValidMessage().eraseMeterList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.EMPTY_LIST.getErrorCode(),
                    "The list of 'MasterDataLinkageConfig.Meter' cannot be empty.");
        }
    }

    @Test
    public void testValidate_closeLinkage_emptyMeterList() throws Exception {
        // Prepare
        message = getValidMessage().eraseMeterList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.EMPTY_LIST.getErrorCode(),
                    "The list of 'MasterDataLinkageConfig.Meter' cannot be empty.");
        }
    }

    @Test
    public void testValidate_createLinkage_emptyUsagePointList() throws Exception {
        // Prepare
        message = getValidMessage().eraseUsagePointList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.EMPTY_LIST.getErrorCode(),
                    "The list of 'MasterDataLinkageConfig.UsagePoint' cannot be empty.");
        }
    }

    @Test
    public void testValidate_closeLinkage_emptyUsagePointList() throws Exception {
        // Prepare
        message = getValidMessage().eraseUsagePointList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.EMPTY_LIST.getErrorCode(),
                    "The list of 'MasterDataLinkageConfig.UsagePoint' cannot be empty.");
        }
    }

    @Test
    public void testValidate_createLinkage_createdDateTimeIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().withCreatedDateTime(null).build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                    "Element 'MasterDataLinkageConfig.ConfigurationEvent.createdDateTime' is required.");
        }
    }

    @Test
    public void testValidate_closeLinkage_createdDateTimeIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().withCreatedDateTime(null).build();

        // Act and verify no exceptions
        getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
    }

    @Test
    public void testValidate_createLinkage_effectiveDateTimeIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().withEffectiveDateTime(null).build();

        // Act and verify no exceptions
        getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
    }

    @Test
    public void testValidate_closeLinkage_effectiveDateTimeIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().withEffectiveDateTime(null).build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                    "Element 'MasterDataLinkageConfig.ConfigurationEvent.effectiveDateTime' is required.");
        }
    }

    @Test
    public void testValidate_createLinkage_identificationAttributesMissedForMeter() throws Exception {
        // Prepare
        message = getValidMessage().withMeterName(null).withMeterMRID(null).build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER,
                    MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                    "Either element 'mRID' or 'Names' is required under 'MasterDataLinkageConfig.Meter[0]' for identification purpose.");
        }
    }

    @Test
    public void testValidate_closeLinkage_identificationAttributesMissedForMeter() throws Exception {
        // Prepare
        message = getValidMessage().withMeterName(null).withMeterMRID(null).build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER,
                    MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                    "Either element 'mRID' or 'Names' is required under 'MasterDataLinkageConfig.Meter[0]' for identification purpose.");
        }
    }

    @Test
    public void testValidate_createLinkage_identificationAttributesMissedForUsagePoint() throws Exception {
        // Prepare
        message = getValidMessage().withUsagePointMRID(null).withUsagePointName(null).build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER,
                    MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                    "Either element 'mRID' or 'Names' is required under 'MasterDataLinkageConfig.UsagePoint[0]' for identification purpose.");
        }
    }

    @Test
    public void testValidate_closeLinkage_identificationAttributesMissedForUsagePoint() throws Exception {
        // Prepare
        message = getValidMessage().withUsagePointMRID(null).withUsagePointName(null).build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER,
                    MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT.getErrorCode(),
                    "Either element 'mRID' or 'Names' is required under 'MasterDataLinkageConfig.UsagePoint[0]' for identification purpose.");
        }
    }
}
