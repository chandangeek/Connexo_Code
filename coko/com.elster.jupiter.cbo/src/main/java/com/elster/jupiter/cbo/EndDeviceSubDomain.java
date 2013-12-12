package com.elster.jupiter.cbo;

public enum EndDeviceSubDomain {
    NA("NA", 0, "Not applicable. Use when a domain is not needed. This should rarely be used"),
    ACCESS("Access", 1, "Related to physical security (ie. accessability) or electronic permission to read/write digital media"),
    AD_CONVERTER("ADConverter", 142, "Related to analog-to-digital conversion"),
    ALL_EVENTS("AllEvents", 148, "Related to a set of events (typically used in a Load Control Cancel All Events scenario)"),
    ALLOCATION("Allocation", 2, "Related to designation or allotment; Typically related to memory (RAM/ROM)"),
    ASSOCIATION("Association", 74, "Related to the linking/pairing of one device/object to another device/object"),
    AUTD_PROCESS("AUTDProcess", 3, "Related to Always Up To Date processes; watchdog or keepalive processes"),
    AUTO_REGISTRATION("AutoRegistration", 5, "Related to automatic registration process"),
    AUTO_TIME("AutoTime", 7, "Related to automatic setting of time"),
    BTU("BTU", 13, "Related to British Thermal Units"),
    BUFFER("Buffer", 14, "Related to temporary data storage"),
    CABLE("Cable", 15, "Related to a physical cable"),
    CALCULATION("Calculation", 16, "Related to mathematical computation"),
    CALIBRATION("Calibration", 18, "Related to a set of gradations that show positions or values"),
    CERTIFICATE("Certificate", 21, "Related to a document testifying to the truth of something; typically a security certificate"),
    CHARGE("Charge", 22, "Related to electrical charge; Related to billing charge"),
    CONCENTRATION("Concentration", 39, "Related to the density or composition of something"),
    CONSTANTS("Constants", 23, "Related to statically defined values"),
    CONTROL_POINT("ControlPoint", 26, "Related to load control settings"),
    COVER("Cover", 29, "Related to something that provides shelter; a covering"),
    CRC("CRC", 30, "Related to cyclical redundancy check"),
    CREDIT("Credit", 8, "Related to the right-hand side of an account; billing"),
    CURRENT("Current", 6, "Related to electrical power measured in amperes"),
    DATA("Data", 31, "Related to factual information"),
    DATA_LOG("DataLog", 33, "Related to a record (ie. log) of factual information"),
    DATE("Date", 34, "Related to calendar time"),
    DAY("Day", 35, "Related to the day portion of calendar time"),
    DAYLIGHT_SAVINGS_TIME("DaylightSavingsTime", 56, "Related to the practice of setting the clock forward one hour in the spring."),
    DECRYPTION("Decryption", 36, "Related to making encrypted data readable"),
    DISPLAY("Display", 143, "Related to a CRT, LED, or other form of video device"),
    DOOR("Door", 128, "Related to a moveable barrier used to cover an opening; as in a door to a meter or collector"),
    EMERGENCY_SUPPLY_CAPACITY_LIMIT("EmergencySupplyCapacityLimit", 138, "Related to emergency supply capacity limits"),
    ENCODER("Encoder", 40, "Related to the thing that converts information from one format to another"),
    ENCODER_REGISTER("EncoderRegister", 41, "Related to the encoder register (ie. on a meter)"),
    EPROM("EPROM", 42, "Related to erasable programmable read-only memory"),
    EVENT("Event", 43, "Related to something that has happened; other, more specific subdomains should be used before using this one"),
    EVENT_LOG("EventLog", 44, "Related to a record (ie. log) of event data"),
    EWM("EWM", 45, "Related to an external wireless module"),
    FEATURE("Feature", 46, "Related to a non-specific characteristic"),
    FIRMWARE_RESET("FirmwareReset", 47, "Related to reverting of firmware to original state"),
    FLOW("Flow", 48, "Related to the movement of a substance (electricity, gas, water, etc.)"),
    FPV("FPV", 49, "Related to a form of super-compressibility"),
    FRAMES("Frames", 50, "Related to fixed-sized blocks; as in memory"),
    FREQUENCY("Frequency", 4, "Related to the number of cycles per unit of time"),
    GC_ANALYZER("GCAnalyzer", 51, "Related to gas chromatograph analyzer which is use to measure the component mixture of the natural gas delivers to a site"),
    HEAD_END_SYSTEM("HeadEndSystem", 52, "Related to the metering/AMI system"),
    HISTORY_LOG("HistoryLog", 53, "Related to a record (ie. log) of historical data"),
    HMAC("HMAC", 54, "Related to hash-based message authentication code; a specific method for calculated a MAC"),
    HOLIDAY("Holiday", 97, "Related to days set aside having special significance"),
    IDENTITY("Identity", 10, "Related to a unique identifier"),
    INPUT("Input", 55, "Related to data entered into the system"),
    INSTALL_DATE("InstallDate", 57, "Related to the prepared for use date"),
    INTELLIGENT_REGISTER("IntelligentRegister", 58, "Related to a specific register on a device"),
    INTERVAL("Interval", 59, "Related to interval energy data; the time between to events"),
    IO("IO", 60, "Related to general input/output"),
    IP_ADDRESS("IPAddress", 127, "Related to an IP Address (Internet Protocol Address)"),
    LAN_ADDRESS("LANAddress", 61, "Related to a unique identification of a device on a network of devices"),
    LAST_READ("LastRead", 62, "Related to the final reading from a meter"),
    LIST("List", 63, "Related to an internal list [contained in memory or on firmware]"),
    LIST_POINTERS("ListPointers", 64, "Related to a specific set of values kept by a meter"),
    LOGIN("Login", 65, "Related to the process by which access is gained to a device, computer, or system"),
    LOW_SPEED_BUS("LowSpeedBus", 145, "Related to a circuit that connects CPU with other devices; lowspeed transmission"),
    MAGNETIC_SWITCH("MagneticSwitch", 66, "Related to any type of magnetic switch"),
    MAINT_MODE("MaintMode", 11, "Related to a specific mode of operation into which a device can be set"),
    MEASUREMENT("Measurement", 67, "Relating to the magnitude of a quantity"),
    MESH("Mesh", 68, "Typically related to the type of meter network"),
    METER_BUS("MeterBus", 147, "Related to a circuit that connects a devices or module to a meter"),
    METERING_MODE("MeteringMode", 12, "Related to a specific mode of operation into which a device can be set"),
    MOBILE("Mobile", 69, "Related to devices that are not confined to one place"),
    MOL("MOL%", 70, "Related to percentage of moles"),
    NETWORK_ID("NetworkId", 71, "Related to a unique identification of a device on a network of devices"),
    NEUTRAL_CURRENT("NeutralCurrent", 137, "Related to the essential part of electroweak unification"),
    NVRAM("NVRAM", 72, "Related to non-volatile random access memory"),
    OPTION_BOARD("OptionBoard", 146, "Related to a type of module in a meter"),
    PARAMETER("Parameter", 75, "Related to a variable passed to a function"),
    PARENT_DEVICE("ParentDevice", 76, "Related to a device’s owner"),
    PARITY("Parity", 77, "Typically related to an odd/even or on/off state; a symmetry property"),
    PASSWORD("Password", 24, "Related to a secret word used for authentication"),
    PHASE("Phase", 25, "Typically related to a means of distributing alternating current; When the specific phase is irrelevant, this should be used as the EndDeviceSubdomain"),
    PHASE_ANGLE("PhaseAngle", 130, "Related to the angular component of the polar coordinates"),
    PHASE_A_VOLTAGE("PhaseAVoltage", 131, "Related to the voltage of the first phase of 3-phase power"),
    PHASE_A_VOLTAGE_POTENTIAL("PhaseAVoltagePotential", 126, "Related to the voltage potential of the first phase of 3-phase power"),
    PHASE_B_VOLTAGE("PhaseBVoltage", 132, "Related to the voltage of the second phase of 3-phase power"),
    PHASE_B_VOLTAGE_POTENTIAL("PhaseBVoltagePotential", 134, "Related to the voltage potential of the second phase of 3-phase power"),
    PHASE_C_VOLTAGE("PhaseCVoltage", 133, "Related to the voltage of the third phase of 3-phase power"),
    PHASE_C_VOLTAGE_POTENTIAL("PhaseCVoltagePotential", 135, "Related to the voltage potential of the third phase of 3-phase power"),
    PHASE_SEQUENCE("PhaseSequence", 78, "Related to the order of the phases in multi-phase power"),
    PHASE_VOLTAGE("PhaseVoltage", 79, "In single-phase or in situations where the specific phase is irrelevant, this is related to voltage across the phase"),
    POWER_FACTOR("PowerFactor", 27, "Related to the ratio of the real power flowing to the load to the apparent power in the circuit"),
    POWER_QUALITY("PowerQuality", 28, "Related to the set of limits of electrical properties that allows electrical systems to function in their intended manner without significant loss of performance"),
    POWER_QUALITY_RECORDING("PowerQualityRecording", 80, "Related to the capture and storage of power quality data"),
    PREPAYMENT_CREDIT("PrepaymentCredit", 81, "Related to the right-hand side of an account; billing for prepayment accounts"),
    PRICING("Pricing", 9, "Related to billing"),
    PROCESSOR("Processor", 82, "Related to a CPU, typically"),
    PROGRAM("Program", 83, "Related to a pre-defined set of instructions"),
    PULSE("Pulse", 84, "Related to a means by which energy is measured"),
    QUEUE("Queue", 197, "Related to a relatively temporary storage area used to hold requests or tasks until they can be processed"),
    RADIO("Radio", 136, "Related to a physical device that processes radio signals"),
    RAM("RAM", 85, "Related to random access memory"),
    RATE("Rate", 86, "Related to the speed or velocity"),
    READ_ACCESS("ReadAccess", 202, "Related to the permission level one has; as in read, write, update"),
    READINGS("Readings", 87, "Related to the collection of consumption, diagnostic, and status data from a meter"),
    RECOVERY("Recovery", 88, "Related to a process of restoring from a broken state"),
    REGISTER("Register", 89, "Related to a placeholder for information"),
    REGISTRATION("Registration", 90, "Related to a process by which a device is recognized or added"),
    RELAY("Relay", 91, "Related to an electrically operated switch"),
    REMOTE_ACCESS("RemoteAccess", 211, "Related to physical security (ie. accessability) or electronic permission to read/write digital media from a mobile device or from a location other than where the object being accessed is"),
    ROM("ROM", 92, "Related to read-only memory"),
    ROTATION("Rotation", 93, "Related to the movement of an object in a circular motion"),
    RTP("RTP", 94, "Related to real-time pricing"),
    SCHEDULE("Schedule", 95, "Related to a timetable or plan of future events"),
    SEASON("Season", 228, "Related to the division of a year marked by changes in weather; typically winter, spring, summer, and fall"),
    SECONDARY_CREDIT("SecondaryCredit", 96, "Related to a non-primary amount of credit"),
    SECURED_REGISTER("SecuredRegister", 98, "Related to a specific register on a device"),
    SECURED_TABLE("SecuredTable", 99, "Related a table that requires authorization prior to access being granted"),
    SECURITY_KEY("SecurityKey", 32, "Related to a piece of information that determines the functional output of a cryptographic cipher"),
    SECURITY_KEY_LENGTH("SecurityKeyLength", 119, "Related to the length of a security key"),
    SECURITY_KEY_VERSION("SecurityKeyVersion", 120, "Related to the version of a security key"),
    SELF_READ("SelfRead", 231, "Related to a process where a device will read itself"),
    SELF_TEST("SelfTest", 100, "Related to a process where a device will run an internal test on itself"),
    SENSOR("Sensor", 144, "Related to a mechanical device that transmits a signal to a measuring device"),
    SESSION("Session", 129, "Related to a communication session, typically"),
    SET_POINT("SetPoint", 101, "Related to the threshold at which a feature is engaged; typically related to load control"),
    SIGMATIC_MESSAGE("SigmaticMessage", 102, "Related to sigmatic messages"),
    SIGNATURE("Signature", 103, "Related to electronic security and signing of messages"),
    SIGNATURE_LENGTH("SignatureLength", 104, "Related to the length of a security signature"),
    SIGNATURE_TIMESTAMP("SignatureTimestamp", 105, "Related to the timeframe within which a security signature is valid"),
    SIGNATURE_USAGE("SignatureUsage", 106, "Related to how a signature is being used"),
    SPECIFIC_GRAVITY("SpecificGravity", 240, "Related to the ratio of the density of a substance to the density of water"),
    STANDARD_TIME("StandardTime", 107, "Related to the opposite of daylight savings time"),
    STANDBY_MODE("StandbyMode", 108, "Related to a specific mode of operation into which a device can be set"),
    STATUS("Status", 17, "Related to the current state of something"),
    STORAGE("Storage", 109, "Related to the medium on which information is kept; also related to the act of storing information"),
    SUPPLY_CAPACITY_LIMIT("SupplyCapacityLimit", 139, "Related to supply capacity limits"),
    TABLE("Table", 110, "Relating to a structure containing rows and columns"),
    TARIFF("Tariff", 140, "Billing term relating to cost or amount chaged"),
    TEST("Test", 111, "Related to a classification that specifies non-production"),
    TEST_MODE("TestMode", 19, "Related to a specific mode of operation into which a device can be set"),
    TEXT_MESSAGE("TextMessage", 112, "Related to a message or set of characters that are sent to a device"),
    THRESHOLD("Threshold", 261, "Related to a level or point at which something will happen"),
    TIER("Tier", 113, "Related to a level"),
    TIME("Time", 114, "Related to time of day, as in hours:minutes:seconds:miliseconds"),
    TIMEOUT("Timeout", 125, "Related to a specific threshold specifying when to automatically return after having received no response"),
    TIME_RESET("TimeReset", 115, "Related to the resetting of the time of day"),
    TIME_SYNC("TimeSync", 116, "Related to the process of adjusting the time of day value on a device to match that of a trusted source for time of day"),
    TIME_VARIANCE("TimeVariance", 117, "Related to the acceptable difference of a device time of day as compared to a trusted source for time of day"),
    TIME_ZONE("TimeZone", 118, "Related to the time regions around the Earth defined by the lines of longitude"),
    TOU("TOU", 121, "Related to time of use"),
    TRANCEIVER("Tranceiver", 122, "Related to a device that has both a transmitter and a receiver"),
    USAGE("Usage", 123, "Related to how something is used"),
    VERSION("Version", 124, "Related to a specific iteration or translation"),
    VOLTAGE("Voltage", 38, "Related to the electrical force that would drive an electric current between two points"),
    WINDOW("Window", 73, "Related to a period of time during which a device can be linked/paired with a meter or other device"),
    WRITE_ACCESS("WriteAccess", 282, "Related to the permission level one has; as in read, write, update");

    private final String mnemonic;
    private final int value;
    private final String description;

    EndDeviceSubDomain(String mnemonic, int value, String description) {
        this.mnemonic = mnemonic;
        this.value = value;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public int getValue() {
        return value;
    }

    public boolean isApplicable() {
        return NA != this;
    }

    public static EndDeviceSubDomain get(int value) {
        for (EndDeviceSubDomain endDeviceSubDomain : EndDeviceSubDomain.values()) {
            if (endDeviceSubDomain.getValue() == value) {
                return endDeviceSubDomain;
            }
        }
        throw new IllegalEnumValueException(EndDeviceSubDomain.class, value);
    }

}