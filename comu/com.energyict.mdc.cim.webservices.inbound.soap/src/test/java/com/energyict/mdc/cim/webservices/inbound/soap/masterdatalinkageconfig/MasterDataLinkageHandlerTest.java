/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class MasterDataLinkageHandlerTest extends AbstractMasterDataLinkageTest {
    @Rule
    public TestRule frosty = Using.timeZoneOfMcMurdo();

    private MasterDataLinkageHandler linkageHandler;

    private static final String USAGE_POINT_NAME = "usagePointName";
    private static final String USAGE_POINT_MRID = "usagePointMRID";
    private static final String METER_NAME = "meterName";
    private static final String METER_MRID = "meterMRID";
    private static final String METER_ROLE = "meter.role.check";

    private static final Instant CREATED_DATE_TIME = LocalDate.of(2017, Month.JULY, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final Instant EFFECTIVE_DATE_TIME = LocalDate.of(2017, Month.JULY, 5).atStartOfDay().toInstant(ZoneOffset.UTC);

    @Mock
    private Meter meter;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterRole meterRole;
    @Mock
    private UsagePointMeterActivator usagePointMeterActivator;
    @Mock
    private MeterActivation meterActivation;


    @Before
    public void setUp() throws Exception {
        linkageHandler = getInstance(MasterDataLinkageHandler.class);
    }

    @Test
    public void testForMessage() throws Exception {
        //Act
        linkageHandler.forMessage(getValidMessage().build());

        //Verify
        assertThat(linkageHandler.configurationEventNode.getCreatedDateTime()).isNotNull().isEqualTo(CREATED_DATE_TIME);
        assertThat(linkageHandler.configurationEventNode.getEffectiveDateTime()).isNotNull().isEqualTo(EFFECTIVE_DATE_TIME);
        assertThat(linkageHandler.meterNodes.get(0).getMRID()).isNotNull().isEqualTo(METER_MRID);
        assertThat(linkageHandler.meterNodes.get(0).getNames().get(0).getName()).isNotNull().isEqualTo(METER_NAME);
        assertThat(linkageHandler.meterNodes.get(0).getRole()).isNotNull().isEqualTo(METER_ROLE);
        assertThat(linkageHandler.usagePointNodes.get(0).getMRID()).isNotNull().isEqualTo(USAGE_POINT_MRID);
        assertThat(linkageHandler.usagePointNodes.get(0).getNames().get(0).getName()).isNotNull().isEqualTo(USAGE_POINT_NAME);
    }

    //<editor-fold desc="Create linkage">
    @Test
    public void testCreateLinkage_byMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint, times(1)).linkMeters();
        verify(usagePointMeterActivator, times(1)).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator, times(1)).complete();
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateLinkage_byName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterMRID(null)
                .withUsagePointMRID(null)
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint, times(1)).linkMeters();
        verify(usagePointMeterActivator, times(1)).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator, times(1)).complete();
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateLinkage_roleNotSpecified() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterRole(null)
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT)).thenReturn(meterRole);
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint, times(1)).linkMeters();
        verify(usagePointMeterActivator, times(1)).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator, times(1)).complete();
        verifyResponse(response, HeaderType.Verb.CREATED, ReplyType.Result.OK);
    }

    @Test
    public void testCreateLinkage_invalidRoleSpecified() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterRole("invalidRole")
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole("invalidRole")).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_METER_ROLE_WITH_KEY.getErrorCode(),
                    "No meter role is found by key 'invalidRole'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_bulkReceived() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .spawnLists()
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.empty());
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.activate(CREATED_DATE_TIME, meter, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.createLinkage();

        //Verify
        verify(usagePoint, times(1)).linkMeters();
        verify(usagePointMeterActivator, times(1)).activate(CREATED_DATE_TIME, meter, meterRole);
        verify(usagePointMeterActivator, times(1)).complete();
        verifyResponse(response,
                HeaderType.Verb.CREATED,
                ReplyType.Result.PARTIAL,
                MessageSeeds.UNSUPPORTED_BULK_OPERATION,
                MessageSeeds.UNSUPPORTED_BULK_OPERATION);
    }

    @Test
    public void testCreateLinkage_meterNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_METER_WITH_MRID.getErrorCode(),
                    "No meter is found by MRID 'meterMRID'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_meterNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterMRID(null)
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_METER_WITH_NAME.getErrorCode(),
                    "No meter is found by name 'meterName'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_usagePointNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_USAGE_POINT_WITH_MRID.getErrorCode(),
                    "No usage point is found by MRID 'usagePointMRID'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_usagePointNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withUsagePointMRID(null)
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.NO_USAGE_POINT_WITH_NAME.getErrorCode(),
                    "No usage point is found by name 'usagePointName'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCreateLinkage_meterAndUsagePointAlreadyLinked() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMeterRole(METER_ROLE)).thenReturn(Optional.of(meterRole));
        when(meter.getUsagePoint(CREATED_DATE_TIME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(meter.getName()).thenReturn(METER_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.createLinkage();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_LINK_METER, MessageSeeds.SAME_USAGE_POINT_ALREADY_LINKED.getErrorCode(),
                    "Meter 'meterName' is already linked to usage point 'usagePointName' at the given time '2017-07-01T12:00:00+12:00'.");
            verifyZeroInteractions(usagePointMeterActivator);
            verify(usagePoint, never()).linkMeters();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Close linkage">
    @Test
    public void testCloseLinkage_byMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMeterActivations(EFFECTIVE_DATE_TIME)).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.clear(EFFECTIVE_DATE_TIME, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.closeLinkage();

        //Verify
        verify(usagePoint, times(1)).linkMeters();
        verify(usagePointMeterActivator, times(1)).clear(EFFECTIVE_DATE_TIME, meterRole);
        verify(usagePointMeterActivator, times(1)).complete();
        verifyResponse(response, HeaderType.Verb.CLOSED, ReplyType.Result.OK);
    }

    @Test
    public void testCloseLinkage_byName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withUsagePointMRID(null)
                .withMeterMRID(null)
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMeterActivations(EFFECTIVE_DATE_TIME)).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePointMeterActivator.clear(EFFECTIVE_DATE_TIME, meterRole)).thenReturn(usagePointMeterActivator);

        //Act
        linkageHandler.forMessage(message);
        MasterDataLinkageConfigResponseMessageType response = linkageHandler.closeLinkage();

        //Verify
        verify(usagePoint, times(1)).linkMeters();
        verify(usagePointMeterActivator, times(1)).clear(EFFECTIVE_DATE_TIME, meterRole);
        verify(usagePointMeterActivator, times(1)).complete();
        verifyResponse(response, HeaderType.Verb.CLOSED, ReplyType.Result.OK);
    }

    @Test
    public void testCloseLinkage_meterNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_METER_WITH_MRID.getErrorCode(),
                    "No meter is found by MRID 'meterMRID'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_meterNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withMeterMRID(null)
                .build();
        when(meteringService.findMeterByName(METER_NAME)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_METER_WITH_NAME.getErrorCode(),
                    "No meter is found by name 'meterName'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_usagePointNotFoundByMRID() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_USAGE_POINT_WITH_MRID.getErrorCode(),
                    "No usage point is found by MRID 'usagePointMRID'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_usagePointNotFoundByName() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage()
                .withUsagePointMRID(null)
                .build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.empty());

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
            failNoException();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.NO_USAGE_POINT_WITH_NAME.getErrorCode(),
                    "No usage point is found by name 'usagePointName'.");
            verify(usagePoint, never()).linkMeters();
        }
    }

    @Test
    public void testCloseLinkage_meterAndUsagePointNotLinked() throws Exception {
        //Prepare
        MasterDataLinkageConfigRequestMessageType message = getValidMessage().build();
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePointByMRID(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMeterActivations(EFFECTIVE_DATE_TIME)).thenReturn(Collections.emptyList());
        when(meter.getName()).thenReturn(METER_NAME);
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);

        //Act and verify
        try {
            linkageHandler.forMessage(message);
            linkageHandler.closeLinkage();
        } catch (FaultMessage e) {
            verifyFaultMessage(e, MessageSeeds.UNABLE_TO_UNLINK_METER, MessageSeeds.METER_AND_USAGE_POINT_NOT_LINKED.getErrorCode(),
                    "Meter 'meterName' is not linked to usage point 'usagePointName' at the given time '2017-07-05T12:00:00+12:00'.");
            verify(usagePoint, never()).linkMeters();
        }
    }
    //</editor-fold>

    private void verifyResponse(MasterDataLinkageConfigResponseMessageType response,
                                HeaderType.Verb expectedVerb,
                                ReplyType.Result expectedReplyType,
                                MessageSeeds... expectedWarnings) {
        assertThat(response.getHeader().getNoun()).isEqualTo("MasterDataLinkageConfig");
        assertThat(response.getHeader().getVerb()).isEqualTo(expectedVerb);
        assertThat(response.getReply().getResult()).isEqualTo(expectedReplyType);
        assertThat(response.getReply().getError()).hasSize(expectedWarnings.length);

        List<String> expectedCodes = Stream.of(expectedWarnings).map(MessageSeeds::getErrorCode).collect(Collectors.toList());
        response.getReply().getError().forEach(
                errorType -> {
                    assertThat(errorType.getLevel()).isEqualTo(ErrorType.Level.WARNING);
                    assertThat(expectedCodes).contains(errorType.getCode());
                });
    }

    @Override
    protected MasterDataLinkageMessageBuilder getValidMessage() {
        return MasterDataLinkageMessageBuilder.createEmptyMessage()
                .withCreatedDateTime(CREATED_DATE_TIME)
                .withEffectiveDateTime(EFFECTIVE_DATE_TIME)
                .withMeterMRID(METER_MRID)
                .withMeterName(METER_NAME)
                .withMeterRole(METER_ROLE)
                .withUsagePointMRID(USAGE_POINT_MRID)
                .withUsagePointName(USAGE_POINT_NAME);
    }

}
