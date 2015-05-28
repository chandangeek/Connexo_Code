package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.nls.Thesaurus;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileDestinationTest {


    @Mock
    private AppService appService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private AppServer appServer;
    @Mock
    private DataExportService dataExportService;
    @Mock
    FileDestination fileDestination;
    @Mock
    FormattedExportData data1;
    @Mock
    FormattedExportData data2;

    private List<FormattedExportData> data = new ArrayList<FormattedExportData>();

    private FileSystem fileSystem;

    @Before
    public void setUp() throws IOException {
        data.add(data1);
        data.add(data2);
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(dataExportService.getExportDirectory(appServer)).thenReturn(Optional.of(fileSystem.getPath("c:\\appserver\\export")));
        when(fileDestination.getFileName()).thenReturn("filename");
        when(fileDestination.getFileExtension()).thenReturn("csv");
        when(data1.getAppendablePayload()).thenReturn("blablablablabla1");
        when(data2.getAppendablePayload()).thenReturn("blablablablabla2");
    }

    @Test
    public void testExportToCsvWithAbsolutePath() {
        when(fileDestination.getFileLocation()).thenReturn("c:\\export");
        fileDestination.send(data);
        Path file = fileSystem.getPath("c:\\export", "filename.csv");
        assertThat(Files.exists(file)).isTrue();
    }

    @Test
    public void testExportToCsvWithRelativePath() {
        when(fileDestination.getFileLocation()).thenReturn("datadir");
        fileDestination.send(data);
        Path file = fileSystem.getPath("c:\\appserver\\export", "datadir", "filename.csv");
        assertThat(Files.exists(file)).isTrue();
    }


}

