/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.ftpclient.FtpSessionFactory;
import com.elster.jupiter.ftpclient.IOConsumer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FtpDestinationImplTest {

    public static final String DATA1 = "line 1";
    public static final String DATA2 = "line 2";
    public static final String DATA3 = "line 3";
    public static final String DATA4 = "line 4";
    public static final String RELATIVE_DIR = "datadir";

    private FileSystem fileSystem, ftpFileSystem;
    private Clock clock = Clock.systemDefaultZone();
    private TagReplacerFactory tagReplacerFactory = structureMarker -> TagReplacerImpl.asTagReplacer(clock, structureMarker, 17);
    private Path file1, file2;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataExportService dataExportService;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private FtpClientService ftpClientService;
    @Mock
    private IExportTask exportTask;
    private Logger logger = Logger.getAnonymousLogger();
    private TransactionService transactionService = new TransactionVerifier();

    @Before
    public void setUp() throws IOException {

        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocation -> {
            NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
            when(messageFormat.format(anyVararg())).thenReturn(((MessageSeed) invocation.getArguments()[0]).getDefaultFormat());
            return messageFormat;
        });

        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        file1 = fileSystem.getPath("/a.tmp");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file1, StandardOpenOption.CREATE_NEW)))) {
            writer.write(DATA1);
            writer.write(DATA2);
        }
        file2 = fileSystem.getPath("/b.tmp");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file2, StandardOpenOption.CREATE_NEW)))) {
            writer.write(DATA3);
            writer.write(DATA4);
        }

        ftpFileSystem = Jimfs.newFileSystem(Configuration.unix());

        when(ftpClientService.getFtpFactory("server", 21, "user", "password")).thenReturn(new FtpSessionFactory() {
            @Override
            public void runInSession(IOConsumer ftpSessionBehavior) throws IOException {
                ftpSessionBehavior.accept(ftpFileSystem);
            }
        });
        when(dataVaultService.decrypt(anyString())).thenAnswer(invocation -> ((String) invocation.getArguments()[0]).getBytes());
        when(dataVaultService.encrypt(any())).thenAnswer(invocation -> new String((byte[]) invocation.getArguments()[0]));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testSendMultipleFiles() {
        FtpDestinationImpl ftpDestination = new FtpDestinationImpl(dataModel, clock, thesaurus, dataExportService, fileSystem, dataVaultService, ftpClientService, transactionService);
        ftpDestination.doInitialize(exportTask, "server", 21, "user", "password", RELATIVE_DIR, "DDD<identifier>", "txt");

        ftpDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1), tagReplacerFactory, logger, thesaurus);
        ftpDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root1"), file1), tagReplacerFactory , logger, thesaurus);
        ftpDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root2"), file1), tagReplacerFactory , logger, thesaurus);

        Path file1 = ftpFileSystem.getPath("/", RELATIVE_DIR, "DDDroot.txt");
        assertThat(Files.exists(file1)).isTrue();
        assertThat(getContent(file1)).isEqualTo(DATA1 + DATA2);

        Path file2 = ftpFileSystem.getPath("/", RELATIVE_DIR, "DDDroot.txt");
        assertThat(Files.exists(file2)).isTrue();
        assertThat(getContent(file2)).isEqualTo(DATA1 + DATA2);

        Path file3 = ftpFileSystem.getPath("/", RELATIVE_DIR, "DDDroot.txt");
        assertThat(Files.exists(file3)).isTrue();
        assertThat(getContent(file3)).isEqualTo(DATA1 + DATA2);

    }

    @Test
    public void testSendMultipleFilesInAMap() {
        FtpDestinationImpl ftpDestination = new FtpDestinationImpl(dataModel, clock, thesaurus, dataExportService, fileSystem, dataVaultService, ftpClientService, transactionService);
        ftpDestination.doInitialize(exportTask, "server", 21, "user", "password", RELATIVE_DIR, "DDD<identifier>", "txt");

        ftpDestination.send(
                ImmutableMap.of(
                        DefaultStructureMarker.createRoot(clock, "root"), file1,
                        DefaultStructureMarker.createRoot(clock, "root1"), file2,
                        DefaultStructureMarker.createRoot(clock, "root2"), file1
                ), tagReplacerFactory, logger, thesaurus
        );

        Path file1 = ftpFileSystem.getPath("/", RELATIVE_DIR, "DDDroot.txt");
        assertThat(Files.exists(file1)).isTrue();
        assertThat(getContent(file1)).isEqualTo(DATA1 + DATA2);

        Path file2 = ftpFileSystem.getPath("/", RELATIVE_DIR, "DDDroot1.txt");
        assertThat(Files.exists(file2)).isTrue();
        assertThat(getContent(file2)).isEqualTo(DATA3 + DATA4);

        Path file3 = ftpFileSystem.getPath("/", RELATIVE_DIR, "DDDroot2.txt");
        assertThat(Files.exists(file3)).isTrue();
        assertThat(getContent(file3)).isEqualTo(DATA1 + DATA2);

    }

    @Test
    public void testSend() {
        FtpDestinationImpl ftpDestination = new FtpDestinationImpl(dataModel, clock, thesaurus, dataExportService, fileSystem, dataVaultService, ftpClientService, transactionService);
        ftpDestination.initialize(exportTask, "server", 21, "user", "password", RELATIVE_DIR, "DDD<identifier>", "txt");

        ftpDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1), tagReplacerFactory, logger, thesaurus);

        Path file = ftpFileSystem.getPath("/", RELATIVE_DIR, "DDDroot.txt");
        assertThat(Files.exists(file)).isTrue();
        assertThat(getContent(file)).isEqualTo(DATA1 + DATA2);

    }

    private String getContent(Path file) {
        try {
            StringBuilder content = new StringBuilder();
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}