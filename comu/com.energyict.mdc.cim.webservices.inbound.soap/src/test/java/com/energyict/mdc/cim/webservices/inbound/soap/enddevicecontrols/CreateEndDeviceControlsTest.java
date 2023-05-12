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

public class CreateEndDeviceControlsTest extends AbstractMockEndDeviceControls {

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreateEndDeviceControlsFaultMessages() throws Exception {
        // No payload
        EndDeviceControlsRequestMessageType requestMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsRequestMessageType();

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Payload' is required.");

        //No EndDeviceControls
        EndDeviceControlsPayloadType payloadType = endDeviceControlsMessageObjectFactory.createEndDeviceControlsPayloadType();
        requestMessage.setPayload(payloadType);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Payload.EndDeviceControls' is required.");

        //Empty EndDeviceControls
        EndDeviceControls endDeviceControls = endDeviceControlsObjectFactory.createEndDeviceControls();
        payloadType.setEndDeviceControls(endDeviceControls);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'CreateEndDeviceControls.Payload.EndDeviceControls' can't be empty.");

        // No header
        EndDeviceControl endDeviceControl = endDeviceControlsObjectFactory.createEndDeviceControl();
        endDeviceControls.getEndDeviceControl().add(endDeviceControl);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Header' is required.");

        //Synchronous mode
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(HeaderType.Verb.CREATE);
        requestMessage.setHeader(header);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.SYNC_MODE_NOT_SUPPORTED_GENERAL.getErrorCode(),
                "Synchronous mode isn't supported.");

        header.setAsyncReplyFlag(true);

        //No correlation id
        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Header.CorrelationID' is required.");

        header.setCorrelationID(CORRELATION_ID);

        //No reply address
        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Header.ReplyAddress' is required.");

        header.setReplyAddress(REPLY_ADDRESS);

