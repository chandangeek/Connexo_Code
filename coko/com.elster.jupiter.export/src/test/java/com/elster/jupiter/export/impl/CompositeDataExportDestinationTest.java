/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompositeDataExportDestinationTest {
    @Mock
    private EmailDestinationImpl emailDestination;
    @Mock
    private FtpDestinationImpl ftpDestination;
    @Mock
    private FtpsDestinationImpl ftpsDestination;
    @Mock
    private SftpDestinationImpl sftpDestination;
    @Mock
    private LocalFileDestinationImpl localFileDestination;
    @Mock
    private WebServiceDestinationImpl webServiceDestination;
    @Mock
    private ExportData exportData;
    @Mock
    private Clock clock;
    @Mock
    private Path path;
    @Mock
    private TagReplacerFactory tagReplacerFactory;
    private Logger logger = Logger.getAnonymousLogger();
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private List<ExportData> data;
    private Map<StructureMarker, Path> files;

    private CompositeDataExportDestination compositeDataExportDestination;
    private CompositeDataExportDestination testInstance;

    @Before
    public void setUp() {
        data = Collections.singletonList(exportData);
        files = Collections.singletonMap(DefaultStructureMarker.createRoot(clock, "create"), path);

        when(emailDestination.getType()).thenReturn(Destination.Type.FILE);
        when(ftpDestination.getType()).thenReturn(Destination.Type.FILE);
        when(ftpsDestination.getType()).thenReturn(Destination.Type.FILE);
        when(sftpDestination.getType()).thenReturn(Destination.Type.FILE);
        when(localFileDestination.getType()).thenReturn(Destination.Type.FILE);
        when(webServiceDestination.getType()).thenReturn(Destination.Type.DATA);
        compositeDataExportDestination = new CompositeDataExportDestination(localFileDestination, webServiceDestination);
    }

    @Test
    public void testGetters() {
        testInstance = new CompositeDataExportDestination(localFileDestination);
        assertThat(testInstance.hasDataDestinations()).isFalse();
        assertThat(testInstance.hasFileDestinations()).isTrue();
        assertThat(testInstance.getType()).isSameAs(Destination.Type.COMPOSITE);
        testInstance = new CompositeDataExportDestination(localFileDestination, emailDestination, ftpDestination, ftpsDestination, sftpDestination);
        assertThat(testInstance.hasDataDestinations()).isFalse();
        assertThat(testInstance.hasFileDestinations()).isTrue();
        assertThat(testInstance.getType()).isSameAs(Destination.Type.COMPOSITE);
        testInstance = new CompositeDataExportDestination(testInstance);
        assertThat(testInstance.hasDataDestinations()).isFalse();
        assertThat(testInstance.hasFileDestinations()).isTrue();
        assertThat(testInstance.getType()).isSameAs(Destination.Type.COMPOSITE);
        testInstance = new CompositeDataExportDestination(webServiceDestination, ftpsDestination);
        assertThat(testInstance.hasDataDestinations()).isTrue();
        assertThat(testInstance.hasFileDestinations()).isTrue();
        assertThat(testInstance.getType()).isSameAs(Destination.Type.COMPOSITE);
        testInstance = new CompositeDataExportDestination(compositeDataExportDestination);
        assertThat(testInstance.hasDataDestinations()).isTrue();
        assertThat(testInstance.hasFileDestinations()).isTrue();
        assertThat(testInstance.getType()).isSameAs(Destination.Type.COMPOSITE);
        testInstance = new CompositeDataExportDestination(webServiceDestination);
        assertThat(testInstance.hasDataDestinations()).isTrue();
        assertThat(testInstance.hasFileDestinations()).isFalse();
        assertThat(testInstance.getType()).isSameAs(Destination.Type.COMPOSITE);
        testInstance = new CompositeDataExportDestination(testInstance);
        assertThat(testInstance.hasDataDestinations()).isTrue();
        assertThat(testInstance.hasFileDestinations()).isFalse();
        assertThat(testInstance.getType()).isSameAs(Destination.Type.COMPOSITE);
    }

    @Test
    public void testSendData() {
        testInstance = new CompositeDataExportDestination(emailDestination,
                ftpDestination, ftpsDestination, sftpDestination, compositeDataExportDestination);
        testInstance.send(data, tagReplacerFactory, logger);
        verify(emailDestination, never()).send(anyMapOf(StructureMarker.class, Path.class), any(TagReplacerFactory.class), any(Logger.class), any(Thesaurus.class));
        verify(ftpDestination, never()).send(anyMapOf(StructureMarker.class, Path.class), any(TagReplacerFactory.class), any(Logger.class), any(Thesaurus.class));
        verify(ftpsDestination, never()).send(anyMapOf(StructureMarker.class, Path.class), any(TagReplacerFactory.class), any(Logger.class), any(Thesaurus.class));
        verify(sftpDestination, never()).send(anyMapOf(StructureMarker.class, Path.class), any(TagReplacerFactory.class), any(Logger.class), any(Thesaurus.class));
        verify(localFileDestination, never()).send(anyMapOf(StructureMarker.class, Path.class), any(TagReplacerFactory.class), any(Logger.class), any(Thesaurus.class));
        verify(webServiceDestination).send(data, tagReplacerFactory, logger);
    }

    @Test
    public void testSendFiles() {
        testInstance = new CompositeDataExportDestination(emailDestination,
                ftpDestination, ftpsDestination, sftpDestination, compositeDataExportDestination);
        testInstance.send(files, tagReplacerFactory, logger, thesaurus);
        verify(emailDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(ftpDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(ftpsDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(sftpDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(localFileDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(webServiceDestination, never()).send(anyListOf(ExportData.class), any(TagReplacerFactory.class), any(Logger.class));
    }

    @Test
    public void testSendDataAndFiles() {
        testInstance = new CompositeDataExportDestination(emailDestination,
                ftpDestination, ftpsDestination, sftpDestination, compositeDataExportDestination);
        testInstance.send(data, files, tagReplacerFactory, logger, thesaurus);
        verify(emailDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(ftpDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(ftpsDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(sftpDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(localFileDestination).send(files, tagReplacerFactory, logger, thesaurus);
        verify(webServiceDestination).send(data, tagReplacerFactory, logger);
    }
}
