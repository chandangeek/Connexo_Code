/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.ObjectHolder;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.Action;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.UsagePointBuilder;
import com.elster.jupiter.cim.webservices.outbound.soap.FailedUsagePointOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyUsagePointConfigWebService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.usagepointconfig.Name;
import ch.iec.tc57._2011.usagepointconfig.NameType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointConfigMasterServiceCallHandlerTest {
    private static final String NAME = "my name";
    private static final String MRID = "my mrid";
    private UsagePointConfigMasterServiceCallHandler handler;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private WebServicesService webServicesService;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ObjectHolder<ReplyUsagePointConfigWebService> replyUsagePointConfigWebServiceHolder;
    @Mock
    private JsonService jsonService;
    @Mock
    private ReplyUsagePointConfigWebService replyWebService;
    @Mock
    private EndPointConfiguration endPointConfiguration;
    @Mock
    private UsagePointConfigDomainExtension childExtensionNoMrid;
    @Mock
    private UsagePointConfigDomainExtension childExtensionWithMridAndName;
    @Mock
    private UsagePointConfigMasterDomainExtension extension;
    @Mock
    private Finder<ServiceCall> finder;
    @Mock
    private ServiceCall successChildServiceCallNoMrid;
    @Mock
    private ServiceCall successChildServiceCallWithMridAndName;
    @Mock
    private ServiceCall failureChildServiceCallNoMrid;
    @Mock
    private ServiceCall failureChildServiceCallWithMridAndName;
    @Mock
    private ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointNoMrid;
    @Mock
    private ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointWithMridAndName;
    @Mock
    private UsagePoint meterUsagePoint;

    @Before
    public void setup() {
        handler = new UsagePointConfigMasterServiceCallHandler(endPointConfigurationService,
                replyUsagePointConfigWebServiceHolder, jsonService, meteringService, thesaurus, webServicesService);
        String usagePointConfigNoMrid = "some json without mrid";
        when(jsonService.deserialize(usagePointConfigNoMrid, ch.iec.tc57._2011.usagepointconfig.UsagePoint.class))
                .thenReturn(usagePointNoMrid);
        String usagePointConfigWithMridAndName = "some json with mrid and name";
        when(jsonService.deserialize(usagePointConfigWithMridAndName,
                ch.iec.tc57._2011.usagepointconfig.UsagePoint.class)).thenReturn(usagePointWithMridAndName);
        when(serviceCall.findChildren()).thenReturn(finder);
        when(finder.stream()).thenAnswer(new Answer<Stream<ServiceCall>>() {
            @Override
            public Stream<ServiceCall> answer(InvocationOnMock invocation) throws Throwable {
                return Stream.of(successChildServiceCallNoMrid, successChildServiceCallNoMrid,
                        successChildServiceCallWithMridAndName, failureChildServiceCallNoMrid,
                        failureChildServiceCallWithMridAndName);
            }
        });
        when(successChildServiceCallNoMrid.getExtension(UsagePointConfigDomainExtension.class))
                .thenReturn(Optional.of(childExtensionNoMrid));
        when(successChildServiceCallWithMridAndName.getExtension(UsagePointConfigDomainExtension.class))
                .thenReturn(Optional.of(childExtensionWithMridAndName));
        when(failureChildServiceCallNoMrid.getExtension(UsagePointConfigDomainExtension.class))
                .thenReturn(Optional.of(childExtensionNoMrid));
        when(failureChildServiceCallWithMridAndName.getExtension(UsagePointConfigDomainExtension.class))
                .thenReturn(Optional.of(childExtensionWithMridAndName));

        when(successChildServiceCallNoMrid.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(successChildServiceCallWithMridAndName.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(failureChildServiceCallNoMrid.getState()).thenReturn(DefaultState.FAILED);
        when(failureChildServiceCallWithMridAndName.getState()).thenReturn(DefaultState.FAILED);

        when(childExtensionNoMrid.getUsagePoint()).thenReturn(usagePointConfigNoMrid);
        when(childExtensionWithMridAndName.getUsagePoint()).thenReturn(usagePointConfigWithMridAndName);

        Name name = new Name();
        NameType nameType = new NameType();
        nameType.setName(UsagePointBuilder.USAGE_POINT_NAME);
        name.setNameType(nameType);
        name.setName(NAME);
        when(usagePointWithMridAndName.getMRID()).thenReturn(MRID);
        when(usagePointWithMridAndName.getNames()).thenReturn(Arrays.asList(name));
        when(usagePointNoMrid.getNames()).thenReturn(Arrays.asList(name, name));
        when(meteringService.findUsagePointByName(NAME)).thenReturn(Optional.of(meterUsagePoint));
        when(meteringService.findUsagePointByMRID(MRID)).thenReturn(Optional.of(meterUsagePoint));

        when(extension.getExpectedNumberOfCalls()).thenReturn(BigDecimal.valueOf(5));
    }

    @Test
    public void testSendReplyForCreate() throws FaultMessage {
        when(childExtensionNoMrid.getOperation()).thenReturn(Action.CREATE.name());
        when(childExtensionWithMridAndName.getOperation()).thenReturn(Action.CREATE.name());

        handler.sendReply(replyWebService, endPointConfiguration, serviceCall, extension);
    }

    @Test
    public void testSendReplyForUpdate() throws FaultMessage {
        when(childExtensionNoMrid.getOperation()).thenReturn(Action.UPDATE.name());
        when(childExtensionWithMridAndName.getOperation()).thenReturn(Action.UPDATE.name());
        doAnswer(new Answer<Void>() {

            @SuppressWarnings("unchecked")
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                EndPointConfiguration configuration = invocation.getArgumentAt(0, EndPointConfiguration.class);
                assertThat(configuration).isSameAs(endPointConfiguration);

                List<UsagePoint> successList = invocation.getArgumentAt(2, List.class);
                assertThat(successList).hasSize(3);

                List<FailedUsagePointOperation> failureList = invocation.getArgumentAt(3, List.class);
                assertThat(failureList).hasSize(2);

                // failureChildServiceCallNoMrid
                assertThat(failureList.get(0).getUsagePointMrid()).isNull();
                assertThat(failureList.get(0).getUsagePointName()).isEqualTo(NAME);

                // failureChildServiceCallWithMridAndName
                assertThat(failureList.get(1).getUsagePointMrid()).isEqualTo(MRID);
                assertThat(failureList.get(1).getUsagePointName()).isEqualTo(NAME);

                return null;
            }
        }).when(replyWebService).call(any(EndPointConfiguration.class), eq(Action.UPDATE.name()),
                anyListOf(UsagePoint.class), anyListOf(FailedUsagePointOperation.class), eq(BigDecimal.valueOf(5)));

        handler.sendReply(replyWebService, endPointConfiguration, serviceCall, extension);

        verify(replyWebService).call(any(EndPointConfiguration.class), eq(Action.UPDATE.name()),
                anyListOf(UsagePoint.class), anyListOf(FailedUsagePointOperation.class), eq(BigDecimal.valueOf(5)));
    }
}
