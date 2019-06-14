package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.rest.impl.DestinationSpecInfo;
import com.elster.jupiter.messaging.rest.impl.DestinationSpecTypeName;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DestinationSpecResourceTest extends MessagingApplicationTest {

    private Response response;
    private DestinationSpecInfo info;

    @Test
    public void testDoCreateDestinationSpec() {
        givenMessagingService4Create();
        givenDestinationSpecInfo("Expo", "DataExport");
        whenClientCallsPost("/destinationspec/", info);
        thenResponseStatusIs(Response.Status.OK.getStatusCode());
    }

    private void givenMessagingService4Create() {
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.getName()).thenReturn("DataExport");

        SubscriberSpec subscriberSpec = mock(SubscriberSpec.class);
        when(subscriberSpec.getDestination()).thenReturn(destinationSpec);

        QueueTableSpec queueTableSpec = mock(QueueTableSpec.class);
        when(queueTableSpec.createDestinationSpec(anyString(), anyInt(), anyInt(), eq(false), anyString(), eq(true), eq(false))).thenReturn(destinationSpec);

        when(messageService.getSubscribers()).thenReturn(Arrays.asList(subscriberSpec));
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.empty());
        when(messageService.createQueueTableSpec(anyString(), eq("RAW"), eq(false), eq(false))).thenReturn(queueTableSpec);
    }

    private void whenClientCallsPost(String uriPath, DestinationSpecInfo info) {
        response = target(uriPath).request().method("POST", Entity.json(info));
    }

    @Test
    public void testGetDestinationSpecTypeNames() {
        givenMessagingService(true);
        whenClientCallsGet("/destinationspec/queuetypenames");
        thenResponseStatusIs(Response.Status.OK.getStatusCode());
        thenResponseContainsDestinationSpecTypeNames();
    }

    private void givenMessagingService(boolean isDefault) {
        QueueTableSpec queueTableSpec = mock(QueueTableSpec.class);

        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.isExtraQueueCreationEnabled()).thenReturn(true);
        when(destinationSpec.isDefault()).thenReturn(isDefault);
        when(destinationSpec.getQueueTypeName()).thenReturn("DataExport");
        when(destinationSpec.getQueueTableSpec()).thenReturn(queueTableSpec);

        when(messageService.findDestinationSpecs()).thenReturn(Arrays.asList(destinationSpec));
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.of(destinationSpec));
    }

    private void whenClientCallsGet(String uriPath) {
        response = target(uriPath).request().get();
    }

    private void thenResponseStatusIs(int value) {
        assertEquals(value, response.getStatus());
    }

    private void thenResponseContainsDestinationSpecTypeNames() {
        DestinationSpecTypeName infos = response.readEntity(DestinationSpecTypeName.class);
        assertNotNull(infos);
        assertNotNull(infos.data);
        assertTrue(infos.data.size() > 0);
        assertEquals("DataExport", infos.data.get(0).name);
        assertEquals("DataExport", infos.data.get(0).value);
    }

    private void givenServiceCallService() {
        Finder<ServiceCallType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(new ArrayList<>());
        when(serviceCallService.getServiceCallTypes()).thenReturn(finder);
    }

    @Test
    public void testDeleteDestinationSpec() {
        givenMessagingService(false);
        givenDestinationSpecInfo("Expo", "DataExport");
        givenServiceCallService();
        whenClientCallsDelete("/destinationspec/Expo", info);
        thenResponseStatusIs(Response.Status.OK.getStatusCode());
    }

    private void givenDestinationSpecInfo(String name, String queueTypeName) {
        info = new DestinationSpecInfo();
        info.tasks = new ArrayList<>();
        info.name = name;
        info.queueTypeName = queueTypeName;
    }

    private void whenClientCallsDelete(String uriPath, DestinationSpecInfo info) {
        response = target(uriPath).request().method("DELETE", Entity.json(info));
    }

}
