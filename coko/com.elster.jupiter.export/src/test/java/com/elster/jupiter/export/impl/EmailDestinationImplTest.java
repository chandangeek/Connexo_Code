package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.mail.OutboundMailMessage;
import com.elster.jupiter.mail.impl.MailAddressImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailDestinationImplTest {

    public static final String DATA1 = "blablablablabla1";
    public static final String DATA2 = "blablablablabla2";
    public static final String SUBJECT = "subject";

    private FileSystem fileSystem;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataExportService dataExportService;
    @Mock
    private AppService appService;
    @Mock
    private FormattedExportData data1, data2;
    @Mock
    private MailService mailService;
    @Mock
    private OutboundMailMessage mailMessage;
    private AtomicReference<MailMessageBuilder> builder = new AtomicReference<>();

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        when(dataModel.getInstance(EmailDestinationImpl.class)).thenAnswer(invocation -> new EmailDestinationImpl(dataModel, thesaurus, dataExportService, appService, fileSystem, mailService));
        when(data1.getAppendablePayload()).thenReturn(DATA1);
        when(data2.getAppendablePayload()).thenReturn(DATA2);
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
        EmailDestinationImpl emailDestination = new EmailDestinationImpl(dataModel, thesaurus, dataExportService, appService, fileSystem, mailService);
        emailDestination.init(null, "target@mailinator.com", SUBJECT, "file", "txt");

        emailDestination.send(Arrays.asList(data1, data2));

        verify(mailService).messageBuilder(MailAddressImpl.of("target@mailinator.com"));
        verify(builder.get()).withSubject(SUBJECT);
        verify(builder.get()).withAttachment(any(), eq("file.txt"));
        verify(mailMessage).send();
    }
}