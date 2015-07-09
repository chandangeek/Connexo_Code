package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.export.TextLineExportData;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalFileWriterTest {

    private static final ZonedDateTime NOW = ZonedDateTime.of(2014, 6, 14, 9, 40, 43, 617508339, TimeZoneNeutral.getMcMurdo());

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    private FileSystem fileSystem = Jimfs.newFileSystem(Configuration.windows());
    private Clock clock = Clock.fixed(NOW.toInstant(), TimeZoneNeutral.getMcMurdo());
    private Path tempDirectory;

    @Mock
    private IDataExportService dataExportService;

    @Before
    public void setUp() throws IOException {
        tempDirectory = fileSystem.getPath("C:/temp");
        when(dataExportService.getTempDirectory()).thenReturn(tempDirectory);

        Files.createDirectory(tempDirectory);
    }

    @After
    public void tearDown() {

    }
    @Test
    public void testWriteToMultipleFiles() throws IOException {
        StructureMarker file1 = DefaultStructureMarker.createRoot(clock, "file1");
        FormattedExportData line1 = TextLineExportData.of(file1, "line1\n");
        FormattedExportData line2 = TextLineExportData.of(file1, "line2\n");
        StructureMarker file2 = DefaultStructureMarker.createRoot(clock, "file2");
        FormattedExportData line3 = TextLineExportData.of(file2, "line3\n");
        FormattedExportData line4 = TextLineExportData.of(file2, "line4\n");

        LocalFileWriter localFileWriter = new LocalFileWriter(dataExportService);

        Map<StructureMarker, Path> map = localFileWriter.writeToTempFiles(Arrays.asList(line1, line2, line3, line4));

        assertThat(map)
                .hasSize(2)
                .containsKey(file1)
                .containsKey(file2);

        assertThat(getContents(map.get(file1))).isEqualTo("line1\nline2\n");
        assertThat(getContents(map.get(file2))).isEqualTo("line3\nline4\n");

    }

    private String getContents(Path file) throws IOException {
        int bytes = (int) Files.size(file);
        try(Reader reader = new InputStreamReader(Files.newInputStream(file))) {
            char[] all = new char[bytes];
            reader.read(all);
            return new String(all);
        }
    }


}