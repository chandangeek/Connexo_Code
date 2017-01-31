/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.master.data.rest.impl;


import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.rest.LogBookTypeInfo;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogBookTypeResourceTest extends MasterDataApplicationJerseyTest {

    public static final long OK_VERSION = 11;
    public static final long BAD_VERSION = 8;
    public static final long LOGBOOK_ID = 1L;

    private LogBookType mockLogBookType() {
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getId()).thenReturn(LOGBOOK_ID);
        when(logBookType.getName()).thenReturn("LogBook 1");
        when(logBookType.getVersion()).thenReturn(OK_VERSION);
        when(logBookType.getDescription()).thenReturn("Default description");
        when(logBookType.getObisCode()).thenReturn(new ObisCode(1,2,3,4,5,1,true));

        when(masterDataService.findLogBookType(LOGBOOK_ID)).thenReturn(Optional.of(logBookType));
        when(masterDataService.findAndLockLogBookTypeByIdAndVersion(LOGBOOK_ID, OK_VERSION)).thenReturn(Optional.of(logBookType));
        when(masterDataService.findAndLockLogBookTypeByIdAndVersion(LOGBOOK_ID, BAD_VERSION)).thenReturn(Optional.empty());
        return logBookType;
    }

    @Test
    public void testUpdateLogBookTypeOkVersion() {
        LogBookType logBook = mockLogBookType();
        LogBookTypeInfo info = new LogBookTypeInfo(logBook);
        info.name = "new name";
        Response response = target("/logbooktypes/" + LOGBOOK_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(logBook, times(1)).setName("new name");
    }

    @Test
    public void testUpdateLogBookTypeBadVersion() {
        LogBookType logBook = mockLogBookType();
        LogBookTypeInfo info = new LogBookTypeInfo(logBook);
        info.name = "new name";
        info.version = BAD_VERSION;
        Response response = target("/logbooktypes/" + LOGBOOK_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(logBook, never()).setName("new name");
    }

    @Test
    public void testDeleteLogBookTypeOkVersion() {
        LogBookType logBook = mockLogBookType();
        LogBookTypeInfo info = new LogBookTypeInfo(logBook);
        info.name = "new name";
        Response response = target("/logbooktypes/" + LOGBOOK_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(logBook).delete();
    }

    @Test
    public void testDeleteLogBookTypeBadVersion() {
        LogBookType logBook = mockLogBookType();
        LogBookTypeInfo info = new LogBookTypeInfo(logBook);
        info.name = "new name";
        info.version = BAD_VERSION;
        Response response = target("/logbooktypes/" + LOGBOOK_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(logBook, never()).delete();
    }
}
