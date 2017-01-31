/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.mail.OutboundMailMessage;
import com.elster.jupiter.mail.impl.MailAddressImpl;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyVararg;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailDestinationImplTest {

    private static final String DATA1 = "line 1";
    private static final String DATA2 = "line 2";
    private static final String DATA3 = "line 3";
    private static final String DATA4 = "line 4";
    private static final String SUBJECT = "subject";

    private Clock clock = Clock.systemDefaultZone();

    private FileSystem fileSystem;
    private TagReplacerFactory tagReplacerFactory = new TagReplacerFactory() {
        @Override
        public TagReplacer forMarker(StructureMarker structureMarker) {
            return TagReplacerImpl.asTagReplacer(clock, structureMarker, 17);
        }
    };
    private Path file1, file2;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataExportService dataExportService;
    @Mock
    private AppService appService;
    @Mock
    private MailService mailService;
    @Mock
    private OutboundMailMessage mailMessage;
    private Logger logger = Logger.getAnonymousLogger();
    private AtomicReference<MailMessageBuilder> builder = new AtomicReference<>();
    private TransactionService transactionService = new TransactionVerifier();

    @Before
    public void setUp() throws IOException {

        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocation -> {
            NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
            when(messageFormat.format(anyVararg())).thenReturn(((MessageSeed) invocation.getArguments()[0]).getDefaultFormat());
            return messageFormat;
        });

        when(dataModel.getInstance(EmailDestinationImpl.class)).thenAnswer(invocation -> new EmailDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, mailService, transactionService));

        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        file1 = fileSystem.getPath("C:/a.tmp");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file1, StandardOpenOption.CREATE_NEW)))) {
            writer.write(DATA1);
            writer.write(DATA2);
        }
        file2 = fileSystem.getPath("C:/b.tmp");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file2, StandardOpenOption.CREATE_NEW)))) {
            writer.write(DATA3);
            writer.write(DATA4);
        }
//        builder = FakeBuilder.initBuilderStub(mailMessage, MailMessageBuilder.class);
        builder.set(mock(MailMessageBuilder.class, builderAnswer()));
        when(mailService.messageBuilder(any())).thenReturn(builder.get());
        when(mailService.mailAddress(any())).thenAnswer(invocation -> MailAddressImpl.of(invocation.getArguments()[0].toString()));
    }

    Answer<Object> builderAnswer() {
       return invocation -> {
           if (invocation.getMethod().getReturnType().isAssignableFrom(MailMessageBuilder.class)) {
               return builder.get();
           }
           return mailMessage;
       };
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSend() throws Exception {
        EmailDestinationImpl emailDestination = new EmailDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, mailService, transactionService);
        emailDestination.init(null, "target@mailinator.com", SUBJECT, "file", "txt");

        emailDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1), tagReplacerFactory, logger, thesaurus);

        verify(mailService).messageBuilder(MailAddressImpl.of("target@mailinator.com"));
        verify(builder.get()).withSubject(SUBJECT);
        verify(builder.get()).withAttachment(any(), eq("file.txt"));
        verify(mailMessage).send();
    }

    @Test
    public void testSendToMultipleAddresses() throws Exception {
        EmailDestinationImpl emailDestination = new EmailDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, mailService, transactionService);
        emailDestination.init(null, "target1@mailinator.com;target2@mailinator.com", SUBJECT, "file", "txt");

        emailDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1), tagReplacerFactory, logger, thesaurus);

        verify(mailService).messageBuilder(MailAddressImpl.of("target1@mailinator.com"));
        verify(builder.get()).addRecipient(MailAddressImpl.of("target2@mailinator.com"));
        verify(builder.get()).withSubject(SUBJECT);
        verify(builder.get()).withAttachment(any(), eq("file.txt"));
        verify(mailMessage).send();
    }

    @Test
    public void testSendMultipleAttachments() throws Exception {
        EmailDestinationImpl emailDestination = new EmailDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, mailService, transactionService);
        emailDestination.init(null, "target@mailinator.com", SUBJECT, "file<identifier>", "txt");

        emailDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1, DefaultStructureMarker.createRoot(clock, "root2"), file2), tagReplacerFactory, logger, thesaurus);

        verify(mailService).messageBuilder(MailAddressImpl.of("target@mailinator.com"));
        verify(builder.get()).withSubject(SUBJECT);
        verify(builder.get()).withAttachment(any(), eq("fileroot.txt"));
        verify(builder.get()).withAttachment(any(), eq("fileroot2.txt"));
        verify(mailMessage).send();
    }
}