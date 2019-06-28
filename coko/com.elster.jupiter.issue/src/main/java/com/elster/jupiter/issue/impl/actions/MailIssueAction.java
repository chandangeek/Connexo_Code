package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.MailPropertyFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.sql.SqlBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class MailIssueAction extends AbstractIssueAction {
    private static final String NAME = "MailIssueAction";
    public static final String TO = NAME + ".to";

    private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String MAIL_USER_PROPERTY = "mail.user";
    private static final String MAIL_PASSWORD_PROPERTY = "mail.password";
    private static final String MAIL_FROM_PROPERTY = "mail.from";


    private Issue issue;
    private IssueService issueService;

    private String smtpHost;
    private int port = 25;
    private Address from;
    private String user;
    private String password;
    private String smtpPort;
    private String fromAddress;


    @Inject
    protected MailIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
    }

    @Override
    public MailIssueAction setIssue(Issue issue) {
        this.issue = issue;
        return this;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_ISSUE).format();
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        sendMail(issue);
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        result.success(getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_ISSUE).format());
        return result;
    }

    public void sendMail(Issue issue) {

        Transport transport = null;
        String recipients = getAssigneeFromParameters(properties);

        Properties properties = getMailProperties();
        Session mailConnection = Session.getInstance(properties, null);
        Message msg = new MimeMessage(mailConnection);

        Address a = null;
        MessagingException rootException = null;
        String[] recipientList = recipients.split(";");
        InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
        int counter = 0;
        String mailContent = getContent(issue);

        try {
            for (String recipient : recipientList) {
                recipientAddress[counter] = new InternetAddress(recipient.trim());
                counter++;
            }

            a = new InternetAddress(fromAddress);
            msg.setContent(mailContent, "text/plain");
            msg.setFrom(a);
            msg.setRecipients(Message.RecipientType.TO, recipientAddress);
            msg.setSubject(issue.getIssueId() + "-" + issue.getTitle());
            try {
                if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
                    transport = mailConnection.getTransport("smtp");
                    transport.connect(smtpHost, user, password);
                    transport.sendMessage(msg, msg.getAllRecipients());
                } else
                    Transport.send(msg, msg.getAllRecipients());
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
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    public Properties getMailProperties() {
        Properties props = new Properties();
        BundleContext bundleContext = issueService.getBundleContext().get();
        user = bundleContext.getProperty(MAIL_SMTP_HOST_PROPERTY);
        password = bundleContext.getProperty(MAIL_PASSWORD_PROPERTY);
        fromAddress = bundleContext.getProperty(MAIL_FROM_PROPERTY);
        smtpHost = bundleContext.getProperty(MAIL_SMTP_HOST_PROPERTY);
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", bundleContext.getProperty(MAIL_SMTP_PORT_PROPERTY));
        props.setProperty("mail.smtp.user", user);
        props.setProperty("mail.smtp.password", password);
        return props;
    }

    private String getContent(Issue issue) {
        Optional<String> user = Optional.ofNullable(Optional.ofNullable(issue.getAssignee().getUser().getName()).orElse("Undefined"));
        return "ID:" + issue.getIssueId() + "\n" +
                "Issue reason: " + issue.getReason().getName() + "\n" +
                "Issue type: " + issue.getReason().getIssueType().getName() + "\n" +
                "User: " + user.get() + "\n" +
                "Creation Time: " + issue.getCreateDateTime();
    }

    @SuppressWarnings("unchecked")
    private String getAssigneeFromParameters(Map<String, Object> properties) {
        Object value = properties.get(TO);
        if (value != null) {
            String assignee = getPropertySpec(TO).get().getValueFactory().toStringValue(value);
            try {
                JSONObject jsonData = new JSONObject(assignee);
                return jsonData.get("recipients").toString();
            } catch (JSONException e) {
                return "";
            }
        }
        return "";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = Arrays.asList(
                getPropertySpecService()
                        .specForValuesOf(new MailToValueFactory())
                        .named(TO, TranslationKeys.ACTION_MAIL_TO)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(getDefaultValues(issue))
                        .finish());
        return propertySpecs;
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(TO);
        String data = "";
        if (value != null) {
            try {
                String recipients = ((MailTo) value).mailTo.get();
                data = String.format("%s", recipients);
            } catch (Exception e) {
                data = "";
            }
        }
        return data;
    }


    static class MailTo extends HasIdAndName {
        private Optional<String> mailTo;

        MailTo(String mailTo) {
            this.mailTo = mailTo != null ? Optional.of(mailTo) : Optional.of("");
        }

        @Override
        public String getId() {
            return mailTo.get();
        }

        @Override
        public String getName() {
            try {
                JSONObject jsonName = new JSONObject();
                jsonName.put("recipients", mailTo.get());
                return jsonName.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }

    }

    private MailTo getDefaultValues(Issue issue) {
        if (issue != null)
            return new MailTo("");
        return new MailTo("");
    }

    private class MailToValueFactory implements ValueFactory<MailTo>, MailPropertyFactory {
        @Override
        public MailTo fromStringValue(String stringValue) {
            if (stringValue.substring(0, 1).compareTo("{") == 0) {
                try {
                    JSONObject jsonData = new JSONObject(stringValue);
                    String value = jsonData.get("recipients").toString();
                    return new MailTo(value);
                } catch (JSONException e) {
                    return null;
                }
            }
            return new MailTo(stringValue);

        }

        @Override
        public String toStringValue(MailTo object) {
            return object.getName();
        }

        @Override
        public Class<MailTo> getValueType() {
            return MailTo.class;
        }

        @Override
        public MailTo valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);

        }

        @Override
        public Object valueToDatabase(MailTo object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, MailTo value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, MailTo value) {
            if (value != null) {
                builder.addObject(this.valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }

        }
    }
}