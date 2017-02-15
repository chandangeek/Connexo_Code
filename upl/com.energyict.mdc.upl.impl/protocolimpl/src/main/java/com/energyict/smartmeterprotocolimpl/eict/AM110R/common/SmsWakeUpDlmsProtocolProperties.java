package com.energyict.smartmeterprotocolimpl.eict.AM110R.common;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Interface containing all required property getters for Proximus SMS functionality
 *
 * @author sva
 * @since 23/05/13 - 9:00
 */
public abstract class SmsWakeUpDlmsProtocolProperties extends DlmsProtocolProperties {

    private static final String SMS_BASE_URL_PROP = "SmsConnectionURL";
    private static final String SMS_SERVICE_CODE_PROP = "SmsServiceCode";
    private static final String SMS_SOURCE_PROP = "SmsSource";
    private static final String SMS_AUTH_PROP = "SmsAuthentication";
    private static final String SMS_PHONE_NUMBER_PROP = "SmsPhoneNumber";

    private static final String WAKEUP_POLLING_TIMEOUT = "PollTimeOut";
    private static final String WAKEUP_POLLING_FREQUENCY = "PollFrequency";
    private static final String DEFAULT_WAKEUP_POLLING_TIMEOUT = "300";
    private static final String DEFAULT_WAKEUP_POLLING_FREQUENCY = "20:5";
    private static final int DEFAULT_FIRST_POLL_DELAY = 20;
    private static final int DEFAULT_SECOND_POLL_DELAY = 5;

    private long pollTimeOut;
    private int firstPollDelay;
    private int secondPollDelay;

    private final PropertySpecService propertySpecService;

    protected SmsWakeUpDlmsProtocolProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

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

    protected List<PropertySpec> getSmsWakeUpPropertySpecs(boolean required) {
        return Arrays.asList(
                this.stringSpec(SMS_BASE_URL_PROP, required, PropertyTranslationKeys.EICT_SMS_BASE_URL),
                this.stringSpec(SMS_SOURCE_PROP, required, PropertyTranslationKeys.EICT_SMS_SOURCE),
                this.stringSpec(SMS_AUTH_PROP, required, PropertyTranslationKeys.EICT_SMS_AUTH),
                this.stringSpec(SMS_SERVICE_CODE_PROP, required, PropertyTranslationKeys.EICT_SMS_SERVICE_CODE),
                this.stringSpec(SMS_PHONE_NUMBER_PROP, required, PropertyTranslationKeys.EICT_SMS_PHONE_NUMBER),
                this.longSpec(WAKEUP_POLLING_TIMEOUT, required, PropertyTranslationKeys.EICT_WAKEUP_POLLING_TIMEOUT),
                this.stringSpec(WAKEUP_POLLING_FREQUENCY, required, PropertyTranslationKeys.EICT_WAKEUP_POLLING_FREQUENCY));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private  <T> PropertySpec spec(String name, boolean required, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, required, translationKey, optionsSupplier).finish();
    }

    protected PropertySpec stringSpec(String name, boolean required, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, this.propertySpecService::stringSpec);
    }

    protected PropertySpec hexStringSpec(String name, boolean required, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, this.propertySpecService::hexStringSpec);
    }

    protected PropertySpec integerSpec(String name, boolean required, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, this.propertySpecService::integerSpec);
    }

    private PropertySpec longSpec(String name, boolean required, TranslationKey translationKey) {
        return this.spec(name, required, translationKey, this.propertySpecService::longSpec);
    }

}
