package com.elster.jupiter.export.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.exception.MessageSeed;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class DataExportTaskResourceTest extends FelixRestApplicationJerseyTest {

    @Mock
    private RestQueryService restQueryService;
    @Mock
    private DataExportService dataExportService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private TimeService timeService;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        DataExportApplication application = new DataExportApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setDataExportService(dataExportService);
        application.setMeteringService(meteringService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setTimeService(timeService);

        return application;
    }

    @Test
    public void getTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();

        Entity<DataExportTaskInfo> json = Entity.json(info);
        Response response1 = target("/dataexporttask").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());


//        Response response = target("/export/dataexporttask").request().post(json);


//        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

//        verify(myTestOutboundPool1).removeOutboundComPort(any(OutboundComPort.class));
//        verify(myTestOutboundPool3).addOutboundComPort(any(OutboundComPort.class));
//        verify(myTestOutboundPool2, never()).addOutboundComPort(any(OutboundComPort.class));
//        verify(myTestOutboundPool2, never()).removeOutboundComPort(any(OutboundComPort.class));
    }

    @Test
    public void getCreateTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();

        Entity<DataExportTaskInfo> json = Entity.json(info);


        Response response = target("/dataexporttask").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

//        verify(myTestOutboundPool1).removeOutboundComPort(any(OutboundComPort.class));
//        verify(myTestOutboundPool3).addOutboundComPort(any(OutboundComPort.class));
//        verify(myTestOutboundPool2, never()).addOutboundComPort(any(OutboundComPort.class));
//        verify(myTestOutboundPool2, never()).removeOutboundComPort(any(OutboundComPort.class));
    }


}
