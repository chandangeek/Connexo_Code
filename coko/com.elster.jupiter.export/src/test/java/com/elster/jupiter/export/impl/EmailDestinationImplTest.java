package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.mail.OutboundMailMessage;
import com.elster.jupiter.mail.impl.MailAddressImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailDestinationImplTest {

    public static final String DATA1 = "blablablablabla1";
    public static final String DATA2 = "blablablablabla2";
    public static final String DATA3 = "blablablablabla3";
    public static final String DATA4 = "blablablablabla4";
    public static final String SUBJECT = "subject";

    private Clock clock = Clock.systemDefaultZone();

    private FileSystem fileSystem;
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
    private AtomicReference<MailMessageBuilder> builder = new AtomicReference<>();

    @Before
    public void setUp() throws IOException {
        when(dataModel.getInstance(EmailDestinationImpl.class)).thenAnswer(invocation -> new EmailDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, mailService));

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
        EmailDestinationImpl emailDestination = new EmailDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, mailService);
        emailDestination.init(null, "target@mailinator.com", SUBJECT, "file", "txt");

        emailDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1));

        verify(mailService).messageBuilder(MailAddressImpl.of("target@mailinator.com"));
        verify(builder.get()).withSubject(SUBJECT);
        verify(builder.get()).withAttachment(any(), eq("file.txt"));
        verify(mailMessage).send();
    }

    @Test
    public void testSendMultiple() throws Exception {
        EmailDestinationImpl emailDestination = new EmailDestinationImpl(dataModel, clock, thesaurus, dataExportService, appService, fileSystem, mailService);
        emailDestination.init(null, "target@mailinator.com", SUBJECT, "file<identifier>", "txt");

        emailDestination.send(ImmutableMap.of(DefaultStructureMarker.createRoot(clock, "root"), file1, DefaultStructureMarker.createRoot(clock, "root2"), file2));

        verify(mailService).messageBuilder(MailAddressImpl.of("target@mailinator.com"));
        verify(builder.get()).withSubject(SUBJECT);
        verify(builder.get()).withAttachment(any(), eq("fileroot.txt"));
        verify(builder.get()).withAttachment(any(), eq("fileroot2.txt"));
        verify(mailMessage).send();
    }
}