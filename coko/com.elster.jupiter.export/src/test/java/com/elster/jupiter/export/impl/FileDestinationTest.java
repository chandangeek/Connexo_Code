package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
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

    public static final String DATA1 = "blablablablabla1";
    public static final String DATA2 = "blablablablabla2";
    public static final String APPSERVER_PATH = "c:\\appserver\\export";
    public static final String FILENAME = "filename";
    public static final String EXTENSION = "txt";
    public static final String ABSOLUTE_DIR = "c:\\export";
    public static final String RELATIVE_DIR = "datadir";



    @Mock
    private AppService appService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private AppServer appServer;
    @Mock
    private DataExportService dataExportService;
    @Mock
    FormattedExportData data1;
    @Mock
    FormattedExportData data2;
    @Mock
    DataModel dataModel;

    private List<FormattedExportData> data = new ArrayList<FormattedExportData>();

    private FileSystem fileSystem;

    @Before
    public void setUp() throws IOException {
        data.add(data1);
        data.add(data2);
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(dataExportService.getExportDirectory(appServer)).thenReturn(Optional.of(fileSystem.getPath(APPSERVER_PATH)));
        when(data1.getAppendablePayload()).thenReturn(DATA1);
        when(data2.getAppendablePayload()).thenReturn(DATA2);
    }

    @Test
    public void testExportToCsvWithAbsolutePath() {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, thesaurus, dataExportService, appService,fileSystem);
        fileDestination.init(FILENAME, EXTENSION, ABSOLUTE_DIR);
        fileDestination.send(data);
        Path file = fileSystem.getPath(ABSOLUTE_DIR, FILENAME + "." + EXTENSION);
        assertThat(Files.exists(file)).isTrue();
    }

    @Test
    public void testExportToCsvWithRelativePath() {
        FileDestinationImpl fileDestination = new FileDestinationImpl(dataModel, thesaurus, dataExportService, appService, fileSystem);
        fileDestination.init(FILENAME, EXTENSION, RELATIVE_DIR);
        fileDestination.send(data);
        Path file = fileSystem.getPath(APPSERVER_PATH, RELATIVE_DIR, FILENAME + "." + EXTENSION);
        assertThat(Files.exists(file)).isTrue();
    }


}

