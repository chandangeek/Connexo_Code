/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
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
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MailServiceImplTest {

    public static final String BODY = "Sending you this file...";
    public static final String REPLY_TO = "noreply@elster.com";
    public static final String FROM = "info@elster.com";
    public static final String SUBJECT = "First test";
    @Mock
    private BundleContext bundleContext;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(bundleContext.getProperty("mail.smtp.host")).thenReturn("localhost");
        when(bundleContext.getProperty("mail.smtp.port")).thenReturn("2525");
        when(bundleContext.getProperty("mail.user")).thenReturn("");
        when(bundleContext.getProperty("mail.password")).thenReturn("");
        when(bundleContext.getProperty("mail.from")).thenReturn(FROM);

        when(nlsService.getThesaurus(anyString(), anyObject())).thenReturn(thesaurus);
        when(thesaurus.getString(anyString(), anyString()))
                .thenAnswer(invocation->this.getTranslationByKey((String)invocation.getArguments()[0], (String)invocation.getArguments()[1]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));
    }

    private String getTranslationByKey(String translationKey, String defaultMessage) {
        return Arrays.stream(MessageSeeds.values())
                .filter(messageSeed -> messageSeed.getKey().equals(translationKey))
                .map(MessageSeed::getDefaultFormat)
                .findFirst()
                .orElse(defaultMessage);
    }

    @Test
    public void testMailWithWiser() throws MessagingException, IOException, URISyntaxException {
        Wiser wiser = new Wiser(2525);
        wiser.setHostname("localhost");
        wiser.start();

        MailServiceImpl mailService = getMailService();

        MailAddress mailAddress = mailService.mailAddress("MailServiceImplTest@mailinator.com");

        mailService.messageBuilder(mailAddress)
                .withSubject(SUBJECT)
                .withAttachment(Paths.get(getClass().getClassLoader().getResource("com/elster/jupiter/mail/impl/testAttachment.txt").toURI()), "testAttachment.txt")
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

    private MailServiceImpl getMailService() {
        MailServiceImpl mailService = new MailServiceImpl();
        mailService.setNlsService(nlsService);
        mailService.activate(bundleContext);
        return mailService;
    }
}