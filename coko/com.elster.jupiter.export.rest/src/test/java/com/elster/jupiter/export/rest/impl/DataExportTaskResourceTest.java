package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.FileDestination;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataExportTaskResourceTest extends DataExportApplicationJerseyTest {

    public static final ZonedDateTime NEXT_EXECUTION = ZonedDateTime.of(2015, 1, 13, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final int TASK_ID = 750;
    @Mock
    private FileDestination newDestination;

    @Test
    public void getTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();

        Response response1 = target("/dataexporttask").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        DataExportTaskInfos infos = response1.readEntity(DataExportTaskInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.dataExportTasks).hasSize(1);
    }

    @Test
    public void triggerTaskTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();

        Response response1 = target("/dataexporttask/"+TASK_ID+"/trigger").request().post(Entity.json(null));
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(exportTask).triggerNow();
    }

    @Test
    public void createTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.name = "newName";
        info.nextRun = 250L;
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.deviceGroup = new MeterGroupInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        info.dataSelector = new SelectorInfo();
        info.dataSelector.name = "Standard Data Selector";
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);

        DestinationInfo emailDestinationInfo = new DestinationInfo();
        emailDestinationInfo.type = DestinationType.EMAIL;
        emailDestinationInfo.fileName = "attachment";
        emailDestinationInfo.fileExtension = "csv";
        emailDestinationInfo.recipients="user1@elster.com,user2@elster.com";
        emailDestinationInfo.subject="daily report";
        info.destinations.add(emailDestinationInfo);

        DestinationInfo ftpDestinationInfo = new DestinationInfo();
        ftpDestinationInfo.type = DestinationType.FTP;
        ftpDestinationInfo.fileLocation = "";
        ftpDestinationInfo.fileName = "ftpfile";
        ftpDestinationInfo.fileExtension = "ftptxt";
        ftpDestinationInfo.server = "ftpserver";
        ftpDestinationInfo.password = "ftppassword";
        ftpDestinationInfo.user = "ftpuser";
        info.destinations.add(ftpDestinationInfo);

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(exportTask).addFileDestination("", "file", "txt");
        verify(exportTask).addEmailDestination("user1@elster.com,user2@elster.com", "daily report", "attachment", "csv");
        verify(exportTask).addFtpDestination("ftpserver", "ftpuser", "ftppassword", "", "ftpfile", "ftptxt");
    }

    @Test
    public void updateTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.deviceGroup = new MeterGroupInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        info.dataSelector = new SelectorInfo();
        info.dataSelector.name = "Standard Data Selector";


        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask/" + TASK_ID).request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void updateTaskDestinationsTest() {
        EmailDestination obsolete = mock(EmailDestination.class);
        EmailDestination toUpdate = mock(EmailDestination.class);
        when(exportTask.getDestinations()).thenReturn(Arrays.asList(obsolete, toUpdate));
        when(obsolete.getId()).thenReturn(7772L);
        when(toUpdate.getId()).thenReturn(7773L);
        when(exportTask.addFileDestination("", "file", "txt")).thenReturn(newDestination);

        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.name = "newName";
        info.nextRun = 250L;
        info.dataSelector = new SelectorInfo();
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.id = 0; // new
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);
        DestinationInfo emailDestinationInfo = new DestinationInfo();
        emailDestinationInfo.id = 7773L;
        emailDestinationInfo.type = DestinationType.EMAIL;
        emailDestinationInfo.fileName = "attachment";
        emailDestinationInfo.fileExtension = "csv";
        emailDestinationInfo.recipients="user1@elster.com,user2@elster.com";
        emailDestinationInfo.subject="daily report";
        info.destinations.add(emailDestinationInfo);

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask/" + TASK_ID).request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(exportTask).removeDestination(obsolete);
        verify(exportTask, never()).removeDestination(newDestination);
        verify(toUpdate).setRecipients("user1@elster.com,user2@elster.com");
        verify(toUpdate).setSubject("daily report");
        verify(toUpdate).setAttachmentName("attachment");
        verify(toUpdate).setAttachmentExtension("csv");
        verify(toUpdate).save();
        verify(exportTask).addFileDestination("", "file", "txt");
    }


}
