/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.InvalidAddressException;
import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.mail", service = {MailService.class, MessageSeedProvider.class}, property = "name=" + MailService.COMPONENT_NAME, immediate = true)
public class MailServiceImpl implements IMailService, MessageSeedProvider {

    private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String MAIL_USER_PROPERTY = "mail.user";
    private static final String MAIL_PASSWORD_PROPERTY = "mail.password";
    private static final String MAIL_FROM_PROPERTY = "mail.from";

    private static final Logger LOGGER = Logger.getLogger(MailServiceImpl.class.getName());

    private volatile Thesaurus thesaurus;

    private String smtpHost;
    private int port = 25;
    private Address from;
    private String user;
    private String password;
    private String smtpPort;

    @Inject
    public MailServiceImpl() {
    }

    @Override
    public MailMessageBuilder messageBuilder(MailAddress first, MailAddress... other) {
        List<MailAddress> recipients = Stream.of(Stream.of(first), Arrays.stream(other))
                .flatMap(Function.<Stream<MailAddress>>identity())
                .collect(Collectors.toList());
        return new MailMessageBuilderImpl(this, recipients);
    }

    @Override
    public MailAddress mailAddress(String mailAddress) {
        try {
            return MailAddressImpl.of(mailAddress);
        } catch (AddressException e) {
            throw new InvalidAddressException(this.thesaurus, MessageSeeds.INVALID_ADDRESS, e);
        }
    }

    @Activate
    public final void activate(BundleContext bundleContext) {

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);

        smtpHost = bundleContext.getProperty(MAIL_SMTP_HOST_PROPERTY);
        smtpPort = bundleContext.getProperty(MAIL_SMTP_PORT_PROPERTY);

        String fromAddress = bundleContext.getProperty(MAIL_FROM_PROPERTY);
        try {
            from = Checks.is(fromAddress).emptyOrOnlyWhiteSpace() ? null : new InternetAddress(fromAddress);
        } catch (AddressException e) {
            LOGGER.log(Level.SEVERE, e.getMessage() == null ? e.toString() : e.getMessage(), e);
        }
        user = bundleContext.getProperty(MAIL_USER_PROPERTY);
        password = bundleContext.getProperty(MAIL_PASSWORD_PROPERTY);
    }

    private String getSmtpHost() {
        return smtpHost;
    }

    @Override
    public Address getFrom() {
        return from;
    }

    @Override
    public MailSession getSession() {
        validateMailProperties();
        Properties properties = new Properties();
        properties.setProperty(MAIL_SMTP_HOST_PROPERTY, getSmtpHost());
        properties.setProperty(MAIL_SMTP_PORT_PROPERTY, getSmtpPort());
        properties.setProperty("mail.smtp.user", user);
        properties.setProperty("mail.smtp.password", password);
        final Session session = Session.getInstance(properties);
        return new MailSession() {
            @Override
            public MimeMessage createMessage() {
                return new MimeMessage(session);
            }

            @Override
            public void send(MimeMessage message) {
                Transport transport = null;
                MessagingException rootException = null;
                try {
                    transport = session.getTransport("smtp");
                    transport.connect(smtpHost, user, password);
                    transport.sendMessage(message, message.getAllRecipients());
                } catch (MessagingException e) {
                    rootException = e;
                    // TODO
                    throw new RuntimeException(e);
                } finally {
                    if (transport != null) {
                        try {
                            transport.close();
                        } catch (MessagingException e) {
                            if (rootException != null) {
                                rootException.addSuppressed(e);
                            } else {
                                // TODO
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        };
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    private void validateMailProperties(){
        List<String> badProperties = new ArrayList<>();
        validateMailProperty(MAIL_SMTP_HOST_PROPERTY, getSmtpHost(), badProperties);
        validateMailProperty(MAIL_SMTP_PORT_PROPERTY, getSmtpPort(), badProperties);
        validateMailProperty(MAIL_USER_PROPERTY, this.user, badProperties);
        validateMailProperty(MAIL_PASSWORD_PROPERTY, this.password, badProperties);
        if (!badProperties.isEmpty()){
            throw new IncompleteMailConfigException(this.thesaurus, badProperties.toArray(new String[badProperties.size()]));
        }
    }

    private void validateMailProperty(String propertyName, String value, List<String> badPropertiesCollector){
        if (value == null){
            badPropertiesCollector.add(propertyName);
        }
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setNlsService(NlsService nlsService){
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, getLayer());
    }
}
