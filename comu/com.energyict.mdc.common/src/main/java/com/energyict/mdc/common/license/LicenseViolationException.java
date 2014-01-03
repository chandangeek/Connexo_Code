package com.energyict.mdc.common.license;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.license.LicenseViolation;

import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 * User: igh
 * Date: 4-mrt-2011
 * Time: 9:30:41
 * To change this template use File | Settings | File Templates.
 */
public class LicenseViolationException extends RuntimeException {

    private String messageId;
    private Object[] arguments;
    private LicenseViolation violation;


    public LicenseViolationException(String description) {
        super(description);
    }

    public LicenseViolationException(String messageId, String defaultPattern, Object[] arguments) {
        super(MessageFormat.format(defaultPattern.replaceAll("'", "''"), arguments));
        this.messageId = messageId;
        this.arguments = arguments;
    }

    public LicenseViolationException(LicenseViolation violation) {
        super(getMessage(violation));
    }

    /**
     * Returns a localized message
     *
     * @return a localized String
     */
    public String getLocalizedMessage() {
        if (messageId == null) {
            return super.getLocalizedMessage();
        } else {
            return MessageFormat.format(getPattern(messageId).replaceAll("'", "''"), arguments);
        }
    }

    public String toString() {
        return getLocalizedMessage();
    }

    private String getPattern(String messageId) {
        return Environment.DEFAULT.get().getErrorMsg(messageId);
    }

    private static String getMessage(LicenseViolation violation) {
        StringBuilder builder = new StringBuilder();
        if (violation.isFormatViolated()) {
            builder.append(Environment.DEFAULT.get().getErrorMsg("incorrectLicenseFormat"));
        }
        if (violation.isNumberOfOperationalUsersViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedNumberOfOperationalUsersExceeded"));
        }
        if (violation.isNumberOfCustomerEngagementUsersViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedNumberOfCustomerEngagementUsersExceeded"));
        }
        if (violation.isNumberOfMobileUsersViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedNumberOfMobileUsersExceeded"));
        }
        if (violation.isNumberOfChannelsViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedNumberOfChannelsExceeded"));
        }
        if (violation.isNumberOfDevicesViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedNumberOfDevicesExceeded"));
        }
        if (violation.isNumberOfRegistersViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedNumberOfRegistersExceeded"));
        }
        if (violation.isChannelValidationRulesViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedChannelValidationRulesViolated"));
        }
        if (violation.isRegisterValidationRulesViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedRegisterValidationRulesViolated"));
        }
        if (violation.isChannelEstimationRulesViolated()) {
            if (builder.length() > 0) {
                builder.append(System.getProperty("line.separator"));
            }
            builder.append(Environment.DEFAULT.get().getErrorMsg("licensedChannelEstimationRulesViolated"));
        }
        return builder.toString();
    }

}