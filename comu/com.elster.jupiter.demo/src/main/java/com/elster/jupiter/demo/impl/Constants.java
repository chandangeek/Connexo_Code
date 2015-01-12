package com.elster.jupiter.demo.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public final class Constants {
    private Constants() {}

    public static final class IssueCreationRule {
        public static final String TYPE_CONNECTION_LOST = "CONNECTION_LOST";
        public static final String TYPE_COMMUNICATION_FAILED = "DEVICE_COMMUNICATION_FAILURE";

        private IssueCreationRule() {}
    }

    public static final class CreationRuleTemplate{
        public static final String BASIC_DATA_COLLECTION_UUID = "e29b-41d4-a716";

        private CreationRuleTemplate() {}
    }

    public static final class CreationRule {
        public static final String CONNECTION_LOST = "Connection lost rule";
        public static final String COMMUNICATION_FAILED = "Communication failed rule";

        private CreationRule() {}
    }

    public static final class DeviceGroup{
        public static final String SOUTH_REGION = "South region";
        public static final String NORTH_REGION = "North region";

        private DeviceGroup() {}
    }

    public static final class User{
        public static final String MELISSA = "Melissa";
        public static final String SAM = "Sam";
        public static final String PIETER = "Pieter";
        public static final String JOLIEN = "Jolien";
        public static final String INGE = "Inge";
        public static final String KOEN = "Koen";
        public static final String SEBASTIEN = "Sebastien";
        public static final String VEERLE = "Veerle";
        public static final String KURT = "Kurt";
        public static final String EDUARDO = "Eduardo";

        private User() {}
    }

    public static final class UserRoles{
        public static final String ADMINISTRATORS = "Administrators";
        public static final String METER_EXPERT = "Meter expert";
        public static final String METER_OPERATOR = "Meter operator";

        private UserRoles() {}
    }

    public static final class AppServer{
        public static final String DEFAULT = "Description";

        private AppServer() {}
    }

    public static final class RegisterGroup {
        public static final String DEFAULT_GROUP = "Default group";
        public static final String TARIFF_1 = "Tariff 1";
        public static final String TARIFF_2 = "Tariff 2";
        public static final String DEVICE_DATA = "Device data";

        private RegisterGroup() {}
    }

    public static final class LogBookType {
        public static final String DEFAULT_LOGBOOK = "Default Logbook";
        public static final String POWER_FAILURES = "Power Failures";
        public static final String FRAUD_DETECTIONS = "Fraud Detections";

        private LogBookType() {}
    }

    public static final class LoadProfileType {
        public static final String DAILY_ELECTRICITY = "Daily Electricity";
        public static final String MONTHLY_ELECTRICITY = "Monthly Electricity";
        public static final String _15_MIN_ELECTRICITY = "15min Electricity";

        private LoadProfileType() {}
    }

    public static final class OutboundComPortPool{
        public static final String VODAFONE = "Vodafone";
        public static final String ORANGE = "Orange";
        public static final String OUTBOUND_TCP_POOL = "Outbound TCP Pool";

        private OutboundComPortPool() {}
    }

    public static final class CommunicationSchedules {
        public static final String DAILY_READ_ALL = "Daily read all";
        public static final String MONTHLY_BILLING_DATA = "Monthly billing data";

        private CommunicationSchedules() {}
    }

    public static final class CommunicationTask{
        public static final String READ_ALL = "Read all";
        public static final String FORCE_CLOCK = "Force clock";
        public static final String READ_DAILY = "Read daily";
        public static final String TOPOLOGY = "Topology";
        public static final String READ_REGISTER_BILLING_DATA = "Read register billing data";
        public static final String READ_LOAD_PROFILE_DATA = "Read load profile data";

        private CommunicationTask() {}
    }

    public static final class Kpi {
        public static final String CONNECTION = "Connection KPI";
        public static final String COMMUNICATION = "Communication KPI";

        private Kpi() {}
    }

    public static final class Device{
        public static final String STANDARD_PREFIX = "ZABF0100";
        public static final String DABF_12 = "DABF410005812";
        public static final String DABF_13 = "DABF410005813";

        private Device() {}
    }

    public static final class Validation {
        public static final String DETECT_MISSING_VALUES = "Detect missing values";

        private Validation() {}
    }

    public static final class DeviceConfiguration{
        public static final String EXTENDED_CONFIG = "Extended Config";

        private DeviceConfiguration() {}
    }

    public static final class RegisterTypes {
        public static final String BULK_A_FORWARD_ALL_PHASES_TOU_1_K_WH = "Active Energy Import Tariff 1 (kWh)";
        public static final String BULK_A_FORWARD_ALL_PHASES_TOU_1_WH = "Active Energy Import Tariff 1 (Wh)";
        public static final String BULK_A_FORWARD_ALL_PHASES_TOU_2_K_WH = "Active Energy Import Tariff 2 (kWh)";
        public static final String BULK_A_FORWARD_ALL_PHASES_TOU_2_WH = "Active Energy Import Tariff 2 (Wh)";
        public static final String BULK_A_REVERSE_ALL_PHASES_TOU_1_K_WH = "Active Energy Export Tariff 1 (kWh)";
        public static final String BULK_A_REVERSE_ALL_PHASES_TOU_1_WH = "Active Energy Export Tariff 1 (Wh)";
        public static final String BULK_A_REVERSE_ALL_PHASES_TOU_2_K_WH = "Active Energy Export Tariff 2 (kWh)";
        public static final String BULK_A_REVERSE_ALL_PHASES_TOU_2_WH = "Active Energy Export Tariff 2 (Wh)";
        public static final String BULK_A_FORWARD_ALL_PHASES_TOU_0_WH = "Active Energy Import Total (Wh)";
        public static final String BULK_A_REVERSE_ALL_PHASES_TOU_0_WH = "Active Energy Export Total (Wh)";
        public static final String ALARM_REGISTER = "Alarm register";
        public static final String AMR_PROFILE_STATUS_CODE = "AMR Profile status code";
        public static final String ACTIVE_FIRMWARE_VERSION = "Active firmware version";

        private RegisterTypes() {}
    }

    public static enum DeviceType {
        Elster_AS1440 ("Elster AS1440"),
        Elster_AS3000 ("Elster AS3000"),
        Landis_Gyr_ZMD ("Landis+Gyr ZMD"),
        Actaris_SL7000 ("Actaris SL7000"),
        Siemens_7ED ("Siemens 7ED"),
        Iskra_38 ("Iskra 382"),
        ;

        private String name;

        private DeviceType(String name) {
            this.name = name;
        }

        public String getName(){
            return this.name;
        }
    }

    public static enum IssueReason implements MessageSeed {
        DAILY_BILLING_READ_FAILED(1, "reason.demo", "Daily billing read failed", Level.INFO),
        SUSPECT_VALUES(2, "reason.demo.loadprofile", "Suspect values", Level.INFO),
        CONNECTION_FAILED(3, "reason.connection.failed", "", Level.INFO),
        COMMUNICATION_FAILED(4, "reason.failed.to.communicate", "", Level.INFO),
        ;

        private final int number;
        private final String key;
        private final String defaultFormat;
        private final Level level;

        IssueReason(int number, String key, String defaultFormat, Level level) {
            this.number = number;
            this.key = key;
            this.defaultFormat = defaultFormat;
            this.level = level;
        }

        @Override
        public String getModule() {
            return IssueService.COMPONENT_NAME;
        }

        @Override
        public int getNumber() {
            return this.number;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.defaultFormat;
        }

        @Override
        public Level getLevel() {
            return this.level;
        }
    }
}
