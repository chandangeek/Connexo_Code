/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;

import org.junit.Ignore;
import org.junit.Test;

public class MasterDataLinkageMessageValidatorTest extends AbstractMasterDataLinkageTest {
    private MasterDataLinkageConfigRequestMessageType message;

    @Ignore @Test
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

    @Ignore @Test
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

    @Ignore @Test
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

    @Ignore @Test
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

    @Ignore @Test
    public void testValidate_createLinkage_emptyMeterList() throws Exception {
        // Prepare
        message = getValidMessage().eraseMeterList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.EMPTY_LIST.getErrorCode(),
                    "The list of 'MasterDataLinkageConfig.Meter' can't be empty.");
        }
    }

    @Ignore @Test
    public void testValidate_closeLinkage_emptyMeterList() throws Exception {
        // Prepare
        message = getValidMessage().eraseMeterList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.EMPTY_LIST.getErrorCode(),
                    "The list of 'MasterDataLinkageConfig.Meter' can't be empty.");
        }
    }

    @Ignore @Test
    public void testValidate_createLinkage_emptyUsagePointAndEndDeviceList() throws Exception {
        // Prepare
        message = getValidMessage().eraseUsagePointList().eraseEndDeviceList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.EMPTY_USAGE_POINT_OR_END_DEVICE_LIST.getErrorCode(),
                    "Either the node 'MasterDataLinkageConfig.UsagePoint' or 'MasterDataLinkageConfig.EndDevice' should contain elements 'mRID' or 'Names' for identification purpose.");
        }
    }

    @Ignore @Test
    public void testValidate_closeLinkage_emptyUsagePointAndEndDeviceList() throws Exception {
        // Prepare
        message = getValidMessage().eraseUsagePointList().eraseEndDeviceList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.EMPTY_USAGE_POINT_OR_END_DEVICE_LIST.getErrorCode(),
                    "Either the node 'MasterDataLinkageConfig.UsagePoint' or 'MasterDataLinkageConfig.EndDevice' should contain elements 'mRID' or 'Names' for identification purpose.");
        }
    }

    @Ignore @Test
    public void testValidate_createLinkage_createdDateTimeIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().withCreatedDateTime(null).eraseEndDeviceList().build();

        // Act and verify
        try {
            getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                    "Element 'MasterDataLinkageConfig.ConfigurationEvent.createdDateTime' is required.");
        }
    }

    @Ignore @Test
    public void testValidate_closeLinkage_createdDateTimeIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().withCreatedDateTime(null).eraseEndDeviceList().build();

        // Act and verify no exceptions
        getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CLOSE);
    }

    @Ignore @Test
    public void testValidate_createLinkage_effectiveDateTimeIsMissing() throws Exception {
        // Prepare
        message = getValidMessage().withEffectiveDateTime(null).eraseEndDeviceList().build();

        // Act and verify no exceptions
        getInstance(MasterDataLinkageMessageValidator.class).validate(message, MasterDataLinkageAction.CREATE);
    }

    @Ignore @Test
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

    @Ignore @Test
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
                    "Either element 'mRID' or 'Names' is required under 'MasterDataLinkageConfig.Meter' for identification purpose.");
        }
    }

    @Ignore @Test
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
                    "Either element 'mRID' or 'Names' is required under 'MasterDataLinkageConfig.Meter' for identification purpose.");
        }
    }

    @Ignore @Test
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
                    "Either element 'mRID' or 'Names' is required under 'MasterDataLinkageConfig.UsagePoint' for identification purpose.");
        }
    }

    @Ignore
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
                    "Either element 'mRID' or 'Names' is required under 'MasterDataLinkageConfig.UsagePoint' for identification purpose.");
        }
    }
}