        //No endpoint configuration
        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ENDPOINT.getErrorCode(),
                "Endpoint for CIM SendEndDeviceEvents isn't found by URL 'some_url'.");

        mockFindEndPointConfigurations();

        //No Cim code
        EndDeviceControl.EndDeviceControlType endDeviceControlType = endDeviceControlsObjectFactory.createEndDeviceControlEndDeviceControlType();
        endDeviceControl.setEndDeviceControlType(endDeviceControlType);

        Map<String, String> codeToMessageMap = new HashMap<>();
        codeToMessageMap.put(MessageSeeds.COMMAND_CODE_MISSING.getErrorCode(), "The command CIM code is missing under EndDeviceControl[0].");
        codeToMessageMap.put(MessageSeeds.RELEASE_DATE_MISSING.getErrorCode(), "The release date is missing under EndDeviceControl[0].");
        codeToMessageMap.put(MessageSeeds.END_DEVICES_MISSING.getErrorCode(), "End devices are missing under EndDeviceControl[0].");

        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage), codeToMessageMap);

        //No release date
        endDeviceControlType.setRef(CIM_CODE);

        codeToMessageMap.remove(MessageSeeds.COMMAND_CODE_MISSING.getErrorCode());
        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage), codeToMessageMap);

        //No End devices
        EndDeviceTiming endDeviceTiming = endDeviceControlsObjectFactory.createEndDeviceTiming();
        DateTimeInterval dateTimeInterval = endDeviceControlsObjectFactory.createDateTimeInterval();
        dateTimeInterval.setStart(PAST_DATE);
        endDeviceTiming.setInterval(dateTimeInterval);
        endDeviceControl.setPrimaryDeviceTiming(endDeviceTiming);

        codeToMessageMap.remove(MessageSeeds.RELEASE_DATE_MISSING.getErrorCode());
        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage), codeToMessageMap);

        //No MRID or Name or SerialNumber of devices
        EndDevice endDevice = endDeviceControlsObjectFactory.createEndDevice();
        endDeviceControl.getEndDevices().add(endDevice);

        codeToMessageMap.remove(MessageSeeds.END_DEVICES_MISSING.getErrorCode());
        codeToMessageMap.put(MessageSeeds.MISSING_MRID_OR_NAME_OR_SERIALNUMBER_FOR_END_DEVICE_CONTROL.getErrorCode(),
                "Either element 'mRID' or 'Names' or 'serialNumber' is required under EndDeviceControl[0].EndDevices[0] for identification purpose.");
        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage), codeToMessageMap);
    }

    @Test
    public void testCreateEndDeviceControlsSuccessfully() throws Exception {
        when(subServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(deviceServiceCall.getState()).thenReturn(DefaultState.WAITING);

        EndDeviceControlsRequestMessageType requestMessage = createRequest(CIM_CODE);

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testCreateEndDeviceControlsFailed() throws Exception {
        when(subServiceCall.getState()).thenReturn(DefaultState.FAILED);
        when(deviceServiceCall.getState()).thenReturn(DefaultState.FAILED);

        EndDeviceControlsRequestMessageType requestMessage = createRequest(INCORRECT_CIM_CODE);

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
        assertThat(response.getReply().getError().size()).isEqualTo(1);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM8010")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("For command under EndDeviceControl[0]: 'End device control type with CIM code '1.99.99.99' isn't supported.'")));

        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testCreateEndDeviceControlsPartialSuccessfully() throws Exception {
        when(subServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(deviceServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(subServiceCall2.getState()).thenReturn(DefaultState.FAILED);

        when(masterServiceCall.newChildCall(subServiceCallType)).thenReturn(subServiceCallBuilder);
        when(subServiceCallBuilder.origin(anyString())).thenReturn(subServiceCallBuilder);
        when(subServiceCallBuilder.extendedWith(any())).thenReturn(subServiceCallBuilder);
        when(subServiceCallBuilder.targetObject(any())).thenReturn(subServiceCallBuilder);
        when(subServiceCallBuilder.create()).thenReturn(subServiceCall2);

        when(masterServiceCallFinder.stream()).then((i) -> Stream.of(subServiceCall, subServiceCall2));
        when(masterServiceCall.findChildren()).thenReturn(subServiceCallFinder);

        when(subServiceCall2.newChildCall(deviceServiceCallType)).thenReturn(childServiceCallBuilder);
        when(subServiceCallFinder.stream()).then((i) -> Stream.of(deviceServiceCall));
        when(subServiceCall2.findChildren()).thenReturn(subServiceCallFinder);

        EndDeviceControlsRequestMessageType requestMessage = createRequest(CIM_CODE);
        EndDeviceControl endDeviceControl = endDeviceControlsObjectFactory.createEndDeviceControl();

        EndDeviceControl.EndDeviceControlType endDeviceControlType = endDeviceControlsObjectFactory.createEndDeviceControlEndDeviceControlType();
        endDeviceControlType.setRef(INCORRECT_CIM_CODE);
        endDeviceControl.setEndDeviceControlType(endDeviceControlType);
        EndDevice endDevice1 = endDeviceControlsObjectFactory.createEndDevice();
        endDevice1.setMRID(END_DEVICE_MRID);

        EndDeviceTiming endDeviceTiming = endDeviceControlsObjectFactory.createEndDeviceTiming();
        DateTimeInterval dateTimeInterval = endDeviceControlsObjectFactory.createDateTimeInterval();
        dateTimeInterval.setStart(PAST_DATE);
        endDeviceTiming.setInterval(dateTimeInterval);
        endDeviceControl.setPrimaryDeviceTiming(endDeviceTiming);

        endDeviceControl.getEndDevices().add(endDevice1);

        requestMessage.getPayload().getEndDeviceControls().getEndDeviceControl().add(endDeviceControl);

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertThat(response.getReply().getError().size()).isEqualTo(1);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM8010")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("For command under EndDeviceControl[1]: 'End device control type with CIM code '1.99.99.99' isn't supported.'")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    private EndDeviceControlsRequestMessageType createRequest(String ref) {
        EndDeviceControlsRequestMessageType requestMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsRequestMessageType();

        HeaderType header = createHeader(HeaderType.Verb.CREATE);
        header.setReplyAddress(REPLY_ADDRESS);
        requestMessage.setHeader(header);

        requestMessage.setPayload(createPayload(PAST_DATE, ref));

        mockFindEndPointConfigurations();

        return requestMessage;
    }

    @Override
    String getFaultBasicMessage() {
        return "Unable to create end device controls.";
    }
}
