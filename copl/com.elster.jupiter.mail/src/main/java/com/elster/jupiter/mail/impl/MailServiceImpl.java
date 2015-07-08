package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import com.elster.jupiter.util.Checks;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.mail", service = {MailService.class}, property = "name=" + MailService.COMPONENT_NAME, immediate = true)
public class MailServiceImpl implements IMailService {

    private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final Logger LOGGER = Logger.getLogger(MailServiceImpl.class.getName());

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
        return MailAddressImpl.of(mailAddress);
    }

    @Activate
    public final void activate(BundleContext bundleContext) {
        smtpHost = bundleContext.getProperty(MAIL_SMTP_HOST_PROPERTY);
        smtpPort = bundleContext.getProperty(MAIL_SMTP_PORT_PROPERTY);

        String fromAddress = bundleContext.getProperty("mail.from");
        try {
            from = Checks.is(fromAddress).emptyOrOnlyWhiteSpace() ? null : new InternetAddress(fromAddress);
        } catch (AddressException e) {
            LOGGER.log(Level.SEVERE, e.getMessage() == null ? e.toString() : e.getMessage(), e);
        }
        user = bundleContext.getProperty("mail.user");
        password = bundleContext.getProperty("mail.password");
    }

    @Deactivate
    public final void deactivate() {
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
        Properties properties = new Properties();
        properties.setProperty(MAIL_SMTP_HOST_PROPERTY, getSmtpHost());
        properties.setProperty(MAIL_SMTP_PORT_PROPERTY, getSmtpPort());
        properties.setProperty("mail.smtp.user", user);
        properties.setProperty("mail.smtp.password", password);
        final Session session = Session.getDefaultInstance(properties);
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
}
