package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailAddress;
import com.elster.jupiter.mail.MailException;
import com.elster.jupiter.mail.MailMessageBuilder;
import com.elster.jupiter.mail.MailService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

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

@Component(name = "com.elster.jupiter.export", service = {MailService.class}, property = "name=" + MailService.COMPONENT_NAME, immediate = true)
public class MailServiceImpl implements IMailService {

    private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final Logger LOGGER = Logger.getLogger(MailServiceImpl.class.getName());

    private String smtpHost;
    private Address from;
    private String user;
    private String password;

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
        try {
            smtpHost = bundleContext.getProperty(MAIL_SMTP_HOST_PROPERTY);
            from = new InternetAddress(bundleContext.getProperty("mail.from"));
            user = bundleContext.getProperty("mail.user");
            password = bundleContext.getProperty("mail.password");
        } catch (AddressException e) {
            LOGGER.log(Level.SEVERE, e.getMessage() == null ? e.toString() : e.getMessage(), e);
        }
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
    public MimeMessage createMessage() {
        Properties properties = new Properties();
        properties.setProperty(MAIL_SMTP_HOST_PROPERTY, getSmtpHost());
        Session session = Session.getDefaultInstance(properties);
        return new MimeMessage(session);
    }

    @Override
    public void send(MimeMessage message) {
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            // TODO
            throw new MailException(null, null, e);
        }
    }


}
