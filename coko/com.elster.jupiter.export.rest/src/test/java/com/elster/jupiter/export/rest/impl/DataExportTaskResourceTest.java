package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FileDestination;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataExportTaskResourceTest extends DataExportApplicationJerseyTest {

    private static final int TASK_ID = 750;
    private static final long OK_VERSION = 41L;
    private static final long BAD_VERSION = 35L;

    @Mock
    private FileDestination newDestination;

    @Test
    public void getTasksTest() {
        Response response1 = target("/dataexporttask").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        DataExportTaskInfos infos = response1.readEntity(DataExportTaskInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.dataExportTasks).hasSize(1);
    }

    @Test
    public void triggerTaskTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.version = OK_VERSION;

        Response response1 = target("/dataexporttask/"+TASK_ID+"/trigger").request().put(Entity.json(info));
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
        info.dataSelector.name = "Device readings data selector";
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
        emailDestinationInfo.recipients="user1@elster.com;user2@elster.com";
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
        ftpDestinationInfo.port = 21;
        info.destinations.add(ftpDestinationInfo);

        DestinationInfo ftpsDestinationInfo = new DestinationInfo();
        ftpsDestinationInfo.type = DestinationType.FTPS;
        ftpsDestinationInfo.fileLocation = "";
        ftpsDestinationInfo.fileName = "ftpsfile";
        ftpsDestinationInfo.fileExtension = "ftpstxt";
        ftpsDestinationInfo.server = "ftpsserver";
        ftpsDestinationInfo.password = "ftpspassword";
        ftpsDestinationInfo.user = "ftpsuser";
        ftpsDestinationInfo.port = 20;
        info.destinations.add(ftpsDestinationInfo);

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(exportTask).addFileDestination("", "file", "txt");
        verify(exportTask).addEmailDestination("user1@elster.com;user2@elster.com", "daily report", "attachment", "csv");
        verify(exportTask).addFtpDestination("ftpserver", 21, "ftpuser", "ftppassword", "", "ftpfile", "ftptxt");
        verify(exportTask).addFtpsDestination("ftpsserver", 20, "ftpsuser", "ftpspassword", "", "ftpsfile", "ftpstxt");
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
        info.version = OK_VERSION;
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.id = 0; // new
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);

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
        info.version = OK_VERSION;
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
        emailDestinationInfo.recipients="user1@elster.com;user2@elster.com";
        emailDestinationInfo.subject="daily report";
        info.destinations.add(emailDestinationInfo);

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask/" + TASK_ID).request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(exportTask).removeDestination(obsolete);
        verify(exportTask, never()).removeDestination(newDestination);
        verify(toUpdate).setRecipients("user1@elster.com;user2@elster.com");
        verify(toUpdate).setSubject("daily report");
        verify(toUpdate).setAttachmentName("attachment");
        verify(toUpdate).setAttachmentExtension("csv");
        verify(toUpdate).save();
        verify(exportTask).addFileDestination("", "file", "txt");
    }


    @Test
    public void updateTaskBadVersion() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.name = "newName";
        info.nextRun = 250L;
        info.dataSelector = new SelectorInfo();
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        info.version = BAD_VERSION;

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask/" + TASK_ID).request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void deleteTaskBadVersion() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.name = "newName";
        info.version = BAD_VERSION;
        Entity<DataExportTaskInfo> json = Entity.json(info);
        Response response = target("/dataexporttask/" + TASK_ID).request().build(HttpMethod.DELETE, json).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void triggerNowBadVersion() {
        ExportTask exportTask = mock(ExportTask.class);
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.name = "newName";
        info.version = BAD_VERSION;
        Entity<DataExportTaskInfo> json = Entity.json(info);
        Response response = target("/dataexporttask/" + TASK_ID + "/trigger").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(exportTask, never()).triggerNow();
    }
}
