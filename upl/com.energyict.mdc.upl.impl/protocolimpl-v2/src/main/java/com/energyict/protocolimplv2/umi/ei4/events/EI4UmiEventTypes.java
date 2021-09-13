package com.energyict.protocolimplv2.umi.ei4.events;

public enum EI4UmiEventTypes {
    EGM_EVENT_VERIFICATION_WRITE(0, "Verification object write"),
    EGM_EVENT_REVERSE_FLOW(1, "Reverse flow"),
    EGM_EVENT_SOFTWARE_UPDATE(2, "Software upgrade"),
    EGM_EVENT_CONFIG_UPDATE(3, "Configuration update"),
    EGM_EVENT_MISER_MODE(4, "Miser mode"),
    EGM_EVENT_VALVE_RELEASED(5, "Valve released"),
    EGM_EVENT_VALVE_CLOSED(6, "Valve closed"),
    EGM_EVENT_VALVE_RELEASE_FAULT(7, "Valve release fault"),
    EGM_EVENT_VALVE_CLOSE_FAULT(8, "Valve close fault"),
    EGM_EVENT_BATTERY(9, "Battery"),
    EGM_EVENT_CASE_OPENED(10, "Case opened"),
    EGM_EVENT_CASE_CLOSED(11, "Case closed"),
    EGM_EVENT_MAGNET_ON(12, "Magnet on"),
    EGM_EVENT_MAGNET_OFF(13, "Magnet off"),
    EGM_EVENT_UMI_CONTROL(14, "UMI control"),
    EGM_EVENT_SOFTWARE_RESTART_REQUEST(15, "Software restart request"),
    EGM_EVENT_SOFTWARE_RESTART(16, "Software restart"),
    EGM_EVENT_OPTO_COMMUNICATIONS(17, "Opto communications"),
    EGM_EVENT_CLOCK_SET(18, "Clock set"),
    EGM_EVENT_LEAKY_VALVE(19, "Leaky valve"),
    EGM_EVENT_HIGH_FLOW_RATE(20, "High flow rate"),
    EGM_EVENT_ENV_CONDITIONS(21, "Environmental conditions"),
    EGM_EVENT_SOFTWARE_ALARM(22, "Software alarm"),
    EGM_EVENT_PERMLOG_90_PERCENT(23, "Permanent log 90% full"),
    EGM_EVENT_ERASE_EVENTS(24, "Event erase"),
    EGM_EVENT_FAILED_AUTHENTICATION(25, "Failed authentication"),
    EGM_EVENT_SPARE26(26, "Spare"),
    EGM_EVENT_SELFTEST_FAILURE(27, "Self-test failure"),
    EGM_EVENT_BAD_PERIPHERAL(28, "Bad peripheral"),
    EGM_EVENT_PERIPHERAL_FOUND(29, "Peripheral found"),
    EGM_EVENT_BAD_DECRYPT(30, "Bad decrypt"),
    EGM_EVENT_SPARE31(31, "Spare"),
    EGM_EVENT_ECO_MODE(32, "Eco made"),
    EGM_EVENT_VLINE_TOOHIGH_FAILURES(33, "VLAN too high failures"),
    EGM_EVENT_VLINE_UNAVAILABLE_FAILURES(34, "VLAN unavailable failures"),
    EGM_EVENT_VOS_TOOLOW_FAILURES(35, "VOS too low failures"),
    EGM_EVENT_VOS_TOOHIGH_FAILURES(36, "VOS too high failures"),
    EGM_EVENT_VOS_UNAVAILABLE_FAILURES(37, "VOS unavailable failures"),
    EGM_EVENT_VOS_TAMPER(38, "VOS tamper"),
    EGM_EVENT_SNR_FAULTS(39, "SNR faults"),
    EGM_EVENT_NML_ENERGY_FAULTS(40, "Normal energy faults"),
    EGM_EVENT_1L_GAININC_FAILURES(41, "1L gian increment failures"),
    EGM_EVENT_1L_GAINDEC_FAILURES(42, "1L gain decrement failures"),
    EGM_EVENT_3L_GAININC_FAILURES(43, "3L gain increment failures"),
    EGM_EVENT_3L_GAINDEC_FAILURES(44, "#L gain decrement failures"),
    EGM_EVENT_GSM_AT_CMD_FAIL(45, "GSM at CMD fail"),
    EGM_EVENT_GSM_VOLTAGE_LOW(46, "GSM voltage low"),
    EGM_EVENT_GSM_SIGNAL_STRENGTH(47, "GSM signal strength"),
    EGM_EVENT_RETRIES_LEFT_FROM_MAX(48, "Retries left from maximum"),
    EGM_EVENT_NONE(255, "None");

    private final String description;
    private final int value;

    EI4UmiEventTypes(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static EI4UmiEventTypes fromValue(int value) {
        for (EI4UmiEventTypes state : values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        return EGM_EVENT_NONE;
    }
}
