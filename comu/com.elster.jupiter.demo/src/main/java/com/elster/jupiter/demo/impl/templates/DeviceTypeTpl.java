package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.Builder;
import com.elster.jupiter.demo.impl.builders.DeviceTypeBuilder;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link Template} holding a set of predefined attributes for creating Device Types based on a number of standard protocols
 * <p>
 * Copyrights EnergyICT
 * Date: 17/09/2015
 * Time: 9:56
 */
public enum DeviceTypeTpl implements Template<DeviceType, DeviceTypeBuilder> {
    Elster_AS1440("Elster AS1440", "com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP", 245, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY, LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG)) {
        @Override
        public List<ProtocolSupportedCalendarOptions> getTimeOfUseOptions() {
            return Arrays.asList(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }

        @Override
        protected List<CalendarTpl> getCalendars() {
            return Arrays.asList(CalendarTpl.RE_CU_01, CalendarTpl.RE_CU_02);
        }
    },

    Elster_A1800("Elster A1800", "com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP", 352, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY, LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG)) {
        @Override
        protected List<ProtocolSupportedCalendarOptions> getTimeOfUseOptions() {
            return Arrays.asList(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }

        @Override
        protected List<CalendarTpl> getCalendars() {
            return Arrays.asList(CalendarTpl.RE_CU_01, CalendarTpl.RE_CU_02);
        }
    },

    Landis_Gyr_ZMD("Landis+Gyr ZMD", "com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP", 73, OutboundTCPComPortPoolTpl.VODAFONE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY, LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG)) {
        @Override
        protected List<ProtocolSupportedCalendarOptions> getTimeOfUseOptions() {
            return Arrays.asList(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }

        @Override
        protected List<CalendarTpl> getCalendars() {
            return Arrays.asList(CalendarTpl.RE_CU_01, CalendarTpl.RE_CU_02);
        }
    },

    Actaris_SL7000("Actaris SL7000", "com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP", 110, OutboundTCPComPortPoolTpl.VODAFONE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY, LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG)) {
        @Override
        protected List<ProtocolSupportedCalendarOptions> getTimeOfUseOptions() {
            return Arrays.asList(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }

        @Override
        protected List<CalendarTpl> getCalendars() {
            return Arrays.asList(CalendarTpl.RE_CU_01, CalendarTpl.RE_CU_02);
        }
    },

    Siemens_7ED("Siemens 7ED", "com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP", 96, OutboundTCPComPortPoolTpl.VODAFONE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY, LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG)) {
        @Override
        protected List<ProtocolSupportedCalendarOptions> getTimeOfUseOptions() {
            return Arrays.asList(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }

        @Override
        protected List<CalendarTpl> getCalendars() {
            return Arrays.asList(CalendarTpl.RE_CU_01, CalendarTpl.RE_CU_02);
        }
    },

    Iskra_38("Iskra 382", "com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP", 84, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY, LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS, LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG)) {
        @Override
        protected List<ProtocolSupportedCalendarOptions> getTimeOfUseOptions() {
            return Arrays.asList(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR, ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }

        @Override
        protected List<CalendarTpl> getCalendars() {
            return Arrays.asList(CalendarTpl.RE_CU_01, CalendarTpl.RE_CU_02);
        }
    },

    Alpha_A3("ALPHA_A3", "com.energyict.protocolimpl.elster.a3.AlphaA3", 1, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.BULK_A_PLUS_ALL_PHASES, RegisterTypeTpl.BULK_A_MINUS_ALL_PHASES, RegisterTypeTpl.BULK_REACTIVE_ENERGY_PLUS, RegisterTypeTpl.BULK_REACTIVE_ENERGY_MINUS),
            Collections.singletonList(LoadProfileTypeTpl.ELSTER_A3_GENERIC),
            Arrays.<LogBookTypeTpl>asList(LogBookTypeTpl.STANDARD_EVENT_LOG, LogBookTypeTpl.FRAUD_DETECTION_LOG, LogBookTypeTpl.DISCONNECTOR_CONTROL_LOG)),
    RTU_Plus_G3("Elster RTU+Server G3", "com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer", 1, OutboundTCPComPortPoolTpl.ORANGE,
            Collections.<RegisterTypeTpl>emptyList(),
            Collections.<LoadProfileTypeTpl>emptyList(),
            Collections.<LogBookTypeTpl>emptyList()),
    AM540("AM540", "com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540", 1, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY, LoadProfileTypeTpl.DAILY_ELECTRICITY, LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Collections.singletonList(LogBookTypeTpl.GENERIC)),
    AS3000("Elster AS3000", "com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540", 1, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl._15_MIN_ELECTRICITY, com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl.DAILY_ELECTRICITY, com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Collections.singletonList(com.elster.jupiter.demo.impl.templates.LogBookTypeTpl.GENERIC)),
    AS220("Elster AS220", "com.energyict.protocolimplv2.nta.dsmr50.elster.am540.AM540", 1, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_PLUS_TOU_2, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_1, RegisterTypeTpl.SECONDARY_SUM_A_MINUS_TOU_2),
            Arrays.<LoadProfileTypeTpl>asList(com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl._15_MIN_ELECTRICITY, com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl.DAILY_ELECTRICITY, com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl.MONTHLY_ELECTRICITY),
            Collections.singletonList(com.elster.jupiter.demo.impl.templates.LogBookTypeTpl.GENERIC)),
    WEBRTU_Z2("WebRTU Z2", "com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP", 1, OutboundTCPComPortPoolTpl.ORANGE,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.DATA_LOGGER_1, RegisterTypeTpl.DATA_LOGGER_2, RegisterTypeTpl.DATA_LOGGER_3, RegisterTypeTpl.DATA_LOGGER_4, RegisterTypeTpl.DATA_LOGGER_5, RegisterTypeTpl.DATA_LOGGER_6
                    , RegisterTypeTpl.DATA_LOGGER_7, RegisterTypeTpl.DATA_LOGGER_8, RegisterTypeTpl.DATA_LOGGER_9, RegisterTypeTpl.DATA_LOGGER_10, RegisterTypeTpl.DATA_LOGGER_11, RegisterTypeTpl.DATA_LOGGER_12
                    , RegisterTypeTpl.DATA_LOGGER_13, RegisterTypeTpl.DATA_LOGGER_14, RegisterTypeTpl.DATA_LOGGER_15, RegisterTypeTpl.DATA_LOGGER_16, RegisterTypeTpl.DATA_LOGGER_17, RegisterTypeTpl.DATA_LOGGER_18
                    , RegisterTypeTpl.DATA_LOGGER_19, RegisterTypeTpl.DATA_LOGGER_20, RegisterTypeTpl.DATA_LOGGER_21, RegisterTypeTpl.DATA_LOGGER_22, RegisterTypeTpl.DATA_LOGGER_23, RegisterTypeTpl.DATA_LOGGER_24
                    , RegisterTypeTpl.DATA_LOGGER_25, RegisterTypeTpl.DATA_LOGGER_26, RegisterTypeTpl.DATA_LOGGER_27, RegisterTypeTpl.DATA_LOGGER_28, RegisterTypeTpl.DATA_LOGGER_29, RegisterTypeTpl.DATA_LOGGER_30
                    , RegisterTypeTpl.DATA_LOGGER_31, RegisterTypeTpl.DATA_LOGGER_32
            ),
            Collections.singletonList(LoadProfileTypeTpl.DATA_LOGGER_32),
            Collections.emptyList()),
    EIMETER_FLEX("EIMeter flex", null, 10, null,
            Arrays.<RegisterTypeTpl>asList(RegisterTypeTpl.SECONDARY_BULK_A_PLUS, RegisterTypeTpl.SECONDARY_BULK_A_MINUS),
            Collections.singletonList(LoadProfileTypeTpl._15_MIN_ELECTRICITY)),;

    private String longName;
    private String protocol;
    private int deviceCount;
    private OutboundTCPComPortPoolTpl poolTpl;

    private List<RegisterTypeTpl> registerTypes;
    private List<LoadProfileTypeTpl> loadProfileTypes;
    private List<LogBookTypeTpl> logBookTypes;

    DeviceTypeTpl(String name, String protocol, int deviceCount, OutboundTCPComPortPoolTpl poolTpl, List<RegisterTypeTpl> registerTypes, List<LoadProfileTypeTpl> loadProfileTypes, List<LogBookTypeTpl> logBookTypes) {
        this(name, protocol, deviceCount, poolTpl, registerTypes, loadProfileTypes);
        this.logBookTypes = logBookTypes;
    }

    DeviceTypeTpl(String name, String protocol, int deviceCount, OutboundTCPComPortPoolTpl poolTpl, List<RegisterTypeTpl> registerTypes, List<LoadProfileTypeTpl> loadProfileTypes) {
        this.longName = name;
        this.protocol = protocol;
        this.deviceCount = deviceCount;
        this.poolTpl = poolTpl;
        this.registerTypes = registerTypes;
        this.loadProfileTypes = loadProfileTypes;
    }

    public void tuneDeviceCountForSpeedTest() {
        this.deviceCount = 1;
    }

    @Override
    public Class<DeviceTypeBuilder> getBuilderClass() {
        return DeviceTypeBuilder.class;
    }

    @Override
    public DeviceTypeBuilder get(DeviceTypeBuilder builder) {
        builder.withName(this.longName).withProtocol(this.protocol)
                .withRegisterTypes(this.registerTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.toList()))
                .withLoadProfileTypes(this.loadProfileTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.<LoadProfileType>toList()));
        if (this.logBookTypes != null) {
            builder.withLogBookTypes(this.logBookTypes.stream().map(tpl -> Builders.from(tpl).get()).collect(Collectors.<LogBookType>toList()));
        }
        builder.withTimeOfUseOptions(getTimeOfUseOptions());
        builder.withCalendars(getCalendars().stream().map(Builders::from).map(Builder::get).collect(Collectors.toList()));
        return builder;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public OutboundTCPComPortPoolTpl getPoolTpl() {
        return poolTpl;
    }

    public String getName() {
        return longName;
    }

    public String getProtocol() {
        return protocol;
    }

    protected List<ProtocolSupportedCalendarOptions> getTimeOfUseOptions() {
        return Collections.emptyList();
    }

    protected List<CalendarTpl> getCalendars() {
        return Collections.emptyList();
    }

    public static DeviceTypeTpl fromName(String name) {
        return Arrays.stream(values()).filter(x -> x.name().equals(name)).findFirst().orElseThrow(() -> new IllegalArgumentException("No template having longName '" + name + "'"));
    }
}
