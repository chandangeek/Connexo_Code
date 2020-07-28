/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import com.elster.jupiter.servicecall.DefaultState;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.enddevicecontrols.DateTimeInterval;
import ch.iec.tc57._2011.enddevicecontrols.EndDevice;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControl;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControls;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceTiming;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsPayloadType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsRequestMessageType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsResponseMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.SetMultimap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CancelEndDeviceControlsTest extends AbstractMockEndDeviceControls {

    public void setUp() throws Exception {
        super.setUp();

        when(subServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(deviceServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(deviceServiceCall2.getState()).thenReturn(DefaultState.FAILED);
    }

    @Test
    public void testCancelEndDeviceControlsFaultMessages() throws Exception {
        // No payload
        EndDeviceControlsRequestMessageType requestMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsRequestMessageType();

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CancelEndDeviceControls.Payload' is required.");

        //No EndDeviceControls
        EndDeviceControlsPayloadType payloadType = endDeviceControlsMessageObjectFactory.createEndDeviceControlsPayloadType();
        requestMessage.setPayload(payloadType);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CancelEndDeviceControls.Payload.EndDeviceControls' is required.");

        //Empty EndDeviceControls
        EndDeviceControls endDeviceControls = endDeviceControlsObjectFactory.createEndDeviceControls();
        payloadType.setEndDeviceControls(endDeviceControls);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'CancelEndDeviceControls.Payload.EndDeviceControls' can't be empty.");

        // No header
        EndDeviceControl endDeviceControl = endDeviceControlsObjectFactory.createEndDeviceControl();
        endDeviceControls.getEndDeviceControl().add(endDeviceControl);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CancelEndDeviceControls.Header' is required.");

        //No correlation id
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(HeaderType.Verb.CANCEL);
        requestMessage.setHeader(header);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CancelEndDeviceControls.Header.CorrelationID' is required.");

        header.setCorrelationID(CORRELATION_ID);

        //No Cim code
        EndDeviceControl.EndDeviceControlType endDeviceControlType = endDeviceControlsObjectFactory.createEndDeviceControlEndDeviceControlType();
        endDeviceControl.setEndDeviceControlType(endDeviceControlType);

        Map<String, String> codeToMessageMap = new HashMap<>();
        codeToMessageMap.put(MessageSeeds.COMMAND_CODE_MISSING.getErrorCode(), "The command CIM code is missing under EndDeviceControl[0].");
        codeToMessageMap.put(MessageSeeds.END_DEVICES_MISSING.getErrorCode(), "End devices are missing under EndDeviceControl[0].");

        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage), codeToMessageMap);

        //No End devices
        EndDeviceTiming endDeviceTiming = endDeviceControlsObjectFactory.createEndDeviceTiming();
        DateTimeInterval dateTimeInterval = endDeviceControlsObjectFactory.createDateTimeInterval();
        dateTimeInterval.setStart(PAST_DATE);
        endDeviceTiming.setInterval(dateTimeInterval);
        endDeviceControl.setPrimaryDeviceTiming(endDeviceTiming);

        codeToMessageMap.remove(MessageSeeds.RELEASE_DATE_MISSING.getErrorCode());
        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage), codeToMessageMap);

        //No MRID or Name of devices
        EndDevice endDevice = endDeviceControlsObjectFactory.createEndDevice();
        endDeviceControl.getEndDevices().add(endDevice);

        codeToMessageMap.remove(MessageSeeds.END_DEVICES_MISSING.getErrorCode());
        codeToMessageMap.put(MessageSeeds.MISSING_MRID_OR_NAME_FOR_END_DEVICE_CONTROL.getErrorCode(),
                "Either element 'mRID' or 'Names' is required under EndDeviceControl[0].EndDevices[0] for identification purpose.");
        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage), codeToMessageMap);
    }

    @Test
    public void testCancelEndDeviceControlsSuccessfully() throws Exception {
        EndDeviceControlsRequestMessageType requestMessage = createRequest();

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CANCELED);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        verify(deviceServiceCall).requestTransition(DefaultState.CANCELLED);
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testCancelEndDeviceControlsFailed() throws Exception {
        when(endDeviceControlsDomainExtension.getTriggerDate()).thenReturn(PAST_DATE);
        EndDeviceControlsRequestMessageType requestMessage = createRequest();

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CANCELED);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
        assertThat(response.getReply().getError().size()).isEqualTo(1);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM8011")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("For device under EndDeviceControl[0].EndDevices[0]: "
                        + "'Changes to the end device control request can't be applied after the processing has started or finished.'")));

        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testCancelEndDeviceControlsPartialSuccessfully() throws Exception {
        when(subServiceCall.newChildCall(deviceServiceCallType)).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.origin(anyString())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.extendedWith(any())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.targetObject(any())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.create()).thenReturn(deviceServiceCall2);

        when(subServiceCallFinder.stream()).then((i) -> Stream.of(deviceServiceCall, deviceServiceCall2));
        when(subServiceCall.findChildren()).thenReturn(subServiceCallFinder);

        EndDeviceControlsRequestMessageType requestMessage = createRequest();

        EndDevice endDevice2 = endDeviceControlsObjectFactory.createEndDevice();
        endDevice2.setMRID(END_DEVICE_MRID_2);
        requestMessage.getPayload().getEndDeviceControls().getEndDeviceControl().get(0).getEndDevices().add(endDevice2);

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.cancelEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CANCELED);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertThat(response.getReply().getError().size()).isEqualTo(1);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM8011")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("For device under EndDeviceControl[0].EndDevices[1]: "
                        + "'Changes to the end device control request can't be applied after the processing has started or finished.'")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    private EndDeviceControlsRequestMessageType createRequest() {
        EndDeviceControlsRequestMessageType requestMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsRequestMessageType();

        requestMessage.setHeader(createHeader(HeaderType.Verb.CANCEL));
        requestMessage.setPayload(createPayload(CIM_CODE));

        mockFindEndPointConfigurations();

        return requestMessage;
    }

    @Override
    String getFaultBasicMessage() {
        return "Unable to cancel end device controls.";
    }
}
