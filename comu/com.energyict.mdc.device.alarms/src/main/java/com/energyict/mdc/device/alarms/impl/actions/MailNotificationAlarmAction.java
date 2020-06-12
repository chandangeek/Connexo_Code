package com.energyict.mdc.device.alarms.impl.actions;

import com.elster.jupiter.bootstrap.PasswordDecryptService;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.MailPropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmServiceImpl;
import com.energyict.mdc.device.alarms.impl.event.IncompleteEmailConfigurationException;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleContext;


public class MailNotificationAlarmAction extends AbstractIssueAction {


    private static final String NAME = "MailNotificationAlarmAction";
    public static final String TO = NAME + ".to";

    private static final Logger LOGGER = Logger.getLogger(MailNotificationAlarmAction.class.getName());

    private Issue issue;
    private DeviceAlarmService deviceAlarmService;
    private PasswordDecryptService passwordDecryptService;

    private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String MAIL_USER_PROPERTY = "mail.user";
    private static final String MAIL_PASSWORD_PROPERTY = "mail.password";
    private static final String MAIL_FROM_PROPERTY = "mail.from";

    private String smtpHost;
    private String smtpPort;
    private String user;
    private String password;
    private String fromAddress;
    @Inject
    public MailNotificationAlarmAction(DataModel dataModel, Thesaurus thesaurus, com.energyict.mdc.dynamic.PropertySpecService propertySpecService,
                                       DeviceAlarmService deviceAlarmService, PasswordDecryptService passwordDecryptService)
    {
        super(dataModel, thesaurus, propertySpecService);
        this.deviceAlarmService =deviceAlarmService ;
        this.passwordDecryptService = passwordDecryptService;
    }


    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_NOTIFY).format();
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        this.sendMail(issue);
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        result.success(getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_NOTIFY).format());
        return result;
    }
    @Override
    public MailNotificationAlarmAction setIssue(Issue issue) {
        this.issue = issue;
        return this;
    }
    public void sendMail(Issue issue) {
        Transport transport = null;
        String recipients = getRecipientFromParameters(properties);

        Properties properties = getMailProperties();
        Session mailConnection = Session.getInstance(properties, null);
        Message msg = new MimeMessage(mailConnection);

        Address address = null;
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
            address = new InternetAddress(fromAddress);
            msg.setContent(mailContent, "text/plain");
            msg.setFrom(address);
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
                throw new IncompleteEmailConfigurationException(getThesaurus(), e.getLocalizedMessage());
            } finally {
                if (transport != null) {
                    try {
                        transport.close();
                    } catch (MessagingException e) {
                        if (rootException != null) {
                            rootException.addSuppressed(e);
                        } else {
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
        BundleContext bundleContext = ((DeviceAlarmServiceImpl)deviceAlarmService).getBundleContext().get();
        user = bundleContext.getProperty(MAIL_USER_PROPERTY);
        String encryptedPassword = bundleContext.getProperty(MAIL_PASSWORD_PROPERTY);
        password = passwordDecryptService.getDecryptPassword(encryptedPassword, bundleContext.getProperty("com.elster.jupiter.datasource.keyfile"));
        fromAddress = bundleContext.getProperty(MAIL_FROM_PROPERTY);
        smtpHost = bundleContext.getProperty(MAIL_SMTP_HOST_PROPERTY);
        smtpPort = bundleContext.getProperty(MAIL_SMTP_PORT_PROPERTY);
        validateMailProperties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.user", user);
        props.setProperty("mail.smtp.password", password);
        props.setProperty("mail.smtp.from", fromAddress);
        return props;
    }

    private void validateMailProperty(String propertyName, String value, List<String> badPropertiesCollector){
        if (value == null || value.isEmpty()){
            badPropertiesCollector.add(propertyName);
        }
    }

    private void validateMailProperties(){
        List<String> badProperties = new ArrayList<>();
        validateMailProperty(MAIL_SMTP_HOST_PROPERTY, this.smtpHost, badProperties);
        validateMailProperty(MAIL_SMTP_PORT_PROPERTY, this.smtpPort, badProperties);
        validateMailProperty(MAIL_FROM_PROPERTY, this.fromAddress, badProperties);
        validateMailProperty(MAIL_USER_PROPERTY, this.user, badProperties);
        /* password is not mandatory
        validateMailProperty(MAIL_PASSWORD_PROPERTY, this.password, badProperties);
        */
        if (!badProperties.isEmpty()){
            throw new IncompleteEmailConfigurationException(this.getThesaurus(), badProperties.toArray(new String[badProperties.size()]));
        }
    }

    @SuppressWarnings("unchecked")
    private String getRecipientFromParameters(Map<String, Object> properties) {
        Object value = properties.get(TO);
        if (value != null) {
            String receiver = getPropertySpec(TO).get().getValueFactory().toStringValue(value);
            try {
                JSONObject jsonData = new JSONObject(receiver);
                return jsonData.get("recipient").toString();
            } catch (JSONException e) {
                return "";
            }
        }
        return "";
    }

    private String getContent(Issue issue) {
        int totalPriority= issue.getPriority().getImpact() + issue.getPriority().getUrgency();
        DateTimeFormatter formatter  =  DateTimeFormatter.ofPattern("EEE dd MMM''''YY 'at' HH:mm:ss zzz");
        long unixTime = issue.getCreateDateTime().getEpochSecond();
        String current = Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.systemDefault())
                .format(formatter);
        Optional<String> user=Optional.of("Unassigned");
        if(issue.getAssignee().getUser() != null)
            user = Optional.of(issue.getAssignee().getUser().getName());
        return "Alarm Id : " +issue.getIssueId() + "\n" +
                "Alarm reason : " + issue.getReason().getName() + "\n" +
                "Alarm type : " + issue.getReason().getIssueType().getName()+ "\n" +
                "Device : " +  issue.getDevice().getName()+ "\n" +
                "User : " + user.get()+ "\n" +
                "Priority : " + totalPriority + "\n" +
                "Creation Date : " + current;
    }
    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                getPropertySpecService()
                        .specForValuesOf(new MailValueFactory())
                        .named(TO, TranslationKeys.ACTION_MAIL_TO)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(getDefaultValues(issue))
                        .finish());
    }



    private MailTo getDefaultValues(Issue issue) {
        if(issue!=null)
            return new MailTo("");
        return new MailTo("");

    }
    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(TO);
        String data = "";
        if (value != null) {
            data = ((MailTo) value).recipient.get();
        }
        return data;
    }
    public static class MailTo extends HasIdAndName {
        private Optional<String> recipient;


        public MailTo(String recipient) {

            this.recipient = recipient != null ? Optional.of(recipient) : Optional.empty();
        }

        @Override
        public Object getId() {
            return recipient.get();
        }

        @Override
        public String getName() {
            try {
                JSONObject jsonId = new JSONObject();
                jsonId.put("recipient", recipient.orElse(""));
                return jsonId.toString();
            } catch (JSONException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            return "";
        }
    }


    private class MailValueFactory implements ValueFactory<MailTo>, MailPropertyFactory {

        @Override
        public MailTo fromStringValue(String stringValue) {
            //framework is not returning JSON string when recipient value is entered for the first time
            //Hence, this specific check for JSON string
            if (stringValue.substring(0, 1).compareTo("{") == 0) {
                try {
                    JSONObject jsonData = new JSONObject(stringValue);
                    String recipient = jsonData.get("recipient").toString();
                    return new MailTo(recipient);
                }
                catch (JSONException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }}
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
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }
}
