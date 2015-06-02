package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MailServiceImplTest {

    public static final String BODY = "Sending you this file...";
    public static final String REPLY_TO = "noreply@elster.com";
    public static final String FROM = "info@elster.com";
    public static final String SUBJECT = "First test";
    @Mock
    private BundleContext bundleContext;

    @Before
    public void setUp() {
        when(bundleContext.getProperty("mail.smtp.host")).thenReturn("localhost");
        when(bundleContext.getProperty("mail.smtp.port")).thenReturn("2525");
        when(bundleContext.getProperty("mail.user")).thenReturn("");
        when(bundleContext.getProperty("mail.password")).thenReturn("");
        when(bundleContext.getProperty("mail.from")).thenReturn(FROM);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testMailWithWiser() throws MessagingException, IOException, URISyntaxException {
        Wiser wiser = new Wiser(2525);
        wiser.setHostname("localhost");
        wiser.start();

        MailServiceImpl mailService = new MailServiceImpl();

        mailService.activate(bundleContext);

        MailAddress mailAddress = mailService.mailAddress("MailServiceImplTest@mailinator.com");

        mailService.messageBuilder(mailAddress)
                .withSubject(SUBJECT)
                .withAttachment(Paths.get(getClass().getClassLoader().getResource("com/elster/jupiter/mail/impl/testAttachment.txt").toURI()))
                .withBody(BODY)
                .withReplyTo(mailService.mailAddress(REPLY_TO))
                .build()
                .send();

        wiser.stop();

        assertThat(wiser.getMessages()).hasSize(1);

        WiserMessage message = wiser.getMessages().get(0);
        MimeMessage mimeMessage = message.getMimeMessage();
        assertThat(mimeMessage.getReplyTo()).hasSize(1);
        assertThat(mimeMessage.getReplyTo()[0].toString()).isEqualTo(REPLY_TO);
        assertThat(mimeMessage.getFrom()).hasSize(1);
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo(FROM);
        assertThat(mimeMessage.getSubject()).isEqualTo(SUBJECT);
        Object content = mimeMessage.getContent();
        assertThat(content).isInstanceOf(MimeMultipart.class);
        MimeMultipart body = (MimeMultipart) content;
        assertThat(body.getBodyPart(0).getContent()).isEqualTo(BODY);
        assertThat(body.getBodyPart(1).getFileName()).isEqualTo("testAttachment.txt");
        assertThat(body.getBodyPart(1).getContent()).isEqualToComparingFieldByField("Test"); // file content
    }

}