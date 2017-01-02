package com.energyict.mdc.upl.messages;

import java.util.Arrays;
import java.util.Optional;

public enum ProtocolSupportedCalendarOptions {
    VERIFY_ACTIVE_CALENDAR("verify"),
    SEND_ACTIVITY_CALENDAR("send"),
    SEND_ACTIVITY_CALENDAR_WITH_DATE("sendWithDate"),
    SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE("sendWithDateAndType"),
    SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT("sendWithDateAndContract"),
    SEND_ACTIVITY_CALENDAR_WITH_DATETIME("sendWithDateTime"),
    SEND_SPECIAL_DAYS_CALENDAR("sendSpecialDays"),
    SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE("sendSpecialDaysWithType"),
    SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE("sendSpecialDaysWithContractAndDate"),
    CLEAR_AND_DISABLE_PASSIVE_TARIFF("clearAndDisablePassiveTariff"),
    ACTIVATE_PASSIVE_CALENDAR("activatePassive");

    private String id;

    ProtocolSupportedCalendarOptions(String id) {
        this.id = id;
    }

    public static Optional<ProtocolSupportedCalendarOptions> from(String id) {
        return Arrays.stream(values()).filter(option -> option.getId().equals(id)).findFirst();
    }

    public String getId() {
        return id;
    }
}