package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface containing all required property getters for Proximus SMS functionality
 *
 * @author sva
 * @since 23/05/13 - 9:00
 */
public abstract class SmsWakeUpDlmsProtocolProperties extends DlmsProtocolProperties {

    public static final String SMS_BASE_URL_PROP = "SmsConnectionURL";
    public static final String SMS_SERVICE_CODE_PROP = "SmsServiceCode";
    public static final String SMS_SOURCE_PROP = "SmsSource";
    public static final String SMS_AUTH_PROP = "SmsAuthentication";
    public static final String SMS_PHONE_NUMBER_PROP = "SmsPhoneNumber";

    public static final String WAKEUP_POLLING_TIMEOUT = "PollTimeOut";
    public static final String WAKEUP_POLLING_FREQUENCY = "PollFrequency";
    public static final String DEFAULT_WAKEUP_POLLING_TIMEOUT = "300";
    public static final String DEFAULT_WAKEUP_POLLING_FREQUENCY = "20:5";
    public static final int DEFAULT_FIRST_POLL_DELAY = 20;
    public static final int DEFAULT_SECOND_POLL_DELAY = 5;

    private long pollTimeOut;
    private int firstPollDelay;
    private int secondPollDelay;

    /**
     * Getter for the base URL of the Proximus VAMP SMS API
     */
    public String getSmsConnectionUrl() {
        return getStringValue(SMS_BASE_URL_PROP, "");
    }

    /**
     * Getter for the unique source of the SMS (comparable to a login name)
     */
    public String getSmsSource() {
        return getStringValue(SMS_SOURCE_PROP, "");
    }

    /**
     * Getter for the authentication security credentials (comparable to password)
     */
    public String getSmsAuthentication() {
        return getStringValue(SMS_AUTH_PROP, "");
    }

    /**
     * Getter for the service code, which identifies the tariff for SMS.
     */
    public String getSmsServiceCode() {
        return getStringValue(SMS_SERVICE_CODE_PROP, "");
    }

    /**
     * Getter for the phone number towards which the SMSes should be send
     */
    public String getSmsPhoneNumber() {
        return getStringValue(SMS_PHONE_NUMBER_PROP, "");
    }

    /**
     * Getter for the general poll timeout (expressed in milliseconds).
     */
    public long getPollTimeOut() {
        if (this.pollTimeOut == 0) {
            this.pollTimeOut = getLongProperty(WAKEUP_POLLING_TIMEOUT, DEFAULT_WAKEUP_POLLING_TIMEOUT) * 1000L;
        }
        return this.pollTimeOut;
    }

    /**
     * Getter for the number of milliseconds to sleep before doing the first poll
     */
    public long getFirstPollDelay() {
        if (this.firstPollDelay == 0) {
            parsePollFrequencies();
        }
        return this. firstPollDelay;
    }

    /**
     * Getter for the number of milliseconds to sleep between consecutive polls
     */
    public long getSecondPollDelay() {
        if (this.secondPollDelay == 0) {
            parsePollFrequencies();
        }
        return this.secondPollDelay;
    }

    private void parsePollFrequencies() {
        String pollFreqProp = getStringValue(WAKEUP_POLLING_FREQUENCY, DEFAULT_WAKEUP_POLLING_FREQUENCY);
        String[] freqs = pollFreqProp.split(":");
        try {
            firstPollDelay = Integer.parseInt(freqs[0]);
        } catch (NumberFormatException e) {
            firstPollDelay = DEFAULT_FIRST_POLL_DELAY;
        }
        if (freqs.length == 1) {
            secondPollDelay = DEFAULT_SECOND_POLL_DELAY;
        } else {
            try {
                secondPollDelay = Integer.parseInt(freqs[1]);
            } catch (NumberFormatException e) {
                secondPollDelay = DEFAULT_SECOND_POLL_DELAY;
            }
        }

        // We check if the values are below 1000, then they are given in seconds so we should convert them to milliseconds
        // else, then we should just keep them in milliseconds
        firstPollDelay = (firstPollDelay < 1000) ? (firstPollDelay * 1000) : firstPollDelay;
        secondPollDelay = (secondPollDelay < 1000) ? (secondPollDelay * 1000) : secondPollDelay;
    }

    public List<String> getOptionalSmsWakeUpKeys() {
        ArrayList<String> smsWakeUpKeys = new ArrayList<String>();
        smsWakeUpKeys.add(SMS_BASE_URL_PROP);
        smsWakeUpKeys.add(SMS_SOURCE_PROP);
        smsWakeUpKeys.add(SMS_AUTH_PROP);
        smsWakeUpKeys.add(SMS_SERVICE_CODE_PROP);
        smsWakeUpKeys.add(SMS_PHONE_NUMBER_PROP);

        smsWakeUpKeys.add(SmsWakeUpDlmsProtocolProperties.WAKEUP_POLLING_TIMEOUT);
        smsWakeUpKeys.add(SmsWakeUpDlmsProtocolProperties.WAKEUP_POLLING_FREQUENCY);
        return smsWakeUpKeys;
    }

}
