package com.energyict.mdc.common.license;

/**
 * Copyrights EnergyICT
 * Date: 1-mrt-2011
 * Time: 11:00:40
 */
public final class LicenseViolation {

    public static final LicenseViolation NUMBER_OPERATIONAL_USERS = new LicenseViolation(1);
    public static final LicenseViolation NUMBER_CUSTOMER_ENGAGEMENT_USERS = new LicenseViolation(1<<1);
    public static final LicenseViolation NUMBER_CHANNELS = new LicenseViolation(1<<2);
    public static final LicenseViolation NUMBER_DEVICES = new LicenseViolation(1<<3);
    public static final LicenseViolation NUMBER_REGISTERS = new LicenseViolation(1<<4);
    public static final LicenseViolation FORMAT = new LicenseViolation(1<<5);
    public static final LicenseViolation NUMBER_MOBILE_USERS = new LicenseViolation(1<<6);
    public static final LicenseViolation CHANNEL_VALIDATION_RULES = new LicenseViolation(1<<7);
    public static final LicenseViolation REGISTER_VALIDATION_RULES = new LicenseViolation(1<<8);
    public static final LicenseViolation CHANNEL_ESTIMATION_RULES = new LicenseViolation(1<<9);

    private int code;

    public LicenseViolation(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isNumberOfOperationalUsersViolated() {
        return ((this.getCode() & NUMBER_OPERATIONAL_USERS.getCode()) != 0);
    }

    public boolean isNumberOfCustomerEngagementUsersViolated() {
        return ((this.getCode() & NUMBER_CUSTOMER_ENGAGEMENT_USERS.getCode()) != 0);
    }

    public boolean isNumberOfChannelsViolated() {
        return ((this.getCode() & NUMBER_CHANNELS.getCode()) != 0);
    }

    public boolean isNumberOfDevicesViolated() {
        return ((this.getCode() & NUMBER_DEVICES.getCode()) != 0);
    }

    public boolean isNumberOfRegistersViolated() {
        return ((this.getCode() & NUMBER_REGISTERS.getCode()) != 0);
    }

    public boolean isFormatViolated() {
        return ((this.getCode() & FORMAT.getCode()) != 0);
    }

    public boolean isNumberOfMobileUsersViolated() {
        return ((this.getCode() & NUMBER_MOBILE_USERS.getCode()) != 0);
    }

    public boolean isChannelValidationRulesViolated() {
        return ((this.getCode() & CHANNEL_VALIDATION_RULES.getCode()) != 0);
    }

    public boolean isRegisterValidationRulesViolated() {
        return ((this.getCode() & REGISTER_VALIDATION_RULES.getCode()) != 0);
    }

    public boolean isChannelEstimationRulesViolated() {
        return ((this.getCode() & CHANNEL_ESTIMATION_RULES.getCode()) != 0);
    }

    public String toString() {
        return "" + getCode();
    }

}