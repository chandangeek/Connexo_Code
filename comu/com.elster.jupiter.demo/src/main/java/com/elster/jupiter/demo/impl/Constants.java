package com.elster.jupiter.demo.impl;

import java.util.Optional;

public final class Constants {
    private Constants() {}

    public static final class IssueCreationRule {
        public static final String TYPE_CONNECTION_LOST = "CONNECTION_LOST";
        public static final String TYPE_CONNECTION_SETUP_LOST = "UNABLE_TO_CONNECT";
        public static final String TYPE_COMMUNICATION_FAILED = "DEVICE_COMMUNICATION_FAILURE";

        private IssueCreationRule() {}
    }

    public static final class CreationRuleTemplate{
        public static final String BASIC_DATA_COLLECTION_UUID = "e29b-41d4-a716";

        private CreationRuleTemplate() {}
    }

    public static final class CreationRule {
        public static final String CONNECTION_LOST = "Connection failed";
        public static final String CONNECTION_SETUP_LOST = "Connection setup failed";
        public static final String COMMUNICATION_FAILED = "Device communication failed";

        private CreationRule() {}
    }

    public static final class DeviceGroup{
        public static final String SOUTH_REGION = "South region";
        public static final String NORTH_REGION = "North region";
        public static final String ALL_ELECTRICITY_DEVICES = "All electricity devices";

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
        public static final String BOB = "Bob";

        private User() {}
    }

    public static final class UserRoles{
        public static final String ADMINISTRATORS = "Administrators";
        public static final String METER_EXPERT = "Meter expert";
        public static final String METER_OPERATOR = "Meter operator";
        public static final String SECURITY_EXPERT = "Security expert";
        public static final String SECURITY_EXPERT_DESCRIPTION = "Manage security sets and keys";

        private UserRoles() {}
    }

    public static final class RegisterGroup {
        public static final String TARIFF_1 = "Tariff 1";
        public static final String TARIFF_2 = "Tariff 2";
        public static final String DEVICE_DATA = "Device data";

        private RegisterGroup() {}
    }

    public static final class LogBookType {
        public static final String DEFAULT_LOGBOOK = "Default Logbook";

        private LogBookType() {}
    }

    public static final class LoadProfileType {
        public static final String DAILY_ELECTRICITY = "Daily Electricity";
        public static final String MONTHLY_ELECTRICITY = "Monthly Electricity";
        public static final String _15_MIN_ELECTRICITY = "15min Electricity";

        private LoadProfileType() {}
    }

    public static final class ComPortPool {
        public static final String VODAFONE = "Vodafone";
        public static final String ORANGE = "Orange";
        public static final String INBOUND_SERVLET_POOL = "Inbound Servlet Pool";

        private ComPortPool() {}
    }

    public static final class CommunicationSchedules {
        public static final String DAILY_READ_ALL = "Daily read all";

        private CommunicationSchedules() {}
    }

    public static final class CommunicationTask{
        public static final String READ_ALL = "Read all";
        public static final String TOPOLOGY = "Topology";
        public static final String READ_REGISTER_DATA = "Read register data";
        public static final String READ_LOAD_PROFILE_DATA = "Read load profile data";
        public static final String READ_LOG_BOOK_DATA = "Read logbook data";

        private CommunicationTask() {}
    }

    public static final class Device{
        public static final String STANDARD_PREFIX = "SPE";
        public static final String MOCKED_VALIDATION_DEVICE = "VPB";
        public static final String A3WIC16499990 = "A3WIC16499990";

        private Device() {}
    }

    public static final class Validation {
        public static final String RULE_SET_NAME = "Residential customers";
        public static final String RULE_SET_DESCRIPTION = "Set with rules regarding residential customers";
        public static final String DETECT_MISSING_VALUES = "Detect missing values";
        public static final String REGISTER_INCREASE = "Register increase";
        public static final String DETECT_THRESHOLD_VIOLATION = "Detect threshold violation";

        private Validation() {}
    }

    public static final class DeviceConfiguration{
        public static final String DEFAULT = "Default";

        private DeviceConfiguration() {}
    }

    public static class OutboundTcpComPort {
        public static final String TCP_1 = "Outbound TCP 1";
        public static final String TCP_2 = "Outbound TCP 2";

        private OutboundTcpComPort() {}
    }

    public static final class RegisterTypes {
        public static final String B_F_E_S_M_E = "Active Energy Import Tariff 1 (kWh)";
        public static final String B_R_E_S_M_E = "Active Energy Import Tariff 1 (Wh)";
        public static final String B_F_E_S_M_E_T1 = "Active Energy Import Tariff 2 (kWh)";
        public static final String B_F_E_S_M_E_T2 = "Active Energy Import Tariff 2 (Wh)";
        public static final String B_R_E_S_M_E_T1 = "Active Energy Export Tariff 1 (kWh)";
        public static final String B_R_E_S_M_E_T2 = "Active Energy Export Tariff 1 (Wh)";

        private RegisterTypes() {}
    }

    public static enum DeviceType {
        Elster_AS1440 ("Elster AS1440", 1 /*245*/),
        Elster_AS3000 ("Elster AS3000", 1 /*352*/),
        Landis_Gyr_ZMD ("Landis+Gyr ZMD", 1 /*73*/),
        Actaris_SL7000 ("Actaris SL7000", 1 /*110*/),
        Siemens_7ED ("Siemens 7ED", 1 /*96*/),
        Iskra_38 ("Iskra 382", 1 /*84*/),
        Alpha_A3 ("ALPHA_A3", 1),
        ;

        private String name;
        private int deviceCount;

        private DeviceType(String name, int deviceCount) {
            this.deviceCount = deviceCount;
            this.name = name;
        }

        public String getName(){
            return this.name;
        }

        public int getDeviceCount() {
            return deviceCount;
        }

        public static Optional<DeviceType> from(String name){
            for (DeviceType deviceType : DeviceType.values()) {
                if (deviceType.getName().equals(name)){
                    return Optional.of(deviceType);
                }
            }
            return Optional.empty();
        }
    }

    public static class IssueReason {
        public static final String CONNECTION_FAILED = "reason.connection.failed";
        public static final String CONNECTION_SETUP_FAILED = "reason.connection.setup.failed";
        public static final String COMMUNICATION_FAILED = "reason.failed.to.communicate";

        private IssueReason() {}
    }
}
