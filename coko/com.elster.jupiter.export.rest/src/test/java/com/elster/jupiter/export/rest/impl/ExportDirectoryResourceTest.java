package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExportDirectoryResourceTest extends DataExportApplicationJerseyTest {

    @Test
    public void testJsonModel(){
        String name = "appServ";
        AppServer appServer = mock(AppServer.class);
        when(appServer.getName()).thenReturn(name);
        when(appService.findAppServer(name)).thenReturn(Optional.of(appServer));
        Path path = Paths.get("/some");
        Map<AppServer, Path> allDirs = new HashMap<>();
        allDirs.put(appServer, path);
        when(dataExportService.getAllExportDirecties()).thenReturn(allDirs);
        String response = target("/exportdirs").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<List>get("$.directories")).isNotEmpty();
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.directories[0].appServerName")).isEqualTo(name);
        assertThat(model.<String>get("$.directories[0].directory")).isEqualTo("\\some");
    }

    @Test
    public void testRemoveExportDir(){
        String name = "appServ";
        AppServer appServer = mock(AppServer.class);
        when(appService.findAppServer(name)).thenReturn(Optional.of(appServer));
        Response response = target("/exportdirs/appServ").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
