package com.energyict.mdc.device.alarms;


import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.share.IssueAction;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.impl.actions.MailNotificationAlarmAction;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MailNotificationAlarmActionTest extends BaseTest {
    private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String MAIL_USER_PROPERTY = "mail.user";
    private static final String MAIL_PASSWORD_PROPERTY = "mail.password";
    private static final String MAIL_FROM_PROPERTY = "mail.from";

    IssueAction action;
    BundleContext bundleContext;

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(MailNotificationAlarmAction.class.getName());
        bundleContext = issueService.getBundleContext().get();
    }

    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        when(bundleContext.getProperty(MAIL_SMTP_HOST_PROPERTY)).thenReturn("smtp.honeywell.com");
        when(bundleContext.getProperty(MAIL_SMTP_PORT_PROPERTY)).thenReturn("25");
        when(bundleContext.getProperty(MAIL_USER_PROPERTY)).thenReturn("venkatakrishna.alisetty@honeywell.com");
        when(bundleContext.getProperty(MAIL_PASSWORD_PROPERTY)).thenReturn("Honeywell@123");
        when(bundleContext.getProperty(MAIL_FROM_PROPERTY)).thenReturn("venkatakrishna.alisetty@honeywell.com");

        Map<String, Object> properties = new HashMap<>();
        properties.put(MailNotificationAlarmAction.TO, new MailNotificationAlarmAction.MailTo("venkatakrishnaalisetty@honeywell.com"));
        IssueAction actionResult = action.initAndValidate(properties);

        DeviceAlarm alarm = createAlarmMinInfo();
        assertThat(alarm.getAssignee().getUser()).isNull();
        assertThat(alarm.getAssignee().getWorkGroup()).isNull();
        assertThat(actionResult.getDisplayName()).isEqualTo("Email");


    }

}