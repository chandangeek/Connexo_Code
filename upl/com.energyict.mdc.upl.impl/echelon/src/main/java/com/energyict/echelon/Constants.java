package com.energyict.echelon;

/**
 * Hard coded constants for using the webservices as provided by echelon.
 * 
 * @author fbo
 */

public final class Constants {
	public final class LogType {
		public static final String ERROR = "f27e59fb29f140c2a62b0167e016bac1";
		public static final String USERACTIVITY = "396d29884c8449b0bca4e54c3c31d177";
		public static final String DEBUG = "04866276652c4ee8a5573195f0d34939";
		public static final String SQL_TRACE = "c1e81dbe173742b5b230420bf85f1f24";
		public static final String TCPIP_TRACE = "2db39ef3186146b7a4d45df5025e5bfc";
		public static final String DC1000_ADAPTER_ENHANCED_DEBUG = "6d57a96ffc1f4fd3b3bcb5985af09128";
		public static final String INTERNAL_TESTING = "28f3422ebfbc417bb75a2214368cad9d";
		public static final String PERFORMANCE = "f3a7374299fb4cdcad829bde99781499";
	}

	public final class DatabaseTypeID {
		public static final String CORE = "dd5141bd0f754256b6a51c1eb934841b";
		public static final String DISTRIBUTION = "b596af7a0c4d455fa1e77294557dc561";
	}

	public final class DataTypes {
		public static final String NUMERIC = "3616f4c224084b958047f8ac8a4ab0bc";
		public static final String STRING = "309c5d9a62454707b6bd74ed973583fd";
	}

	public final class DataPointValueStatus {
		public static final String COMPLETE = "86FF3E549A5C4cecAFADC739246686DA";
		public static final String FAILURE = "4E312B96EDA447f29F74E1FC1F9F06DA";
	}

	public final class DataPointStatus {
		public static final String ENABLED = "65610EB0B08F41399F2D5BC236DA23A3";
		public static final String DISABLED = "5F3F591C8667467b967CF68CAC6252C4";
		public static final String STOPPED = "168C025616D94db7A6D63257BF67F02C";
	}

	public final class Categories {
		public static final String DATAPOINTVALUE_STATUS_TYPE = "DataPointValues.StatusType";
		public static final String DATAPOINT_STATUS_TYPE = "DataPoints.StatusType";
		public static final String DATAPOINT_CALCULATION_TYPE = "DataPoints.CalculationType";
		public static final String DATAPOINT_SOURCE_TYPE = "DataPoints.SourceType";
		public static final String TASK_STATUS_TYPE = "Tasks.StatusType";
		public static final String TASK_TASK_TYPE = "Tasks.TaskType";
		public static final String ATTRIBUTE_TYPE = "Attributes.Type";
		public static final String MESSAGE_LOG_STATUS_TYPE = "MessageLog.StatusType";
		public static final String EXTERNAL_RETURN_CODES = "ExternalServiceReturnCodes";
		public static final String DATA_TYPES = "DataTypes";
		public static final String SCHEDULE_RECURRENCE_TYPE = "Schedules.RecurrenceType";
		public static final String SCHEDULE_STATUS_TYPE = "Schedules.StatusType";
		public static final String SCHEDULEDTASKSPENDING_STATUS_TYPE = "ScheduledTasksPending.StatusType";
		public static final String DATABASE_TYPE = "DataManager.DatabaseTypeID";
		public static final String DATALOG_RETRIEVAL_METHOD = "GatewaysILon100.DataLogRetrievalMethodType";
		public static final String GATEWAY_TYPE = "Gateways.Types";
		public static final String GATEWAY_STATUS = "Gateways.StatusTypeID";
		public static final String SETTING_VALUE_TYPES = "SolutionSetting.ValueTypes";
		public static final String SCHEDULE_TIMEOUTINTERVAL_TYPE = "Schedules.TimeoutIntervalType";
		public static final String EXPRESSION_TYPE = "DataPoints.ExpressionType";
		public static final String RESTRICTION_TYPE = "DataPointManager.RestrictionType";
		public static final String HIERARCHY_RESTRICTION_TYPE = "Hierarchy.RetrieveRestrictions";
		public static final String HIERARCHY_LEVEL_MEMBER_DELETE_TYPE = "HierarchyLevelMember.DeleteTypes";
		public static final String GATEWAY_COMMUNICATION_TYPE = "Gateways.CommunicationTypes";
		public static final String GATEWAY_TEMPLATE_TYPE = "Gateways.TemplateTypes";
		public static final String ID_TYPE = "IDTypes";
		public static final String DEVICE_STATUS = "Devices.StatusTypes";
		public static final String DEVICE_TYPE = "Devices.Types";
		public static final String ENTITY_TYPE = "EntityTypes";
		public static final String TASK_PROCESSOR_TYPES = "TaskProcessorTypes";
		public static final String COMMAND_HISTORY_STATUS_TYPE = "CommandHistory.StatusTypes";
		public static final String METER_LONTALK_KEY_STATUS = "Meters.AuthenticationStatusTypes";
		public static final String GATEWAY_COMMUNICATION_REQUEST_TYPE = "GatewayCommunicationHistory.RequestTypeID";
		public static final String GATEWAY_COMMUNICATION_STATUS_TYPE = "GatewayCommunicationHistory.StatusTypeID";
		public static final String DEVICE_SERVICE_STATUS_TYPE = "Devices.ServiceStatusTypes";
		public static final String PHASE_TYPES = "PhaseTypes";
		public static final String RESULT_TYPES = "Results.Types";
		public static final String EVENT_DEFINITION_STATUS_TYPES = "EventDefinition.StatusType";
		public static final String EVENT_DEFINITION_DELIVERY_TYPE = "Event.DeliveryType";
		public static final String FIRMWARE_TYPES = "Firmware.Types";
		public static final String CHANGE_TYPE = "PendingChanges.ChangeTypeID";
		public static final String EXPIRED_INTERVAL_TYPE = "ArchiveSetting.ExpiredIntervalType";
		public static final String SCHEDULE_ASSIGNMENT_STATUS_TYPE = "ScheduleAssignment.StatusType";
		public static final String SCHEDULE_TYPE = "Schedules.Types";
		public static final String SETTING_TYPE = "SettingTypes";
		public static final String DATA_POINT_VALUE_SORT_TYPES = "DataPointValue.SortTypes";
		public static final String SORT_ORDER_TYPES = "SortOrderTypes";
		public static final String INFORMATION_RETURN_TYPES = "InformationReturnTypes";
		public static final String METER_DISPLAY_CATEGORY_TYPES = "MeterDisplayCategoryTypes";
		public static final String PROPERTY_DATA_TYPES = "Properties.DataTypes";
		public static final String TOUCALENDARTYPES = "TOUCalendarTypes";
		public static final String CONTROL_RELAY_STATUS = "SecondaryControlOutputRelayStatus";
		public static final String MAXIMUM_POWER_LEVEL_ENABLE_TYPES = "MaximumPowerEnableTypes";
		public static final String LAST_GATEWAY_TO_DEVICE_COMMUNICATION_STATUS_TYPES = "GatewayToDeviceCommunicationStatusTypes";
		public static final String REPEATER_PATH_STATUS = "RepeaterPathStatus";
		public static final String ADD_METER_FAILURE_TYPES = "AddMeterFailureTypes";
		public static final String DEVICE_DISCOVERED_TYPES = "DiscoveredTypes";
		public static final String BILLING_DATA_STRUCTURE_TYPES = "BillingDataStructureTypes";
		public static final String IP_ADDRESS_TYPE = "IPAddressTypes";
		public static final String APPLICATION_LEVEL_AUTHENTICATION_TYPES = "ApplicationLevelAuthenticationStatusTypes";
		public static final String GATEWAY_ENCRYPTION_STATUS_TYPES = "Gateway.EncryptionStatusTypes";
		public static final String GATEWAY_WAN_CONFIGURATION_STATUS_TYPES = "Gateway.WANConfigurationStatusTypes";
		public static final String GATEWAY_OUTBOUND_CHAP_STATUS_TYPES = "Gateway.OutboundCHAPStatusTypes";
		public static final String GATEWAY_INBOUND_CHAP_STATUS_TYPES = "Gateway.InboundCHAPStatusTypes";
		public static final String GATEWAY_PAP_STATUS_TYPES = "Gateway.PAPStatusTypes";
		public static final String GATEWAY_PORT_SPEED_TYPES = "Gateway.PortSpeedTypes";
		public static final String GATEWAY_MODEM_COMMAND_TYPES = "Gateway.ModemCommandTypes";
		public static final String GATEWAY_MODEM_CONNECT_TYPES = "Gateway.ModemConnectTypes";
		public static final String GATEWAY_PPP_CONNECTION_TYPES = "Gateway.PPPConnectionTypes";
		public static final String GATEWAY_MODEM_ANSWER_TYPES = "Gateway.ModemAnswerTypes";
		public static final String GATEWAY_AUTHENTICATION_TYPES = "Gateway.AuthenticationTypes";
		public static final String PING_GATEWAY_STATUS_TYPE = "PingGatewayStatusTypes";
		public static final String MBUS_STATUS_TYPES = "MBusStatusTypes";
		public static final String RESERVED_IDS = "ReservedIDs";
		public static final String DEVICE_MBUS_AUTO_DISCOVERY_STATUS_TYPE = "Device.MBusAutoDiscoveryStatusTypes";
		public static final String CONNECTION_FAILURE_TYPES = "ConnectionFailureTypes";
		public static final String PERFORMANCE_LOG_TYPES = "PerformanceLogTypes";
		public static final String APPLICATION_LEVEL_AUTHENTICATION_STATUS_TYPES = "ApplicationLevelAuthenticationStatusTypes";
		public static final String COMMAND_FAILURE_TYPES = "Command.FailureTypes";
		public static final String COMMUNICATION_FAILURE_STATUS_TYPES = "CommunicationFailureStatusTypes";
		public static final String DATA_ORDER_TYPES = "DataOrderTypes";
		public static final String DC1000_DEVICE_ALARMS = "DC1000DeviceAlarms";
		public static final String DC1000_DEVICE_KNACK_CODES = "DC1000DeviceNackCodes";
		public static final String DC1000_HARDWARE_DIAGNOSTIC_CODES = "DC1000HardwareDiagnosticCodes";
		public static final String DC1000_NACK_CAUSE_CODES = "DC1000NackCauseCodes";
		public static final String DC1000_REBOOT_CAUSES = "DC1000RebootCauses";
		public static final String DC1000_RESOURCE_ENTRY_PRIORITIES = "DC1000ResourceEntryPriorities";
		public static final String DC1000_RESOURCES = "DC1000Resources";
		public static final String DC1000_RSA_KEY_STATES = "DC1000RsaKeyStates";
		public static final String DC1000_SECURITY_EXCEPTIONS = "DC1000SecurityExceptions";
		public static final String DC1000_SOFTWARE_DIAGNOSTIC_CODES = "DC1000SoftwareDiagnosticCodes";
		public static final String DC1000_EVENT_TYPES = "Event.DC1000EventTypes";
		public static final String EVENT_DELIVERY_STATUS_TYPES = "Event.DeliveryStatusType";
		public static final String EVENT_SECURITY_ALERT_TYPES = "Events.SecurityAlertTypes";
		public static final String EVENT_STATE_TYPE = "EventStateType";
		public static final String GATEWAY_DATA_AVAILABLE_TYPES = "Gateway.DataAvailableTypes";
		public static final String GATEWAY_INITIAL_COMMUNICATION_STATUS_TYPES = "Gateways.InitialCommunicationStatusType";
		public static final String GRAMMAR_STATUS = "Grammar.Status";
		public static final String KEY_AVAILABILITY_TYPES = "KeyAvailabilityTypes";
		public static final String MBUS_SECURITY_STATUS_TYPES = "MBus.SecurityStatusTypes";
		public static final String MBUS_AUTHENTICATION_TYPES = "MBusAuthenticationTypes";
		public static final String MBUS_BILLING_SCHEDULE_FREQUENCY_TYPES = "MBusBillingScheduleFrequencyTypes";
		public static final String METER_DETECTED_MBUS_ALARMS = "MeterDetectedMBusAlarms";
		public static final String REPEATER_TYPES = "RepeaterTypes";
		public static final String SERVER_TO_GATEWAY_PROTOCOL_TYPES = "ServerToGatewayProtocolTypes";
		public static final String STANDARD_API_OPTIONS = "StandardAPIOptions";
		public static final String TIMEZONE_DST_TYPES = "Timezones.DstTypes";
		public static final String PREPAY_ADD_CREDIT_OPTION_TYPES = "PrepayAddCreditOptionTypes";
		public static final String PREPAY_CLEAR_CREDIT_OPTION_TYPES = "PrepayClearCreditOptionTypes";
		public static final String PREPAY_STATUS_TYPES = "PrepayStatusTypes";
		public static final String PREPAY_EMERGENCY_CREDIT_STATUS_TYPES = "PrepayEmergencyCreditStatusTypes";
		public static final String PREPAY_REVERSE_POWER_DEDUCTION_STATUS_TYPES = "PrepayReversePowerDeductionStatusTypes";
		public static final String PREPAY_MAXIMUM_POWER_STATUS_TYPE_ID = "PrepayMaximumPowerStatusTypeID";
		public static final String PREPAY_AUDIBLE_ALARM_STATUS_TYPES = "PrepayAudibleAlarmStatusTypes";
		public static final String PREPAY_CREDIT_TYPES = "PrepayCreditTypes";
		public static final String ENGINE_IP_ADDRESS_ASSIGNMENT_TYPE_ID = "Engine.IPAddressAssignmentTypes";
		public static final String DEVICE_TEST_POINT_STATUS_TYPES = "Device.TestPointStatusTypes";
		public static final String GATEWAY_MESH_DETECTION_STATUS_TYPES = "Gateway.MeshDetectionStatusTypes";
		public static final String MAXIMUM_POWER_TYPES = "Device.MaximumPowerTypes";
		public static final String READ_TOU_CALENDAR_OPTION_TYPES = "Device.ReadTOUCalendarOptionTypes";
		public static final String CONNECT_REQUEST_SOURCE_TYPES = "ConnectRequestSourceTypes";
		public static final String TEST_TCPIP_PORT_STATUS_TYPES = "WanConfigurationSetting.TestTcpIpPortStatusTypes";
		public static final String PASSIVE_FTP_IP_ADDRESS_SOURCE_TYPES = "WanConfigurationSetting.PassiveFtpIpAddressSourceTypes";
		public static final String RETRIEVE_BY_PARAMETER_ID_TYPE = "IDTypes";
		public static final String SECONDARY_CONTROL_OUTPUT_RELAY_STATUS = "SecondaryControlOutputRelayStatus";
		public static final String TIMEOUT_INTERVAL_TYPE = "Schedules.TimeoutIntervalType";
		public static final String PROCESSOR_TYPE = "TaskProcessorTypes";
		public static final String PROCESSOR_PROCESSING_TYPE = "TaskProcessor.ProcessingType";
		public static final String SCHEDULE_PROCESS_TYPE = "Schedules.ProcessType";
		public static final String MESSAGE_LOG_LOG_TYPE = "MessageLog.LogType";
		public static final String SETTING_RETURN_STATUSES = "SolutionSetting.ReturnStatuses";
		public static final String DEVICE_COMMANDS = "Device.CommandTypes";
		public static final String GATEWAY_COMMANDS = "Gateway.CommandTypes";
		public static final String INTERNAL_COMMANDS = "InternalCommandTypes";
		public static final String METER_AUTHENTICATION_STATUS = "Meters.AuthenticationStatusTypes";
	}

	public final class DatabaseServerType {
		public static final String SQL_SERVER = "e8c8d576339849fda9637181e51efaeb";
	}

	public final class TaskProcessorTypes {
		public static final String RECALC = "485939F521C64e029CA139B733C26636";
		public static final String AGGREGATION = "3237604F96A24fbf974D05C3749BE098";
		public static final String ILON_100_ADAPTER = "D088035E1BF54D109CFB9A7B4BA96DE2";
		public static final String ARCHIVE = "6bbe851a0a15451fba5e189d8a8ac06c";
		public static final String SCHEDULE_CONTROLLER = "c1c921f6cc40461d8f431ee4e02b932e";
		public static final String SCHEDULE_PROCESSOR = "5C259D292C5C4651BC63BD17C72C0DAE";
		public static final String GLOBAL_TASK_MANAGER = "F27EC3EDCC174e66B2C21C45FC49FB7E";
		public static final String LOCAL_TASK_MANAGER = "7175289DB7C24f84A0A13D08A64C9F6E";
		public static final String TASK_TIMEOUT = "3136f5dbd4a949aba98bbf2e9a3aa6e4";
		public static final String DC1000_ADAPTER = "2ec6f270e9bf495da852a3b535ec41a8";
		public static final String EVENT = "ee4f5401daad4746acfeff144bf9102f";
		public static final String RAS_ADAPTER = "8be59f6536584f709b0625198905a906";
		public static final String CISCO_ACCESS_SERVER_AUTHENTICATION_ENGINE = "A8E15AB4EA23485cB7BBADDCDCBFC67F";
		public static final String CISCO_ACCESS_SERVER_ADAPTER = "6a85fb5e63f045c7b71cc7ede96f0559";
		public static final String ALWAYS_ON_IP_ADAPTER = "98fc7821d7144d02a04b5e1be0c8f0c8";
	}

	public final class TaskStatusTypes {
		public static final String WAITING = "3BC28FD1B008402b808C3FAD58D1EF02";
		public static final String IN_PROGRESS = "2850E772024F418295571C71F9E8F93C";
		public static final String SLEEPING = "094B25FA426A41dc82968D48F6F87127";
		public static final String ERROR = "EEA3A69129524261B2873B7E77736B5D";
	}

	public final class AttributeTypes {
		public static final String DEFINED = "40074C80216D4effB0347EBBA6A09A90";
		public static final String STRING = "E884BDCCF0374a1380D6543FEFD6D85C";
		public static final String NUMERIC = "9479C1CCAA9A40ebA1167981A315EAA5";
	}

	public final class ExternalServiceReturnCodes {
		public static final String SUCCEEDED = "A92A7EBEE897499fA8B06D5FE94B8A30";
		public static final String FAILURE = "5720769475E544c0A24A0C34CFF35A54";
		public static final String INVALID_XML_PARAMETERS_FORMAT = "8a27cb8afd464686a3893f4f8db26540";
		public static final String INVALID_PARAMETERS_NODE_MISSING = "C74C9E88E7904920B8164E15D363115C";
		public static final String INVALID_DEVICE_ID = "404CD23E49F245e8BDCB7009A8D4F912";
		public static final String INVALID_TYPE_OF_ENTITY_TYPE_ID = "ac633307cde34118b009cdffaf220634";
		public static final String INVALID_DEVICE_ID_ASSOCIATION_INVALID = "030061a6e1154989affb016cde162f89";
		public static final String INVALID_DEVICE_DATAPOINT_ASSIGNMENT = "61c29309a4b442b4afdbdd67f21473e4";
		public static final String INVALID_DEVICE_ID_ASSIGNMENT = "030061a6e1154989affb016cde162f89";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_ID_NODE_MISSING = "A64B4C43AD6348a9891AE1294D58743B";
		public static final String INVALID_NAME_NODE_MISSING = "D80245C7516141a182FA563E103DD45C";
		public static final String HIERARCHY_LEVEL_MEMBER_ID_BLANK = "CAA4811A74724d3c98476AC18671A8F2";
		public static final String INVALID_DEVICE_NAME_BLANK = "BA079E1BDDD347b2BA7091896A81F1CC";
		public static final String DUPLICATE_DEVICE = "9bf9671d1e8440849a0fb2151ffa3087";
		public static final String DUPLICATE_DEVICE_NAME = "9bf9671d1e8440849a0fb2151ffa3087";
		public static final String INVALID_ATTRIBUTE_ID = "897961760A134a738395C10806C5A6C6";
		public static final String INVALID_ATTRIBUTE_VALUE = "20878FC91EC347288053ACE7AC85CBDF";
		public static final String INVALID_NUMERIC_ATTRIBUTE_VALUE = "079a5b64224545ce834b78e12fd96f68";
		public static final String INVALID_ATTRIBUTE_TYPE_ID = "2dc9a71395a74fa7895a4c941f3abd0d";
		public static final String INVALID_ATTRIBUTE_DEFINEDVALUE_ID = "3ac41fafd1aa4f9f86a822d1c1092e33";
		public static final String INVALID_ATTRIBUTE_NAME_DUPLICATE = "cebbadeadbce45cd8efe0383c94fbe6b";
		public static final String INVALID_ATTRIBUTE_DEFINEDVALUE_NAME_DUPLICATE = "d697841dcb9549b7874046a6f3c9c180";
		public static final String INVALID_ATTRIBUTE_NAME_BLANK = "55906f87e5a34fb9bebed0715a89b7bc";
		public static final String INVALID_ATTRIBUTE_DEFINEDVALUE_NAME_BLANK = "9be3f52fbfd94200bc06a8e91dafc16b";
		public static final String INVALID_ATTRIBUTE_ID_NOT_DEFINED = "fa1d9b6f08ba405fa48263f64259250b";
		public static final String INVALID_ATTRIBUTE_ID_NOT_STRING = "6c821e86e00c4a148e1986e288fe3b83";
		public static final String INVALID_ATTRIBUTE_ID_NOT_NUMERIC = "5a74dc403b8842a5aa800d6d03be66d1";
		public static final String DUPLICATE_HIERARCHY_ASSIGNMENT = "DEFB4EC0F91B4daf9F72C71122353556";
		public static final String INVALID_DATA_POINT_ID = "28050E0C73624def90918FDE03F11D8D";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_ID = "BB6CB61FBF2B486dB43F00410AD60A05";
		public static final String INVALID_VARIABLE_TYPES_XML = "40153C219C1D43f69B4994168A38DF38";
		public static final String INVALID_VARIABLE_TYPES_XML_EMPTY = "B08C2B32C9E142c089F0CC74960E937D";
		public static final String BLANK_VARIABLE_NAME = "40830F08C7DC4aafA76436228BD38D01";
		public static final String VARIABLE_TYPE_IN_USE = "3066126399EF40d2BF882D8CADA35178";
		public static final String INVALID_SECURITY_KEY = "1EE016C8AA2C4f709FFA73655CEFD9FB";
		public static final String INVALID_TYPE_CATEGORY = "1D2B8582C2C545f48963719914949A1D";
		public static final String INVALID_TYPE_ID = "ADC4E0656DA446b58F71F4F04A046905";
		public static final String INVALID_DATA_TYPE_ID = "E77B79D1D84A4f7996EF1A82679F42E9";
		public static final String INVALID_USER_AUTHENTICATION_TYPE_ID = "5C5B36E9EAAF4234988D9CB1B4D9B784";
		public static final String INVALID_NUMERIC_RANGE = "C3E8BBBE4FC7415aA4BD78A8D1197BF8";
		public static final String INVALID_NUMERIC_MIN = "4c5d09f9f4ef4346beaf23e731ed6ac6";
		public static final String INVALID_NUMERIC_MAX = "d318c7277ab841ee963df4a05a1d0cd6";
		public static final String INVALID_DELETE_DATA_POINTS_NODE_MISSING = "368B2F3EC6AD42deB2603EBB73B64F1B";
		public static final String DELETE_DATA_POINTS_NODE_MISSING = "368B2F3EC6AD42deB2603EBB73B64F1B";
		public static final String INVALID_DELETE_DATA_POINTS_ID = "DD548AFB4D71457982C0A544897D2233";
		public static final String DELETE_DATA_POINTS_INVALID = "DD548AFB4D71457982C0A544897D2233";
		public static final String PARTIAL_SUCCESS = "7651E04B049E4dddAFA408370D0E80D0";
		public static final String INVALID_HIERARCHY_LEVEL_ID = "e36cf2b913e94595bb183e734b6b6044";
		public static final String INVALID_HIERARCHY_ID = "d7c91ca786e34586a0a56e0fb074cf65";
		public static final String INVALID_HIERARCHY_NAME_DUPLICATE = "42f0450cf5a8432b9a1a07ab43f1e271";
		public static final String INVALID_HIERARCHY_LEVEL_NAME_DUPLICATE = "3fcccf0501804b6ea53d1db55b73661d";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_NAME_DUPLICATE = "d0bf846170664a3c9b36f03e87fbf329";
		public static final String INVALID_HIERARCHY_LEVEL_MULTIPLE_CHILDREN = "9afc0d0cd7184689ab9a87af6c4f01e7";
		public static final String INVALID_HIERARCHY_NAME_BLANK = "c4e2fd9a15ba48d9868bafb93495e6b9";
		public static final String INVALID_HIERARCHY_LEVEL_NAME_BLANK = "e16215e9575e47b3b7e8357203d14ffe";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_NAME_BLANK = "052ea988303f4514921ed7614d9c5c9c";
		public static final String INVALID_HIERARCHY_LEVEL_PARENT_ID = "dfae316306dc48c7ab572e25bc5ba25d";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_PARENT_ID = "2a8626be8fa9443db5dc06d503122bf9";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_PARENT_LEVEL = "8707055784ed4750a7608139567befd9";
		public static final String INVALID_HIERARCHY_RESTRICTION_TYPE_ID = "82e1045be1634e099a25870869223bb7";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_NAME_CHARACTERS = "7b739c7ec3bc49f7b15a6071a2accc59";
		public static final String INVALID_HIERARCHY_DELETION_TYPE_ID = "f34baa69f29143cca9e07a67d2bd9913";
		public static final String INVALID_HIERARCHY_LEVEL_NAME_CHARACTERS = "188beda317f2433fa10ae56bc0ca829c";
		public static final String INVALID_HIERARCHY_LEVEL_STRUCTURE = "23ebfa15e548474f907627b1def3aced";
		public static final String INVALID_RECURRENCE_TYPE_ID = "13f78ae7507d4e76b5c040ddeac989ef";
		public static final String INVALID_STATUS_TYPE_ID = "629C307080974772BB803A49CB7A2CBF";
		public static final String INVALID_SOURCE_TYPE_ID = "67DD454B0AB74bc49AB3F5EC5968EB8C";
		public static final String INVALID_CALCULATION_TYPE_ID = "DC945387F85F43df8911B35F2FDEF2B8";
		public static final String INVALID_VARIABLE_TYPE_ID = "A831975F83EC42b9BEAF8143FF768B93";
		public static final String INVALID_VARIABLE_TYPE_COMPONENT_ID = "dd630af14f8f4434bd2eca8147fc9777";
		public static final String INVALID_TIMEZONE_ID = "C277E2D657D2457a8FE07FFE1A28A7D9";
		public static final String INVALID_STARTDATE = "06c3a04fd0a649b9a5b4e9e10523315f";
		public static final String INVALID_DEVICE_ID_DEVICE_NOT_TIED_TO_GATEWAY_ID = "A8C8417A75A04223BC89110ACB260A4E";
		public static final String INVALID_DATAPOINT_MINMAX_RANGE = "093C0AC65ED74e7390D4DF152D487CF7";
		public static final String INVALID_ENDDATE = "52e68e4510bb44209482cdcdf29afe11";
		public static final String INVALID_PRIORITY = "aa71aac765f042fa984c80d7fd505295";
		public static final String INVALID_PRIORITY_NOT_SUPPORTED = "8f01896567e8421e973fd31ade64af1c";
		public static final String COMPONENT_NOT_FOUND = "628255DA087245a98575F949F7646BD3";
		public static final String INVALID_STOPPED_DATE = "8a09e6008380482ea8829dc5fa32c0ee";
		public static final String INVALID_DATAPOINT_RESTRICTION_TYPE_ID = "665f22df55d54c97a20590609672c11f";
		public static final String INVALID_DATAPOINTVALUE_STATUS_TYPE_ID = "eac58c72de6a49f59cf7c9ea8227e3a3";
		public static final String INVALID_DATAPOINT_VALUE = "46170a98b734415f9cd3e57b2f296d45";
		public static final String INVALID_GATEWAY_COMBINATION = "3601D0EF74BF4abb9C2B26521E830642";
		public static final String INVALID_DATAPOINT_SCHEDULE_ASSIGNMENT = "8a53448446e949e4a9d54ea257e1af2d";
		public static final String INVALID_DATAPOINT_SCHEDULE_DELETE = "426a668af29348c2aadad749f0eda7ec";
		public static final String INVALID_METER_SOFTWARE_VERSION = "d733335439e64ac8b4808a2b7e63cecb";
		public static final String INVALID_TRACKING_ID = "a323e3cb19754ee7a32ae82313fe4463";
		public static final String INVALID_TRACKING_ID_NODE_MISSING = "3f4e5309fc1d4cf59de87aca5354b992";
		public static final String INVALID_GATEWAY_ID = "94f7645740554fcfaca7dd9816545e6f";
		public static final String INVALID_GATEWAY_TYPE_ID = "49988335ee81476c87e66db8145f6841";
		public static final String INVALID_GATEWAY_TEMPLATE_PARAMETER_ID = "6c36061b2afb45ac99670de92195aef4";
		public static final String INVALID_GATEWAY_TEMPLATE_NAME = "750321b796794c339906ab8bec177817";
		public static final String DUPLICATE_GATEWAY_TEMPLATE_NAME = "8638fbd3afd549e6b14f70e1c33b317c";
		public static final String INVALID_GATEWAY_STATUS_TYPE_ID = "908e6efc342744e3bf48a36dce4b837b";
		public static final String INVALID_ILON100_DATA_LOG_RETRIEVAL_TYPE_ID = "bc3dae66b7f1478087713ceba6d6791c";
		public static final String INVALID_ILON100_DATA_LOG_USED_PARAMETER_ID = "35fb3d1275b84de0aa782225fe390943";
		public static final String INVALID_GATEWAY_NAME = "5e0d7db37d854c518c1afd195fad8ebd";
		public static final String INVALID_GATEWAY_VARIABLE_NAME = "2bd91ef3c612457ab54eb8e104165ba0";
		public static final String INVALID_GATEWAY_VARIABLE_TYPE_COMPONENT_ID = "42879ed14a674dd48bce28c897f12e95";
		public static final String INVALID_TAKE_HIERARCHIES_PARAMETER_ID = "980b61c3666447efbc941ac72769da96";
		public static final String INVALID_ATTRIBUTE_PARAMETER_VALUE_NODE_MISSING = "6ce8a02f4c5d40d5a213e7f6efbca018";
		public static final String INVALID_ATTRIBUTE_PARAMETER_ID_NODE_MISSING = "31b76abe7a264b14810adb18d0648d23";
		public static final String INVALID_ATTRIBUTE_VALUE_NODE_MISSING = "6ce8a02f4c5d40d5a213e7f6efbca018";
		public static final String INVALID_ATTRIBUTE_ID_NODE_MISSING = "31b76abe7a264b14810adb18d0648d23";
		public static final String INVALID_DATETIME = "8dd2304a495f4a46b1b875bc54299f64";
		public static final String INVALID_GATEWAY_TEMPLATE_ID = "07cad359cc1142e5a2b97f0e6bf7aee9";
		public static final String REGISTRATION_LIMIT_REACHED = "b3e649a5dfdb49ac9bbfa2c573192400";
		public static final String INVALID_GATEWAY_NAME_BLANK = "ff46053a6ea74f4392fdc2317ee83e8e";
		public static final String DEVICES_IN_USE = "6573c9ae3d2a4797807b9ad26910bd81";
		public static final String UNSUPPORTED_GATEWAY_TYPE_ID = "03cd80ebc0014e59a08fea9abae59695";
		public static final String INVALID_PPP_USERNAME = "2bd9054a5be8434785446ebbc6a10460";
		public static final String INVALID_PPP_USERNAME_BLANK = "7659b5b48e0d41159321ba012f3e33f6";
		public static final String INVALID_PPP_USERNAME_DUPLICATE = "ec945e116bb04a92a5435be263fac195";
		public static final String INVALID_PPP_PASSWORD = "9c3cd24fe69c4bc084a5e3650d2a8f41";
		public static final String INVALID_PPP_PASSWORD_BLANK = "4765fa6ea06944469ad2b1a23e5740a2";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_ID = "8ee098fc32d0443ba3d4770eeeed60a7";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_START_DATETIME = "66bb1eb1223545ed99ffc155a666ff02";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_END_DATETIME = "1e7f6ba5799849078d2f811c9a07ed7d";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_INITIATED_DATETIME_RANGE = "84d2eedd579345148033942d0057ac50";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_START_DATETIME = "b8307e406e764a59a7f376aeccf2aea0";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_END_DATETIME = "f6c5ce1c9e9148c1b2a3729f252a1b8e";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUESTED_DATETIME_RANGE = "1dbae26dc8cd4f9983177b130b6fb632";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_STATUS_TYPE_ID = "48148d9463784af283bb9db387024847";
		public static final String INVALID_COMMUNICATION_REQUEST_TYPE_ID = "9eac04040e4c4119acd50e91bdfa5181";
		public static final String INVALID_GATEWAY_COMMUNICATION_TYPE_ID = "5AE782E09FB44a67AE12F504AFED62A5";
		public static final String GATEWAY_INITIATED_COMMUNICATION_IN_PROGRESS = "04be1b61d83d41da80271eb04f48d759";
		public static final String INVALID_ACTIVATION_DATETIME = "398748e1cb9546a1876aa797569b8809";
		public static final String GATEWAY_NOT_ENABLED = "57f395ab1b2c47bbb149d73a604c1784";
		public static final String INVALID_IP_ADDRESS = "c7d9858b109d403f9dc64597555c3f57";
		public static final String INVALID_MODEM_TYPE = "76c4a668b589429087141cc7a4c865cc";
		public static final String INVALID_MODEM_INIT_STRING = "716012bd9a2b47099c0e61efd72b87bf";
		public static final String INVALID_SEARCH_CRITERIA = "36f5a11454d9406a85a952fc30a50cef";
		public static final String INVALID_ATTRIBUTE_NODE_MISSING = "8fe91c67cc6e48dab1b46fa06e70cc01";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_NODE_MISSING = "f9b4827652b346b9abe84d9e5f8d0518";
		public static final String INVALID_DC1000_IP_ADDRESS = "a0d9a331d5174af083eb4016b09f6a1f";
		public static final String INVALID_IP_ADDRESS_DUPLICATE = "1a54b6481ed84309b85dc664b265eb87";
		public static final String INVALID_IP_ADDRESS_OUT_OF_RANGE = "8ed705cba33c487ebc661f18732cf9ba";
		public static final String INVALID_GATEWAY_TO_SERVER_PHONE_NUMBER_1 = "4db0bb244d3143fd8fe3e24a320f2fed";
		public static final String INVALID_GATEWAY_TO_SERVER_PHONE_NUMBER_2 = "e1603cb62dc543ce811fd5eb6974f650";
		public static final String INVALID_ENABLE_TOTAL_ENERGY_VALUE = "643e8bb2baf74cd68d0d84c8cc13eebd";
		public static final String INVALID_NEW_WAN_CONFIGURATION_ROUTE_ID = "3e2b1120b77546f3b5586ac2464374f4";
		public static final String NO_GATEWAY_IP_ADDRESS_AVAILABLE = "b866d54e8f8d4aeba2bdbcdd622cadba";
		public static final String INVALID_IP_ADDRESS_IN_CONFLICTING_WAN_CONFIGURATION_ROUTE = "dddbb7439b0240b293b068c40d5f98fd";
		public static final String INVALID_APPLICATION_LEVEL_AUTHENTICATION_VALUE = "51f12cc5c21347948f8f01f72295bcc8";
		public static final String INVALID_SCHEDULE_NODE_MISSING = "f2fb555a43c54fae93c71b8dfdbb813a";
		public static final String INVALID_SCHEDULE_ID_NODE_MISSING = "1d6f7a9a0adc4496b7cd7d4e426579b9";
		public static final String INVALID_GATEWAY_DISCONNECT_COMMAND = "e241911a6f3d40a7a29be480e6d10ef6";
		public static final String INVALID_GATEWAY_DISCONNECT_IN_PROGRESS = "2caccf187b814d0fb93b0699389deb63";
		public static final String UNSUPPORTED_GATEWAY_COMMUNICATION_REQUEST_TYPE_ID = "2eccc753de4240cda1f511b65b3daeef";
		public static final String INVALID_GATEWAY_SOFTWARE_VERSION_NOT_SUPPORTED = "4de82e8205854a6bb064f2914042208e";
		public static final String INVALID_SECURITY_OPTIONS_NODE_MISSING = "c636f6b9b6f94966b3a960253c016848";
		public static final String INVALID_PPP_USERNAME_AND_PASSWORD_MISSING = "a00bf7180912475593f8dcb1fa78d1f6";
		public static final String INVALID_NEURON_ID_MISSING = "cec69c1ef6b34e22a986a42367b621ee";
		public static final String INVALID_PPP_USERNAME_NODE_MISSING = "bd4e3987ef92475e84248d5d58b2adcb";
		public static final String INVALID_PPP_PASSWORD_NODE_MISSING = "dbcae2ef87184e65b09cd31c9db098c0";
		public static final String GATEWAY_DISABLED = "EC327A569B0F4680AF59AF17ED01BAAD";
		public static final String INVALID_HIGH_PRIORITY_CONNECTION_REQUEST = "4E66B732B94E42d1825269AD5B091763";
		public static final String INVALID_STATUS_CHANGE = "0677FAC893734e26BB026F20CCC197B6";
		public static final String INVALID_UPDATE_GATEWAY_FIRMWARE_COMMAND = "62C11ECEDF76441a8CBA14B333BB1CF1";
		public static final String INVALID_CONTROL_RELAY_STATUS_TYPE_ID = "37381b497105406993fdaa657393ac13";
		public static final String INVALID_MAXIMUM_POWER_LEVEL_ENABLE_STATUS_ID = "d4038663555a4165ab0c93da4d4ba37c";
		public static final String INVALID_ENABLE_MAXIMUM_POWER_NODE_MISSING = "41ae2f65d9de46338f6e5518505bd02c";
		public static final String INITIAL_GATEWAY_COMMUNICATION_NOT_COMPLETE = "29DE5712AE354b8cB855DCFF84AD0B08";
		public static final String INVALID_LAST_GATEWAY_TO_DEVICE_COMMUNICATION_STATUS_TYPE_ID = "ac2afd7bc52b4817a0c165b0a97e3af4";
		public static final String MULTIPLE_GATEWAYS_WITH_SERIAL_NUMBER = "3464dc8a88f9429899119602d1d78672";
		public static final String INVALID_PRIORITY_NODE_MISSING = "39b9bde7b5b446e39b8ff770efb19722";
		public static final String UNSUPPORTED_GATEWAY_COMMUNICATION_TYPE_ID = "9d4928b8aacc44488328714977ca481c";
		public static final String INVALID_SERVER_ROUTABLE_IP_ADDRESS_NODE_MISSING = "6bb35fc1e6cb4d3487c26780be39a8c8";
		public static final String WAN_CONFIGURATION_ASSIGNMENT_LIMIT_REACHED = "a2e793927f824f86b92af3637c3179db";
		public static final String INVALID_PRIORITY_DUPLICATE = "75c795ad3f1f4f91a339ee62d05a14ad";
		public static final String INVALID_ASSIGNMENT_NAME_NODE_MISSING = "57e4a1a3e7b4490c8783cd3775b9bb51";
		public static final String INVALID_ASSIGNMENT_NAME = "d9b5367df22c4e0698b00720dd690d34";
		public static final String INVALID_ASSIGNMENT_NAME_DUPLICATE = "2b589c04e35e4f24afbd7a99fad242b2";
		public static final String INVALID_SERVER_ROUTABLE_IP_ADDRESS = "9cead96db1de40afaf1f93bc88ca9c03";
		public static final String INVALID_SERVER_ROUTABLE_IP_ADDRESS_DUPLICATE = "9485e37b95db497d899b7d0c17e83849";
		public static final String INVALID_DEFAULT_ASSIGNMENT_NAME_NOT_FOUND = "fe14d16c565648199e128f286ea53beb";
		public static final String INVALID_WAN_CONFIGURATION_ASSIGNMENT = "1262019dc5aa45e3a84088b76b630660";
		public static final String UNSUPPORTED_WAN_CONFIGURATION_COMMUNICATION_TYPE_ID = "2ddccbd83999407ab69c6a5992c8cabf";
		public static final String INVALID_IP_ADDRESS_NODE_MISSING = "62ae26893b5d4fbba9851558c71fb545";
		public static final String INVALID_DEFAULT_WAN_CONFIGURATION_NOT_FOUND = "2c3e5a2530f6454eb575a36bff4453a0";
		public static final String INVALID_WAN_CONFIGURATION_ASSIGNMENT_NODE_MISSING = "32012e51200542428605453791e1b4c9";
		public static final String INVALID_WAN_CONFIGURATION_ID_NODE_MISSING = "e56bd3c319bb4443b9660721f8abdc03";
		public static final String INVALID_NUMBER_OF_GATEWAY_WAN_CONFIGURATIONS = "C1F1422069534daaA56AC66642F0E1EF";
		public static final String INVALID_WAN_CONFIGURATION_NODE_MISSING = "C943D2DCD52744e29D7D21508A92C7C5";
		public static final String INVALID_CURRENT_WAN_NAME_NODE_MISSING = "6ABC17FB2ECE439dBABC667444D3ED7F";
		public static final String INVALID_CURRENT_WAN_NAME = "386C97F587C2437588A74C8DFA150E2A";
		public static final String INVALID_WAN_CONFIGURATION_NAME_NODE_MISSING = "50EF37AD28E949179AEB5038B67F144D";
		public static final String INVALID_ENCRYPTION_STATUS_TYPE_ID_NODE_MISSING = "11C2DF647BD04448BAA1E8CEC56BE610";
		public static final String INVALID_ENCRYPTION_STATUS_TYPE_ID = "c46190a5c8d4487f98fa8d097c5fda45";
		public static final String INVALID_APPLICATION_LEVEL_AUTHENTICATION_STATUS_TYPE_ID_NODE_MISSING = "F1B074957DEC49319D2B19055A6782D9";
		public static final String INVALID_APPLICATION_LEVEL_AUTHENTICATION_STATUS_TYPE_ID = "01E4541008F94b7fA0F523B15FD3C16E";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_STATUS_TYPE_ID_NODE_MISSING = "D7E19F3E71E34aeaB7572BECC8DCF5F1";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_STATUS_TYPE_ID = "67050E258831441cA2402B63C665125C";
		public static final String ENABLED_GATEWAY_WAN_CONFIGURATION_MISSING = "C2AD813B0E374f17BAEB56648A534ED3";
		public static final String INVALID_MODEM_INITIALIZATION_STRING_NODE_MISSING = "41D9065AF6BD40df8A42BA09BF344B6C";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_PRIORITY_NODE_MISSING = "4FA7D2852A884adbA944E84C258E521D";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_PRIORITY = "68EC23C37B644dd5B0DBDC89CAC3D7AA";
		public static final String DUPLICATE_GATEWAY_WAN_CONFIGURATION_PRIORITY = "7D36DFEB18604348A80B7D29A5386301";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_PHONE_NUMBER_NODE_MISSING = "A78576F4A0414b6d90EC83113D614823";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_PHONE_NUMBER = "8D5819255CBC4b71848BC1D337BE8747";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_IP_ADDRESS_NODE_MISSING = "89891A79DEF54f4eB3D3FB9823CE643C";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_IP_ADDRESS = "C470ECD68E1B4e37BB95655F72EE7CCA";
		public static final String INVALID_OUTBOUND_CHAP_STATUS_TYPE_ID_NODE_MISSING = "9B96AF763849430e89589F1A17E6565E";
		public static final String INVALID_OUTBOUND_CHAP_STATUS_TYPE_ID = "282A5E34EFCC45f6BD2C18BF6740EB34";
		public static final String INVALID_INBOUND_CHAP_STATUS_TYPE_ID_NODE_MISSING = "5AB7FD909D654ac58FFBFEF79837AC71";
		public static final String INVALID_INBOUND_CHAP_STATUS_TYPE_ID = "1639C493D5A84193B863F0E9A340F7CC";
		public static final String INVALID_PAP_STATUS_TYPE_ID_NODE_MISSING = "45EF1126DB0D4f8cBAC8B10EED738D46";
		public static final String INVALID_PAP_STATUS_TYPE_ID = "C9A2A094E7F34b40837543CFF0345727";
		public static final String INVALID_MODEM_MONITOR_INTERVAL_NODE_MISSING = "63635B0FCDFA4240A7B1FC036912809A";
		public static final String INVALID_MODEM_MONITOR_INTERVAL = "ECCF3020777D48ffB925792EC982A972";
		public static final String INVALID_CALL_RETRY_WAIT_NODE_MISSING = "1FF7193F1B3E42c3BF9EC6B66AF57C0E";
		public static final String INVALID_CALL_RETRY_WAIT = "F6F0E86B1E794da8A80F3AEC4122FCD0";
		public static final String INVALID_SWITCH_LIMIT_NODE_MISSING = "2577B40570C4444185DBF1CD7078D01C";
		public static final String INVALID_SWITCH_LIMIT = "60D8ED34E61E4bd486BAA45A503DDF72";
		public static final String INVALID_PING_NODE_MISSING = "488E9C9655C44a85ADB9849FB47DCE91";
		public static final String INVALID_PING_IP_ADDRESS_NODE_MISSING = "5B64A208856C48b38ABBF93A159B245F";
		public static final String INVALID_PING_IP_ADDRESS = "3721D37348904c648BD93BBEDA4C1FD5";
		public static final String INVALID_PING_INTERVAL_NODE_MISSING = "50871F2AB8DE440bAD8EDCCC1AEF1DF7";
		public static final String INVALID_PING_INTERVAL = "D810DA96725B4fe998E56A637BE31BE2";
		public static final String INVALID_MODEM_CONFIGURATION_NODE_MISSING = "2E1368F6E20349c998363F9FF1F05C2A";
		public static final String INVALID_PORT_SPEED_TYPE_ID_NODE_MISSING = "8D7695620BB746fa971171497BB02DDB";
		public static final String INVALID_PORT_SPEED_TYPE_ID = "1CCA6932852C4a908C4278DCDE7CC20C";
		public static final String INVALID_MODEM_COMMAND_TYPE_ID_NODE_MISSING = "24CAE1497C8E470f8680493924810ACA";
		public static final String INVALID_MODEM_COMMAND_TYPE_ID = "C90286A20BA74ca694B6B22E459D5E03";
		public static final String INVALID_MODEM_CONNECT_TYPE_ID_NODE_MISSING = "5BF79CF314814111B16BCA4ECFB0377D";
		public static final String INVALID_MODEM_CONNECT_TYPE_ID = "20E4401592614481BF93E54D4AD2B55B";
		public static final String INVALID_PPP_CONNECTION_TYPE_ID_NODE_MISSING = "94AA692E4806418485D23E9D6E7AE859";
		public static final String INVALID_PPP_CONNECTION_TYPE_ID = "FDBB67C6F7DA4c00A2758BFAE38730BF";
		public static final String INVALID_MODEM_ANSWER_TYPE_ID_NODE_MISSING = "70AA1E65C6A143c0B232767F977C5179";
		public static final String INVALID_MODEM_ANSWER_TYPE_ID = "F8F286063AE04b489042B66855570F05";
		public static final String INVALID_ISP_NODE_MISSING = "2EE9B56C6C4F4ee3AAF7538E479FFE50";
		public static final String INVALID_GATEWAY_AUTHENTICATION_TYPE_ID_NODE_MISSING = "3183577DAEC6453cAB9AE3EDB8066825";
		public static final String INVALID_GATEWAY_AUTHENTICATION_TYPE_ID = "BFE14A12B2264f22B3A1D950AACB09F2";
		public static final String INVALID_ISP_USERNAME_NODE_MISSING = "9FA85CD930344934B2EB4642D11719DB";
		public static final String INVALID_ISP_USERNAME = "7777AA7E43A04381944D1DBF45F71EBD";
		public static final String INVALID_ISP_PASSWORD_NODE_MISSING = "5B2691BB7E83422c94F7B4A113E9EF23";
		public static final String INVALID_ISP_PASSWORD = "08CC7699AB314dd3AE2ADFB1E45623E9";
		public static final String GATEWAY_DEVICE_LIMIT_KEY_NOT_FOUND = "2b6d870b580642da85104741a8c951b6";
		public static final String INVALID_NEW_WAN_CONFIGURATION_ROUTE_ID_NODE_MISSING = "a535cf7454c54953bab0abd6f139f78b";
		public static final String INVALID_FORCE_DISCONNECT = "9cec9b7b54ba4bf1a6249cd5637f700a";
		public static final String INVALID_FORCE_DISCONNECT_COMMAND = "9e7acbfa28c14dbfa7e1f4779421d53e";
		public static final String INVALID_NEURON_ID_NOT_FOUND = "fec693fb44c34aad985c3a5a3dfad2bd";
		public static final String INVALID_SERIAL_NUMBER_NOT_FOUND = "f46485aa64834a40a4ed264210dfa043";
		public static final String INVALID_LAST_CONNECTION_START_DATE_TIME = "318b646ff4e44d138fc08f6234401fd4";
		public static final String INVALID_LAST_CONNECTION_END_DATE_TIME = "a2e8f4fb965b40b49604a41c3a29e8ba";
		public static final String INVALID_LAST_CONNECTION_DATE_TIME_RANGE = "ca15df6251b14e9192d7adf474f9aaf3";
		public static final String INVALID_DELETE_CRITERIA = "15849de4ab864381a6d437fef2bd1f7b";
		public static final String INVALID_DATABASE_TYPE_ID = "10a7e3ca60cc475e913c61661587b7c8";
		public static final String INVALID_TRANSFORMER_ID = "65b5b9e110564bd8bfbbf233f13a9c46";
		public static final String INVALID_INSTALLATION_DATE_TIME = "4268ffa7fb9a4f5eaec366af10011893";
		public static final String INVALID_NEURON_ID = "ae7663061d8d4607aa8e39d02ab04f65";
		public static final String DUPLICATE_NEURON_ID = "27d1f8d25d01441b876b2ddfe9142fcb";
		public static final String DUPLICATE_TRANSFORMER_ID = "a93a286c4f6649c289c13d574674db4d";
		public static final String INVALID_TEMPLATE_TYPE_ID = "b7101e9a240342fd95efbb7e1e6ae636";
		public static final String RESULTS_PENDING = "4a5ac18e4f8a47d9bc1a1a98d810c1f1";
		public static final String CURRENTLY_COMMUNICATING = "15d2a24c0db341778580f71742f4f9be";
		public static final String INVALID_ID = "5745f6be0dd74d918ea07aea2d21d627";
		public static final String INVALID_ID_TYPE = "55da2d57a75f44f0a2e747fbea184d5a";
		public static final String INVALID_START_INSTALLATION_DATETIME = "736e9fd35361464b803e7ffc641879e7";
		public static final String INVALID_END_INSTALLATION_DATETIME = "941659468b0c4b36998b75092de8ed21";
		public static final String INVALID_INSTALLATION_DATETIME_RANGE = "aba65e949aaf4b23a436fe6bb1cffc87";
		public static final String INVALID_PHONE_NUMBER = "01b97727a7344a72b0cc1bf9781bff9c";
		public static final String INVALID_PHONE_NUMBER_BLANK = "b337587725294d08bd211a1823c4ca9e";
		public static final String INVALID_IP_ADDRESS_CONFLICTING_SPECIFICATION = "d2984a35c4654187915ae501e3b4a07a";
		public static final String INVALID_SOAP_NAME = "F81AC19F451446ae83F0415DB615EAAE";
		public static final String INVALID_FUNCTION_NAME = "F3E8A95668254b05BAE4825316C52312";
		public static final String INVALID_SOAP_URI = "6B559F99D72F47b28B6EF58C33AA6E90";
		public static final String INVALID_SOAP_NAMESPACE = "398A7BF6CD234e108357467A90A9FA30";
		public static final String INVALID_EXPRESSION = "3DB8906E1152470b9B1941471EB304B3";
		public static final String INVALID_DEFINITION_ARGUMENT = "BE55938B7421474b9FF1BB61693454FC";
		public static final String INVALID_SOAPCALL_NAME_BLANK = "39984D08A60848289BE40E4610469092";
		public static final String INVALID_SOAPCALL_PARAMETER_NAME_BLANK = "428E8440EFE0476eBACF26F46EE99376";
		public static final String INVALID_FUNCTIONCALL_NAME_BLANK = "5859F5C3FAC14476B1AC6571074986CA";
		public static final String INVALID_FUNCTIONCALL_PARAMETER_NAME_BLANK = "F3817C24ECFD44b09DC392C8181864C1";
		public static final String INVALID_FUNCTIONCALL_ID = "f47fda4310964a2dabb1740d329638f9";
		public static final String INVALID_SOAPCALL_ID = "e15cd59753f84d67a35502a725c7672c";
		public static final String INVALID_SOAPCALL_PARAMETER_NAME = "649FCE67CB6D4ada80389EBEC40B63CC";
		public static final String INVALID_FUNCTIONCALL_PARAMETER_NAME = "19C4534E8AB944f8AD50920EA13CAA19";
		public static final String DUPLICATE_SOAPCALL_PARAMETER_NAME = "DB59340182A845898879119B766E2DEA";
		public static final String DUPLICATE_FUNCTIONCALL_PARAMETER_NAME = "A4FB87D191444299A87CE661D2CACDDB";
		public static final String DUPLICATE_FUNCTIONCALL_NAME = "94D43BDFA4D84b359AD8E8DB2C8A6599";
		public static final String DUPLICATE_SOAPCALL_NAME = "7DFF7CBBFB9D4829965B33614FF2AEEB";
		public static final String INVALID_SOAPCALL_PARAMETER_INDEX = "0375F4B6B37B450c86B257E5937128E3";
		public static final String INVALID_SOAPCALL_PARAMETER_TYPE_ID = "27A48FD841F84f1cBBBCCA4858F872E0";
		public static final String INVALID_FUNCTIONCALL_PARAMETER_INDEX = "3A5910618CD645d1B0CB9F491E30CEB9";
		public static final String INVALID_FUNCTIONCALL_PARAMETER_TYPE_ID = "32F3B81ECAE94d57BD3B4E029B0D967A";
		public static final String INVALID_SOAPCALL_PARAMETER_ID = "0280B14D971944efA9D589DEDF9DE372";
		public static final String INVALID_FUNCTIONCALL_PARAMETER_ID = "2E76A5446A4A496dA9C66F7FFEA58D85";
		public static final String INVALID_TIMEOUTINTERVAL_TYPE_ID = "C63DF78E6462474fA8BC0744185ADE7D";
		public static final String INVALID_SCHEDULE_ID = "1c5a2235875543a99e1138b8e3ae62a3";
		public static final String INVALID_TIMEOUTINTERVAL = "8d02544ae15a4fa986f534646b63becc";
		public static final String INVALID_SCHEDULE_INTERVAL = "f0b4a01d133740af8ecd4803f2219a9a";
		public static final String INVALID_SCHEDULE_MINUTE_INTERVAL = "fb923b337a2d4a99bb58ccf69abc77c3";
		public static final String INVALID_TASK_PROCESSOR_TYPE_ID = "2e26ff75a60343f8aafdfbca569a3c74";
		public static final String INVALID_SCHEDULE_STATUS_TYPEID = "b8250ad0a936408db123fa2caff93cf8";
		public static final String INVALID_SCHEDULE_TYPE_ID = "dbfdea626cc14f1788e6be1fce0d172f";
		public static final String INVALID_SCHEDULE_ALREADY_ASSIGNED = "3b78c56a68004affb1df968e11b1f970";
		public static final String INVALID_TIMEOUT_INTERVAL_MINUTE = "f4eca5891ea24ab498b110df797f9ff6";
		public static final String INVALID_SCHEDULE_NAME_BLANK = "D6CBD1601F074c94B1B07B93BEF22F25";
		public static final String INVALID_SCHEDULE_NAME_DUPLICATE = "5f69557c94df4149a102789d18c3571a";
		public static final String INVALID_SCHEDULE_OCCURRENCE_LIST = "a72499b064de48bb9ea0bbab8bed7cd6";
		public static final String INVALID_SCHEDULE_TYPE_ID_NODE_MISSING = "3145d0946898479fa69cf02bf69d06db";
		public static final String UNSUPPORTED_SCHEDULE_TYPE_ID = "58b2755689e24451a8e74cc644ae2ca9";
		public static final String INVALID_TIMEOUT_INTERVAL_TYPE_ID_NODE_MISSING = "1a734fc9308a460bb6bacae560a3595e";
		public static final String INVALID_TIMEOUT_INTERVAL_NODE_MISSING = "0aca8a1f6c59444fb5b80de776ee637d";
		public static final String INVALID_SCHEDULE_REMOVAL = "059defcccb044438901e5d1e542988b5";
		public static final String INVALID_DATABASE_LOCATION_BLANK = "70daf229c6ae42d5928d7c9d844a2c8b";
		public static final String INVALID_DATABASE_NAME_BLANK = "5c0358efe5354b7cba6c42569d25ca26";
		public static final String INVALID_DATABASE_LOGIN_BLANK = "5460ac0344f54dde8bc741abb95b6c98";
		public static final String INVALID_DATABASE_LOGIN_WITHOUT_PASSWORD = "9462e5e3b3924cabb13fb91c6a454585";
		public static final String FAILED_SENDING_UPDATE_TO_GLOBAL_TASK_MANAGER = "a87674d13fb74af191e6eb51efa924f5";
		public static final String FAILED_DATABASE_CONNECTION = "0ef8823e9da84618b44f78038512b4dd";
		public static final String FAILED_GLOBAL_TASK_MANAGER_NOT_FOUND = "a82412d0aa694ef8977a448b6d54d7b4";
		public static final String INVALID_PORT = "6fd34f95edff4f4688942e3efd5b9b28";
		public static final String INVALID_DATABASE_TIMEOUT_SECONDS = "aaa17d31b17743e2967c6d938bed8997";
		public static final String INVALID_EMAIL_ADDRESS_FORMAT = "b7dfe11ab8ad4c6c9f5880f9573a45ba";
		public static final String INVALID_MESSAGELOGDEFINITIONID = "57ec4684a73b4993b4672b1e2d61e176";
		public static final String INVALID_MESSAGELOG_STATUSID = "45d8969daeed4d51b51b4f083b0df43b";
		public static final String INVALID_MESSAGELOG_TYPE_ID = "5079c159380d4a3fb1bd90b23721de27";
		public static final String INVALID_MESSAGELOG_USERID = "9bdd09cf9dcf48dd8f60488312c4efd0";
		public static final String INVALID_MESSAGELOG_TEXT = "ef40f62d68664328810560605a13c9b6";
		public static final String INVALID_MESSAGELOG_LOCATION = "cc0ed8e028b4438a966239fb4e167bbb";
		public static final String INVALID_LOGTYPE_ENABLE = "ff01eab9ab014bf59ce99f5d1cdcb4fe";
		public static final String INVALID_LOGTYPE_DISABLE = "e4ff2416af2941f99971416a9859d4bf";
		public static final String INVALID_EMAIL_ADDRESS_MUST_BE_BLANK = "855a9e43fee24c32a19b942ea813021c";
		public static final String INVALID_MESSAGELOG_START_DATETIME = "d8fa00a3421f463094f61a69826d9a41";
		public static final String INVALID_MESSAGELOG_END_DATETIME = "68fc3484e1b84dffa243e5cd1f550764";
		public static final String INVALID_MESSAGELOG_DATETIME_RANGE = "890eb5bf9bc840e089339166bf31705c";
		public static final String INVALID_MESSAGELOG_SOURCEIPADDRESS = "ea6c0b45c19f4caca0ecb5f45f19899a";
		public static final String INVALID_SMTP_SERVER_LOCATION = "974278d617f34cbaac677206ba84b994";
		public static final String INVALID_DATAPOINT_NAME_BLANK = "88fd875abad147808290329a2f56066c";
		public static final String INVALID_DATAPOINT_NUMERICMIN_VALUE = "941700369a2049a7a26cc3d5063084c7";
		public static final String INVALID_DATAPOINT_NUMERICMAX_VALUE = "c98327903a4d40b49217d756a63faa46";
		public static final String INVALID_MAX_COUNT = "59b5fc09247344b8a24b88270c50b567";
		public static final String INVALID_DATAPOINT_NAME = "7bc17205edd441a4badac565eddb0f16";
		public static final String INVALID_UNIT_OF_MEASURE = "2386fcf105404a7b826f22968635167f";
		public static final String INVALID_DATAPOINT_NODE_MISSING = "e63a69bd48cd4ba2ae021fdb60cbd708";
		public static final String DATAPOINT_NOT_ENABLED = "480f1ebba03d47079ee83c7b9ea43b32";
		public static final String INVALID_SORT_BY_TYPE_ID = "000fecad76a14382ba3648801bc02897";
		public static final String INVALID_SORT_BY_ORDER_TYPE_ID = "11f7cb72ce184593b8a9a7267e0830eb";
		public static final String INVALID_SORT_OPTION_NODE_MISSING = "368de52ee4f34032ae847b77f4a4a6a6";
		public static final String INVALID_SORT_BY_TYPE_ID_NODE_MISSING = "5ec581850b154a64a9fe2b832d44a1a3";
		public static final String INVALID_SORT_BY_ORDER_TYPE_ID_NODE_MISSING = "737050d8ddb449b5a1389acf0a1deddf";
		public static final String INVALID_DATAPOINTVALUE_ACTUALDATETIME = "b659e10386e44f4b8444c715497e517f";
		public static final String INVALID_DATAPOINTVALUE_ACTUALDATETIME_NODE_MISSING = "1a48be0085c64f26a0080e2b44bd5f03";
		public static final String INVALID_DATAPOINTVALUE_ACTUALTIMEZONEDATETIME = "556b5566c79c4b65adb2771ae613538c";
		public static final String INVALID_DATAPOINTVALUE_ACTUALTIMEZONEDATETIME_NODE_MISSING = "33adcb5f697d4d05919bb94105c56e91";
		public static final String INVALID_DATAPOINTVALUE_CALCULATIONEXECUTIONTIMESPAN = "1f8746da7daa4c97a5932d338f9c16ff";
		public static final String INVALID_DATAPOINTVALUE_DATAAVAILABLE = "c98dd11e6fc14a1faf8c041bb1aa7cab";
		public static final String INVALID_DATAPOINTVALUE_DATAAVAILABLE_NODE_MISSING = "ca508a793b304df0adcccd1273c0bec2";
		public static final String INVALID_DATAPOINT_ID_NODE_MISSING = "2622cf8e23cd48b6973f2acb25b38d18";
		public static final String INVALID_DATAPOINTVALUE_EXPECTEDDATETIME = "5c7198b37a284ad5b5f98d8b369d5bac";
		public static final String INVALID_DATAPOINTVALUE_EXPECTEDDATETIME_NODE_MISSING = "ecdf296fd2e545d4a836b83d2f80797b";
		public static final String INVALID_DATAPOINTVALUE_EXPECTEDTIMEZONEDATETIME = "a66aeb7e34164ce5a522e8d6aa63ee03";
		public static final String INVALID_DATAPOINTVALUE_EXPECTEDTIMEZONEDATETIME_NODE_MISSING = "846322e8d7844f179f439c3174638282";
		public static final String INVALID_DATAPOINTVALUE_MANUALLYENTERED_NODE_MISSING = "036726f6433941189b7d565a112803a0";
		public static final String INVALID_DATAPOINTVALUE_MANUALLYENTERED = "5451837566ba41d58b805924d8fc5a9d";
		public static final String INVALID_DATAPOINTVALUE_STATUSTYPEID_NODE_MISSING = "6c19110304da487ea3240ec93c203ef0";
		public static final String INVALID_DATAPOINTVALUE_VALUE_TYPE_MISMATCH = "dfa88fb98c014d6b877ed0e76a126017";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_RETRIEVAL_TYPE_ID = "8c5c4d532c594b75849e01e21c0afb21";
		public static final String INVALID_DEVICE_TYPE_ID = "b240c1d77a7640bb82541e03e4457df0";
		public static final String INVALID_DEVICE_NAME = "c6f37c20012b4b00b7a0ad938d6978e0";
		public static final String INVALID_LONTALK_KEY = "a87498cd5e1d481eadaa4f6f1c1e432e";
		public static final String INVALID_SERIAL_NUMBER = "66de160c38f64e3eab61e8d4ff2a9891";
		public static final String INVALID_PHASE_TYPE_ID = "b8f1f1c627184ed89a63b3c1f86c55e8";
		public static final String INVALID_RESULT_ID = "7f12d45fb10b4ad189207e808d27f1aa";
		public static final String INVALID_RESULT_TYPE_ID = "06bcb2437dc9498cb2a32f01e5f902b0";
		public static final String INVALID_START_DATETIME = "c7bde659dd4f4be9b02256e0f4f03cc2";
		public static final String INVALID_END_DATETIME = "c357faff7ae147759831444b65a7dc60";
		public static final String INVALID_DATETIME_RANGE = "05721023453b4f2c911b41114db77fce";
		public static final String INVALID_TIMEOUT_DATETIME = "aad4ba108fdf4c37bfe019a499c3a011";
		public static final String LOAD_PROFILE_IN_PROCESS = "0dc815344c964ae8ad5a421221a21fc7";
		public static final String UNSUPPORTED_DEVICE_TYPE_ID = "0532ada7a8284094bafda987c0e5c1bb";
		public static final String INVALID_MAXIMUM_POWER_LEVEL = "1ef237fba0a14f6797439b59b31a1cbb";
		public static final String INVALID_MAXIMUM_POWER_LEVEL_DURATION = "bebb9587f75048cfbf1fab1f081b810c";
		public static final String UNSUPPORTED_MAXIMUM_POWER_LEVEL_DURATION = "56CA0C57A44947099317F3CC531750E9";
		public static final String INVALID_ASSOCIATED_WITH_GATEWAY = "5b910e9594dd47d3b1d992d3ad41b127";
		public static final String DEVICE_NOT_ENABLED = "953b40534277401a8bd9c187401ab5c5";
		public static final String INVALID_ENABLE_MAXIMUM_POWER = "af16a9545a0640ddabd50aeb36ad4e26";
		public static final String ENABLE_MAXIMUM_POWER_IN_PROCESS = "78e18517b2484c8eab9b77abb3839875";
		public static final String PERFORM_SELF_BILLING_READ_IN_PROCESS = "49685f8d36b44a43a9f5da304e5079f2";
		public static final String INVALID_ENABLE_CONTROL_RELAY_TIERS_VALUE = "9fe600ec48e94bac9d29b0957155e9f9";
		public static final String INVALID_CONTROL_RELAY_TIER_VALUE = "2b1eb3f5528b4c24b073bda6c4ee57be";
		public static final String INVALID_LOAD_PROFILE_CHANNEL_SOURCE_ID = "ed98589734a04abda4fafc778dba1621";
		public static final String INVALID_LOAD_PROFILE_INTERVAL_PERIOD = "92f0bc4fcdab47a287a04ad07d7b9dd0";
		public static final String INVALID_DEVICE_ID_NODE_MISSING = "3829ead36e344e27b0f1bef2783a00d9";
		public static final String INVALID_DEVICE_NODE_MISSING = "7b9bc07aa29347a09e831f1f6185d1c7";
		public static final String INVALID_DEVICES_NODE_MISSING = "F2CFE341A9A64f309B9F85D028742DAF";
		public static final String INVALID_GATEWAY_ID_NODE_MISSING = "825fdf4bb2ab424185ad274ee89e229b";
		public static final String INVALID_GATEWAY_NODE_MISSING = "ef4d9f24e4e64a6eabe42b4377cf8318";
		public static final String INVALID_POWER_QUALITY_THRESHOLD_COMMAND = "4B1756C208BE49b4818ADE3FA20F3E4C";
		public static final String INVALID_POWER_QUALITY_THRESHOLD_VALUE_NODE_MISSING = "BA450BCC555C43bf84E9F750EDB9ABCF";
		public static final String POWER_QUALITY_THRESHOLD_VALUE_IN_SECONDS_NODE_MISSING = "E51F9252317043f69AB07C829DD3358D";
		public static final String INVALID_POWER_QUALITY_THRESHOLD = "7C15BBD1B01F47259F47469311F729D1";
		public static final String INVALID_PROGRAM_ID = "2285A867C8144545A503B78D028FC507";
		public static final String UPDATE_METER_FIRMWARE_IN_PROGRESS = "0707FF0AAEC5450d925490129AB46D78";
		public static final String INVALID_FORCE_DELETE = "456ddbc0a51847fa907e353e408aec0c";
		public static final String INVALID_ALARM_TYPE_NODE_MISSING = "962BD6F73A9D42b38C2A91ACE17F4A73";
		public static final String INVALID_SET_ALARM_DISPLAY_CONFIGURATION_COMMAND = "0525705B841244718736C5CDC331E831";
		public static final String INVALID_ALARM_INDEX = "3D579899383D4dcf863B52971A5EB92F";
		public static final String INVALID_ALARM_DISPLAY_OPTION = "23FEF76074644dfcA19E22040FB53D39";
		public static final String DUPLICATE_ALARM_INDEX = "4D346AFD201244aaA561818D9F062671";
		public static final String INVALID_CAUTION_ID_INDEX_NODE_MISSING = "273E1AA474284b8bA9EBB566F4206C4C";
		public static final String UNSUPPORTED_CAUTION_ID_INDEX = "0EF54EEAB97D479c8D75CDB1DE6D5BA3";
		public static final String DUPLICATE_CAUTION_ID_INDEX = "FF29A459AD884cae9939079ED8AB7303";
		public static final String INVALID_ERROR_ID_INDEX_NODE_MISSING = "BE4F5AEBCBDC4d10B8B02AE9D32C8B4F";
		public static final String UNSUPPORTED_ERROR_ID_INDEX = "E3DC44FA07BB4e4aB6C4AA3554A58A2D";
		public static final String DUPLICATE_ERROR_ID_INDEX = "6055E65723864cee95635184B0FE47EE";
		public static final String INVALID_ENABLE_ALL_SEGMENTS_LIT = "F7AB557435C748549051F9C4EA9587EF";
		public static final String INVALID_SECONDS_TO_DISPLAY = "4D7BFF77408A4c12BF1386C61137159D";
		public static final String INVALID_METER_DISPLAY_CATEGORY_TYPE_ID = "6F3F217AB8B44c27A69A956ADEFB551F";
		public static final String INVALID_METER_DISPLAY_CATEGORY_INDEX = "7872AEC47CD24e61B2FCB86DE6D36A12";
		public static final String INVALID_METER_DISPLAY_SOURCE_CODE_ID = "0AA222FFD1CB47f3BA82D4CAE37E3FAE";
		public static final String INVALID_METER_DISPLAY_ID_TEXT = "8B8AC43F1DDF4da8B18EB2727212A6C1";
		public static final String DUPLICATE_METER_DISPLAY_ID_TEXT = "A654EED3F11A465691CA574B67587704";
		public static final String INVALID_FIELDS_AFTER_DECIMAL_POINT = "3673CE5182FA4eb4A3410F8D2F9D126C";
		public static final String INVALID_FIELDS_BEFORE_DECIMAL_POINT = "CFB83168E9EA4e4f8AEED7147D3FCDB9";
		public static final String INVALID_DECIMAL_POINT_FIELDS = "41066AC114EC42ab8330B381CD24392A";
		public static final String INVALID_SUPPRESS_ZEROS = "B81595F5131B4971A836F519A6CD5E1B";
		public static final String INVALID_NUMBER_OF_METER_DISPLAY_ITEMS = "FA3D59817F9542228EFC039AB592288B";
		public static final String INVALID_SERIAL_NUMBER_NEURON_ID_MISMATCH = "c71aa21844774accbb190740c4f225ba";
		public static final String INVALID_UPDATE_METER_FIRMWARE_COMMAND = "CD42CD0ED62249728954C29200A793D5";
		public static final String INVALID_DISPLAY_ITEMS_NODE_MISSING = "D4230DB02A1B4f77886CE436BB1FFC88";
		public static final String INVALID_DISPLAY_ITEM_NODE_MISSING = "CC8DE323A05F4f5880F6F60A6527F772";
		public static final String INVALID_METER_DISPLAY_SOURCE_NODE_MISSING = "83EE9E132755461b9BBFE884A30557A6";
		public static final String INVALID_METER_DISPLAY_ID_NODE_MISSING = "3E1AD42F04A14aab855CD81B18FF8AD5";
		public static final String INVALID_METER_DISPLAY_VALUE_NODE_MISSING = "B06164EC472047289EE5D6A9C253C588";
		public static final String INVALID_METER_DISPLAY_CONFIGURATION_NODE_MISSING = "A0A6D9F41FE843ffB1006AD7638CCC03";
		public static final String INVALID_CHANNEL_INDEX_NODE_MISSING = "c8fff0fa8dd54c22bcecc8ef5e079215";
		public static final String INVALID_PULSE_INPUT_CONFIGURATION_NODE_MISSING = "ca3ffa44f2074176b069ed0180a2076f";
		public static final String INVALID_PULSE_INPUT_CONFIGURATION_CHANNEL_NODE_MISSING = "a78353a38f864ac08a826913481e363f";
		public static final String INVALID_PULSE_INPUT_CONFIGURATION_COMMAND = "c827e84fe5004b70916d9092f7c7289b";
		public static final String INVALID_CHANNEL_INDEX = "4b7b50f72635464bbe9a28de00bb62d8";
		public static final String INVALID_PULSE_INPUT_CHANNEL_STATUS = "22a98b9aee644a35acf52e1c1fe88e15";
		public static final String INVALID_PULSE_INPUT_IDLE_STATE = "a46d11fdc3f94d6c9e11aa26811b5a8d";
		public static final String INVALID_PULSE_INPUT_CHANNEL_STATUS_MISSING = "45d43636ae7e4f8b87cd5c123cd43aaf";
		public static final String INVALID_PULSE_INPUT_TAMPER_URGENT_ALARM_STATUS = "ef6657d6de6640f7939bbe7783c7fa91";
		public static final String INVALID_PULSE_INPUT_TAMPER_REGULAR_ALARM_STATUS = "79a2676e8fb14d7aa91e07b47fd02d1e";
		public static final String INVALID_CHANNEL_MINIMUM_PULSE_WIDTH = "022d69ccf478487f95fc9e08402ceec7";
		public static final String INVALID_REPEATER_COUNT = "3dfa1baa81eb494b980ac188571d143c";
		public static final String INVALID_DISCOVERED_TYPE_NODE_MISSING = "1956ddcc3c584556bf0fa738559547b1";
		public static final String INVALID_DISCOVERED_TYPE_ID_NODE_MISSING = "a4a6eb34fdf94167af8519c6e9f6157a";
		public static final String INVALID_DISCOVERED_TYPE_ID = "989a141fad424f249c2bcd13a8963812";
		public static final String INVALID_LAST_CONTACT_START_DATETIME = "bd30ec88d8264f6e914b4107651ef849";
		public static final String INVALID_LAST_CONTACT_END_DATETIME = "1fc6da61138b48f6a38b90665543a6a0";
		public static final String INVALID_LAST_CONTACT_DATETIME_RANGE = "57688eea0c12422c9e10dbc065a718ce";
		public static final String INVALID_INFORMATION_RETURN_TYPE_ID = "4829A5C12DE24345A93A173B7CE26D72";
		public static final String INVALID_SERVICE_STATUS_TYPE_ID = "6abd62163e6640edac24b3da9ddea886";
		public static final String INVALID_DOWN_LIMIT = "1ce3320257d241f8901361bfd57fe22c";
		public static final String INVALID_DOWN_LIMIT_NODE_MISSING = "7d5eccbec7574405bb8bd1ea4cdcada5";
		public static final String UNSUPPORTED_DEVICE_STATUS_TYPE_ID = "0AE1890A33724ab0A6909F4693B48C95";
		public static final String UNSUPPORTED_CHILD_DEVICE_STATUS_TYPE_ID = "EF1DE7EAADDD4622B72AA7D33628E1CB";
		public static final String UNSUPPORTED_GATEWAY_STATUS_TYPE_ID = "012C3F93AEE74f61B8048403E6B60432";
		public static final String INVALID_GATEWAY_NO_TRANSFORMER_ID = "3F0D004C8868403f9A57B503BB5BA4FF";
		public static final String INVALID_DEVICE_TYPE_NODE_MISSING = "99529019eda14a69bb3f9dd202cdec68";
		public static final String INVALID_DEVICE_TYPE_ID_NODE_MISSING = "ebfe9ff0384b406185b7420e3a1dd436";
		public static final String INVALID_MBUS_DEVICE_TYPE_NODE_MISSING = "468170276fa446c7a1bf9122f1c47246";
		public static final String INVALID_MBUS_DEVICE_TYPE_VALUE_NODE_MISSING = "3ccc9b9a39d84427a70f6459927f0f49";
		public static final String INVALID_MBUS_DEVICE_TYPE_VALUE = "a35c9075c9c04d04b46c567ab75c5f86";
		public static final String SERIAL_NUMBER_NOT_SUPPORTED = "1215a759be454245b188338731e50b72";
		public static final String MULTIPLE_DEVICES_WITH_SERIAL_NUMBER = "60efff85a4b2423e8355ced5e030a7ea";
		public static final String CHILD_DEVICES_EXIST = "3f5e017f1993493f84f3a5cb56e6b6d2";
		public static final String INVALID_ALARM_POLLING_RATE_NODE_MISSING = "0dbf4f4170444eabac5b386a837e59b5";
		public static final String INVALID_ALARM_POLLING_RATE = "13cbdbc7b5494604b3159e55eb6dc355";
		public static final String BILLING_SCHEDULE_NODE_MISSING = "48BADA8EF7134d3b89356E877D022BB8";
		public static final String FREQUENCY_TYPE_ID_NODE_MISSING = "38D2F059EE0A4492AD3269E4464023EB";
		public static final String INVALID_FREQUENCY_TYPE_ID = "1D3BFDB345FB4b75B6D1E7BE94757A56";
		public static final String DAY_NODE_MISSING = "0A1D98817FFF4ae4B6E34BF6E6EB4834";
		public static final String INVALID_DAY = "4B921E6EE3744ba891788BC3EFC4613E";
		public static final String HOUR_NODE_MISSING = "72C44D45D2EA40428259BD694E743349";
		public static final String INVALID_HOUR = "99742BE10C3D46018E590201CC1DC9EB";
		public static final String MINUTE_NODE_MISSING = "858CE1EE10554b3dB5520E2FC67C2EA8";
		public static final String INVALID_MINUTE = "5929E220F3D84994A37A3B4684D5A31D";
		public static final String DEVICE_NOT_ASSOCIATED_WITH_GATEWAY = "2EB3DC1DC69C4eb59897C8BD5A882D0D";
		public static final String PARENT_DEVICE_NOT_ENABLED = "26bbfbf4cbf24e32847c949e3e13c6b1";
		public static final String INVALID_LOAD_PROFILE_CHANNEL_SOURCE_ID_ORDER = "7ca7243a21c0455393a1cfc096fb7827";
		public static final String INVALID_LOAD_PROFILE_MBUS_BYTES_TO_READ_NODE_MISSING = "e18d838e98d541ada3cefa920d7f828d";
		public static final String INVALID_LOAD_PROFILE_MBUS_BYTES_TO_READ = "509a6b6aaf3a4373807c0867adc9c7f2";
		public static final String DUPLICATE_LOAD_PROFILE_CHANNEL_SOURCE_ID = "44d460a7b26e44f5924ae6f99a5d42c6";
		public static final String UNSUPPORTED_LOAD_PROFILE_MBUS_BYTES_TO_READ = "7d33cb8f9f6a4404be697d98d7e3218b";
		public static final String INVALID_PREPAY_NODE_MISSING = "ED13CBC5EEC54853A9B9939A3F8156BB";
		public static final String INVALID_ADD_CREDIT_OPTION_TYPES_NODE_MISSING = "70F8E9E7961F4b00A9EA720A9023D343";
		public static final String INVALID_ADD_CREDIT_OPTION_TYPE_NODE_MISSING = "76B45232FB15476eB8C296F12612CD5B";
		public static final String INVALID_ADD_CREDIT_OPTION_TYPE_ID_NODE_MISSING = "679B26AD9B564fb59C004A10177FAC3C";
		public static final String INVALID_ADD_CREDIT_OPTION_TYPE_ID = "90B9761A670D4f54ACB5419F73B1C1F7";
		public static final String INVALID_CREDIT_VALUE_NODE_MISSING = "D4BF3EE807964c629C4307054EE25F68";
		public static final String INVALID_CREDIT_VALUE = "265D0505FDAF41449A5A2A533F345E80";
		public static final String INVALID_CLEAR_CREDIT_OPTION_TYPE_ID_NODE_MISSING = "FE2852B48F9E4a248241CB9E16E8C28B";
		public static final String INVALID_CLEAR_CREDIT_OPTION_TYPE_ID = "109BF61F9A874092B7D320A7336B8B8B";
		public static final String INVALID_PREPAY_CONFIGURATION_NODE_MISSING = "998844EC86544968B900879DD70C903D";
		public static final String INVALID_PREPAY_STATUS_TYPE_ID = "DB2E3DB311E94cd2AEE28C1FC781A53E";
		public static final String INVALID_EMERGENCY_CREDIT_STATUS_TYPE_ID = "9CEED3091C8A4726BBBD3AD75E8E6091";
		public static final String INVALID_MAXIMUM_EMERGENCY_CREDIT = "DD6FD62BCE284fb0A9DF2A7F8EA6CC0C";
		public static final String INVALID_REVERSE_POWER_DEDUCTION_STATUS_TYPE_ID = "49C194B7C6DA4669B48703E1F1136428";
		public static final String INVALID_MAXIMUM_POWER_STATUS_TYPE_ID = "E1B8BC2925594e69BB8AC4A4A7844E49";
		public static final String INVALID_AUDIBLE_ALARM_STATUS_TYPE_ID = "E8AB336112F5454795DDA7C272FD0935";
		public static final String INVALID_LOW_CREDIT_ALARM_LEVEL = "953AE6EED99D43c99825E3B1C4DD0B23";
		public static final String INVALID_TIER_NODE_MISSING = "37EF9F0582C64de3A34C1F02568959D8";
		public static final String INVALID_TIER_COUNT = "9E6065731DD94dcfAF088A5AF7750CF0";
		public static final String INVALID_TIER_INDEX_NODE_MISSING = "36B0DDDAD83242a4A62710D939F08463";
		public static final String INVALID_TIER_INDEX = "4EEEDBA606C948ffB73DB7547E1C67BE";
		public static final String DUPLICATE_TIER_INDEX = "A4DCBE6BF7A54caeB5AD1983F5F1FFB5";
		public static final String INVALID_TIER_RATE_NODE_MISSING = "BF42229EE6D94293BCD63A0431A934F2";
		public static final String INVALID_TIER_RATE = "61888BE8D39640579EE7B414F3F7F561";
		public static final String INVALID_SET_PREPAY_CONFIGURATION_COMMAND = "3FFE9A735F8C49a584CD47B7B8879DA2";
		public static final String INVALID_CALENDAR_ID_NODE_MISSING = "B349AAABA85B4C98B1E3FAD02038D901";
		public static final String INVALID_TOU_RECURRING_DATES_NODE_MISSING = "C3D9F5FB91AA4D94B41D3241F467CE45";
		public static final String INVALID_RECURRING_DATES_SCHEDULE_NODE_MISSING = "9F5A1086DAD04B4E87F800DECFAF76A1";
		public static final String INVALID_RECURRING_DATES_INDEX_NODE_MISSING = "FABAEF2277184F2B9F2086BF4D2A94E9";
		public static final String INVALID_RECURRING_DATE_NODE_MISSING = "99E5116612E640C09FDB356A604A680D";
		public static final String DUPLICATE_RECURRING_DATES_SCHEDULE_INDEX = "C8D4FDCBEBD54E71A66EDC89A6515FB4";
		public static final String INVALID_RECURRING_DATE_MONTH_NODE_MISSING = "4E8FBE68D20C4108ABC878483325ECD0";
		public static final String INVALID_RECURRING_DATE_OFFSET_NODE_MISSING = "9A234F571E2248069F0A2416516971D8";
		public static final String INVALID_RECURRING_DATE_WEEKDAY_NODE_MISSING = "76DF2EED08DE4303B4E20E2D6D0A8B69";
		public static final String INVALID_RECURRING_DATE_PERIOD_NODE_MISSING = "B1E495758E254D85A91607D0195A25A8";
		public static final String INVALID_RECURRING_DATE_DAY_NODE_MISSING = "9C21DD13410A4B53BC80FA19B96944F3";
		public static final String INVALID_RECURRING_DATE_DELTA_NODE_MISSING = "3E9A459C848042CC91C167490E6A9870";
		public static final String INVALID_RECURRING_DATE_ACTION_NODE_MISSING = "00DA47B028174C13B03A7E9ACC64B4C5";
		public static final String INVALID_ACTIVATE_PENDING_TOU_CALENDAR_DATE_TIME_NODE_MISSING = "19BD20C134F2418F85DDDD6AC19260C7";
		public static final String INVALID_RECURRING_DATE_PERFORM_BILLING_READ_NODE_MISSING = "5321DE9169E84A28899390DFBE50BEF4";
		public static final String INVALID_RECURRING_DATES_SCHEDULE_INDEX_NODE_MISSING = "BDA1D72F898E4DC3B58151FE3560CABC";
		public static final String INVALID_RECURRING_DATES_DST_ACTION = "5A3850C3A4454CE78F8B522A4FFEFB62";
		public static final String INVALID_RECURRING_DATES_SPECIAL_SCHEDULE_ACTION = "AC578BC952194867B8A468B772FAA8BF";
		public static final String INVALID_RECURRING_DATES_SCHEDULE_INDEX = "386FD20DBB9D4CF9BB11AF834B14C095";
		public static final String INVALID_RECURRING_DATES_MONTH = "05C8BFB1D5F747f5882634487D4995F1";
		public static final String INVALID_RECURRING_DATES_OFFSET = "13C87067CC61436c8037F121D908BFB1";
		public static final String INVALID_RECURRING_DATES_WEEKDAY = "42AE7A9EA0434e14922DFAA47F44FECF";
		public static final String INVALID_RECURRING_DATES_DAY = "AB84C78514AE40a4A7760050D48D7A37";
		public static final String INVALID_RECURRING_DATES_PERIOD = "504FA84F478249e9AD716DF7330BB8C4";
		public static final String INVALID_RECURRING_DATES_DELTA = "F74119C11A0649499F7FDD04D4FD5086";
		public static final String DUPLICATE_RECURRING_DATES_ACTION = "F35D430F6EF347F1B16208BEA90D2269";
		public static final String INVALID_RECURRING_DATES_ACTION = "CEA8B3233B14469090DD881D4842175C";
		public static final String INVALID_RECURRING_DATES_MONTH_ORDER = "82FBB76CF5F24828826BEE78F7AFCEBD";
		public static final String INVALID_RECURRING_DATES_PERFORM_BILLING_READ = "E3DC115A9A044224BE292C5D4E2EA27D";
		public static final String INVALID_ACTIVATE_PENDING_TOU_CALENDAR_DATE_TIME = "6C65F1244F8F45BDB9BDB2F1FC773E21";
		public static final String INVALID_DAY_SCHEDULES_NODE_MISSING = "50BDC636BB564CF8B3F7616B6FCB8342";
		public static final String INVALID_DAY_SCHEDULES_SCHEDULE_NODE_MISSING = "1483C31C7290417ABC10F0674C61416E";
		public static final String INVALID_DAY_SCHEDULE_INDEX_NODE_MISSING = "8ABF297B88D9461A994945DA35216E08";
		public static final String INVALID_DAY_SCHEDULE_SWITCH_NODE_MISSING = "1AEEE8FB6AEF4E9F893FCDDB193ACD25";
		public static final String INVALID_DAY_SCHEDULE_SWITCH_INDEX_NODE_MISSING = "793EE7B65EC54BEA88DF1405D5D9C5CC";
		public static final String INVALID_DAY_SCHEDULE_TIER_NODE_MISSING = "A8718F4EB34E4489A9DF6D98D8944F85";
		public static final String INVALID_DAY_SCHEDULE_START_HOUR_NODE_MISSING = "7A9EB66D4959407A8C47D5982B0A2643";
		public static final String INVALID_DAY_SCHEDULE_START_MINUTE_NODE_MISSING = "8C98168594D54B16A37B722E5A6A8DCE";
		public static final String DUPLICATE_DAY_SCHEDULE_SWITCH_INDEX = "35FFED4B20034E7D93A4C0996BB0839F";
		public static final String INVALID_CALENDAR_ID = "229890661FED4DFDB95F08357E9FB64E";
		public static final String DUPLICATE_DAY_SCHEDULE_INDEX = "5B0185B7730C45B7B33C805231068058";
		public static final String INVALID_DAY_SCHEDULE_INDEX = "C930FE4908EF4d9590D01247FDCA16D1";
		public static final String INVALID_DAY_SCHEDULE_SWITCH_INDEX = "7C14A0BE198248e68FAF644068F27714";
		public static final String INVALID_DAY_SCHEDULE_START_HOUR_START_MINUTE = "ED55BBEF2FFB427BB05CCA341D2929E9";
		public static final String INVALID_DAY_SCHEDULE_START_HOUR_START_MINUTE_NON_ZERO = "C06E0D6F52214737A7D3FF169788C84F";
		public static final String INVALID_DAY_SCHEDULE_TIER = "DEAF2D4059CE445e94BD228478201DAE";
		public static final String INVALID_DAY_SCHEDULE_START_HOUR = "C64BF0CBA1B84a389DA4562ED31539E4";
		public static final String INVALID_DAY_SCHEDULE_START_MINUTE = "DC94811B66E84e07923CB7B2EF3F8CED";
		public static final String INVALID_SEASON_SCHEDULES_NODE_MISSING = "17A69BB8A8D541ED9C8604BC12665103";
		public static final String INVALID_SEASON_SCHEDULES_SCHEDULE_NODE_MISSING = "B6C51E860B8645D1BB43D7E3780826FF";
		public static final String INVALID_SEASON_SCHEDULE_INDEX_NODE_MISSING = "D04C10895FF54118875213D2B29AEA13";
		public static final String INVALID_SEASON_SCHEDULE_SATURDAY_NODE_MISSING = "9301478647F2464BA2A279BE02468122";
		public static final String INVALID_SEASON_SCHEDULE_SUNDAY_NODE_MISSING = "5589B02AFF4E42EA9C1065F1FA7E0181";
		public static final String INVALID_SEASON_SCHEDULE_WEEKDAY_NODE_MISSING = "C03DFC541F12483FBCAC415DF1CBE454";
		public static final String INVALID_SEASON_SCHEDULE_SPECIAL_0_NODE_MISSING = "A7C30497715D4017A570EF0DA566C68F";
		public static final String INVALID_SEASON_SCHEDULE_SPECIAL_1_NODE_MISSING = "04BF80E8D7CC46C3B405CBA5968DB776";
		public static final String INVALID_SEASON_SCHEDULE_INDEX = "920388D6BB154286A44A3686F0CACDFF";
		public static final String INVALID_SEASON_SCHEDULE_SATURDAY = "8D05A3F0216744aa978A6DFE0875D559";
		public static final String INVALID_SEASON_SCHEDULE_SUNDAY = "33A8DDD671154e0590BCFE9A86AA58A5";
		public static final String INVALID_SEASON_SCHEDULE_WEEKDAY = "41eecd391c8e42b0af5f5d297ecd5b08";
		public static final String INVALID_SEASON_SCHEDULE_SPECIAL_0 = "CA6A5AF5C20C40d4B75986A785D14D53";
		public static final String INVALID_SEASON_SCHEDULE_SPECIAL_1 = "5D21A9FC959B4c63A8F33E73E699D012";
		public static final String INVALID_CALENDAR_PERFORM_SELF_READ_VALUE = "e1db2f18363847568e7018e91938f5f8";
		public static final String INVALID_READ_TOU_CALENDAR_OPTION_TYPE_ID = "AAABD09920144b46BAA47124229B7DB6";
		public static final String INVALID_EXPRESSION_DATA_POINT_PARAMETER = "85225d55bb764eca9fd5f33dfc4725e7";
		public static final String INVALID_EXPRESSION_LENGTH = "b0e000b9c564409084f0de359277b4e6";
		public static final String INVALID_FUNCTION_CALL_EXPRESSION_SYNTAX = "05f4b0994eeb4a4cb81599c9ccccec62";
		public static final String INVALID_SOAP_CALL_EXPRESSION_SYNTAX = "1d8ec4a57ec941a1ac82218a3c6c72da";
		public static final String INVALID_EXPRESSION_DATA_POINT_PARAMETER_NO_RESTRICTIONS = "FD46B5EA53FD44b49A9166E64EE94A04";
		public static final String INVALID_SYNTAX = "6511FCF4AE614d87B8F947B56E1F5147";
		public static final String INVALID_EXPRESSION_CALL_PARAMETER = "0C07350D3C404e4b880E3A9C9B3D3F97";
		public static final String INVALID_DATETIME_SYNTAX = "B43EB1D478D541a896B464448578875F";
		public static final String TYPE_MISMATCH = "AF5D081732DF4e08976D54FDA102EFF8";
		public static final String INVALID_SETTING_NOT_NUMERIC = "8ec661f1063d4644a46b9bdf88fd7c0b";
		public static final String INVALID_SETTING_OUT_OF_RANGE = "b94892f54e67476fa1dff9e20d39e2d0";
		public static final String INVALID_SETTING_EMAIL = "b3b5262ae651435987ef6a75179110ed";
		public static final String INVALID_SETTING_VALUE_LENGTH = "ba014de02b454724b35f4ead2178d0a9";
		public static final String INVALID_SETTING_ID = "28bdfbb073e24cc0b43f7520a75323e2";
		public static final String INVALID_SETTING_TYPE_ID = "E9EE95D8EAD143d183541147F574B10E";
		public static final String INVALID_SETTING_VALUE = "28CD31D77623401d90D7772BFB71ED36";
		public static final String INVALID_SOLUTION_SETTING_VALUE_TYPE_ID = "017209EC20A4468d91596367C56C92BC";
		public static final String UNSUPPORTED_SOLUTION_SETTING_VALUE_TYPE_ID = "A69938DD1AD544119A1E10B56B3EC9BB";
		public static final String INVALID_TABLE_NAME = "5836599954C94e4dB19706D3381997C8";
		public static final String INVALID_COLUMN_NAME = "F1D95AA741A54e11AEBD3C68531C7D59";
		public static final String INVALID_EXPIRED_INTERVAL_TYPE_ID = "8B03750B3D6341faAA2B5E02D6095EAE";
		public static final String INVALID_EXPIRED_INTERVAL = "FB30EB1C877D4c33A206D40F1C36E604";
		public static final String INVALID_SERVER_HOSTNAME = "597fdea72c4c480c872e51c1f703d5cb";
		public static final String INVALID_SERVER_HOSTNAME_DUPLICATE = "94f3e7b696db4d14bd0cd53299e1aead";
		public static final String INVALID_NETMASK = "6b90ff2bfd2b424dbc33297c9c34c714";
		public static final String UNSUPPORTED_SETTING_TYPE_ID = "04571597BF814620BFA6256E440F593A";
		public static final String WAN_CONFIGURATION_ROUTE_NOT_AVAILABLE = "5c88c8dc907b4e33afa5bdbdb97b4775";
		public static final String WAN_CONFIGURATION_ROUTE_NOT_SET_UP = "ae2da0c5e75d43b08d2dc061c3bef7f3";
		public static final String WAN_CONFIGURATION_NOT_SET_UP = "b40f7e5d97a14d39a6709d64ea012cd0";
		public static final String WAN_CONFIGURATION_IN_USE = "0795e486c48e4de58bf1f15cce6353b9";
		public static final String INVALID_WAN_CONFIGURATION_ID = "5823e0e6ff3547f484a04ea9afe1c179";
		public static final String INVALID_WAN_CONFIGURATION_DUPLICATE = "714e628b316946389f41ea8d9775d471";
		public static final String INVALID_GATEWAY_WAN_CONFIGURATION_ROUTE_ID = "facace4ded534412ac770e1b5eedeb57";
		public static final String INVALID_IP_ADDRESS_RANGE_NOT_AVAILABLE = "58883233c48f4457836cfe7dd5048a9b";
		public static final String INVALID_IP_ADDRESS_RANGE_OVERLAP = "7e0b43bcc8a848269efe0c541ebae058";
		public static final String INVALID_OUTBOUND_CONNECTIONS = "b0a639b67b95468aa170a2fb83208473";
		public static final String INVALID_PHONE_NUMBER_DUPLICATE = "765ef23301134d2e8231ee0d93f95fa9";
		public static final String GATEWAY_IN_IP_ADDRESS_RANGE = "EB5EB70F8E1E484893EC885C48A61A18";
		public static final String INVALID_SCHEDULE_ASSIGNMENT_ID = "C8D9D0A1CED949b090685069C6A1A408";
		public static final String INVALID_SCHEDULE_NULL = "63199B9508A2413984B1982B76AB73D2";
		public static final String INVALID_RETRIEVE_ENCRYPTED_VALUE = "f325722b70cd48019193a7f815591b8b";
		public static final String INVALID_WAN_CONFIGURATION_TYPE = "d7583461d0934588b4dfdf07dd76435c";
		public static final String INVALID_SETTING_NODE_MISSING = "5c542febb54a4ff983fa22868ed0e163";
		public static final String INVALID_SETTING_ID_NODE_MISSING = "bf5b9c9aff59418e8672a5d966579306";
		public static final String INVALID_WAN_CONFIGURATION_GATEWAY_COMM_TYPE_MISMATCH = "7589733f21f0498ba3fd2e30e71aa0a4";
		public static final String INVALID_SETTING_VALUE_RANGE = "e79111001a9d44e6acc293f11f7c12c9";
		public static final String INVALID_CONNECTION_GUARD_TIME = "68772ed056414112add88ddb6ac821de";
		public static final String INVALID_NUMBER_OF_RETRY_ATTEMPTS = "454d1bc6523f4cc9b75e38f1776624bb";
		public static final String INVALID_RETRY_ATTEMPTS_INTERVAL = "21d5e58a3315420ca60836254deaefdc";
		public static final String INVALID_MAXIMUM_TIME_TO_CONNECT = "8f1f5ccb4a7142ee9cda23613df95009";
		public static final String INVALID_SEND_HELLO = "c13851288e324f68ae62c5162db35461";
		public static final String INVALID_USE_AS_DEFAULT = "947c73dd72274947884c10af67e997f6";
		public static final String INVALID_USE_AS_DEFAULT_MULTIPLE = "8aafb5fb028c4e03b5e70bb3216abf10";
		public static final String INVALID_DEFAULT_ASSIGNMENT_NAME = "0e9b8f663440477b847fcbe986167d78";
		public static final String INVALID_DEFAULT_ASSIGNMENT_NAME_NODE_MISSING = "966d475c907a49d0b8d14afc0bc87f65";
		public static final String INVALID_IP_ADDRESS_TYPE_ID = "5d8fee62e3a04f8dace07c2303410ad8";
		public static final String INVALID_WAN_CONFIGURATION_NAME = "507a0c9aee984be688eece26bcee9395";
		public static final String INVALID_WAN_CONFIGURATION_NAME_DUPLICATE = "9eac8641046d4ee9af7736b8c7182ac5";
		public static final String INVALID_PING_GATEWAY_STATUS_TYPE_ID = "2bc1fb07bf3d4acc9c307a8b7540d818";
		public static final String INVALID_SOLUTION_SETTING_NAME = "f5c613a32549436dad30c01ddd21d29b";
		public static final String INVALID_SOLUTION_SETTING_NAME_DUPLICATE = "09405518d15c48bcb97adda304796b5d";
		public static final String INVALID_TEST_TCPIP_PORT_STATUS_TYPE_ID = "872eff9f3bc744c8bf21e2c38417360a";
		public static final String CONFLICTING_PING_AND_TEST_TCPIP_STATUS_TYPES = "c622802b5bf341c393951a9bb4c8321d";
		public static final String INVALID_PASSIVE_FTP_IP_ADDRESS_SOURCE_TYPE_ID = "da687b271ff54e66ad89071901c99aff";
		public static final String CATEGORY_NODE_NOT_FOUND = "277E2167DF264d9bAC3BA09D67F46841";
		public static final String INVALID_FIRMWARE_IMAGE = "32639d7a350b41e4a823839b561cc85c";
		public static final String INVALID_VERSION_NUMBER = "633bf2758d2b44c58ceb00c1058947c1";
		public static final String INVALID_ENTITY_TYPE_ID = "c89247794c394284b04065fd6d5c0351";
		public static final String INVALID_ENTITY_TYPE_TYPE_ID = "35d1506667a54d58a2173877fb0c1561";
		public static final String INVALID_BUILD_DATETIME = "0288a11ec44d469698862c00d7332125";
		public static final String INVALID_FIRMWARE_VERSION_ID = "f8305306d5394540a7843285de7f8a4c";
		public static final String INVALID_FIRMWARE_VERSION_NUMBER_NOT_DEFINED = "113743FE24134138895A9FEAEAF377D4";
		public static final String INVALID_VERSION_NUMBER_DUPLICATE = "3f22500c2e5d4c0ebe30e00e18d394d7";
		public static final String INVALID_TASK_PROCESSOR_PORT = "999d30e15a89464ba5a6818d70672a79";
		public static final String INVALID_TASK_PROCESSOR_PORT_DUPLICATE = "3d7d8cfa0d9a449ca687ea7e6be72891";
		public static final String INVALID_HEARTBEAT_INTERVAL = "d6e3972830d749c399476acb55ad1fce";
		public static final String INVALID_LOCAL_TASK_MANAGER_ID = "6e6eea719bf74dcb94f05177f3bf0897";
		public static final String INVALID_LOCAL_TASK_MANAGER_ID_BLANK = "6c8673d65a344b12ac0b8533486166c1";
		public static final String INVALID_LOCAL_TASK_MANAGER_ID_NODE_MISSING = "5651b0322f1a4e8999ce5441a40eb96d";
		public static final String INVALID_ENGINE_RECEIVER_PORT = "129393b49c3e43de8465908afd791636";
		public static final String INVALID_ENGINE_RECEIVER_PORT_DUPLICATE = "25bbb60213224f5080cf8f162f7f8ff4";
		public static final String INVALID_TASK_PROCESSOR_ID = "90cca5554c9c4492ad0a455c4709d5b7";
		public static final String INVALID_DELETE_TASK_PROCESSOR_RUNNING = "9c41f123447941af90b477aeac86e0b9";
		public static final String INVALID_TASK_PROCESSOR_COMMAND_ID = "964d8deca24d43eaad748e8da5a5aaf6";
		public static final String INVALID_TASK_PROCESSOR_COMMAND_ID_MISSING = "263ae98e8e2c4aeea3696abfc5ec2d33";
		public static final String INVALID_TASK_PROCESSOR_COMMAND = "3452cfe8cf1c49b19d15d9d1b52d9f76";
		public static final String INVALID_TASK_PROCESSOR_ID_MISSING = "9ee904168c6c4cd88df004e1f533a87f";
		public static final String INVALID_TASK_PROCESSOR_STOP_COMMAND = "a34c6d94c55648bcb5a4eb5be7dbf111";
		public static final String INVALID_TASK_PROCESSOR_START_COMMAND = "df274a2923b244b9a1a27a320747bf0c";
		public static final String INVALID_COMMAND_HISTORY_ID = "f7d6fa8e502b48d48a50375b84b055db";
		public static final String INVALID_ROUTING_ENTITY_TYPE_ID = "2e257e00d9c444fe8f6730720e372677";
		public static final String INVALID_START_REQUEST_DATETIME = "53210a258bcf48ef8694b64a81700880";
		public static final String INVALID_END_REQUEST_DATETIME = "1ef6fdeca979480ba2ca06cb705d0a6a";
		public static final String INVALID_REQUEST_DATETIME_RANGE = "4a1e1c6745514e86ad1e06bd473d2285";
		public static final String INVALID_START_COMPLETE_DATETIME = "edda6db491e74ff5a99c2592cf0bee4a";
		public static final String INVALID_END_COMPLETE_DATETIME = "dc6af89458504bda8fe57032b05501cc";
		public static final String INVALID_COMPLETE_DATETIME_RANGE = "aec50606ba754525a2793267ba20b6f0";
		public static final String INVALID_TASK_PROCESSOR_TYPE_ID_DUPLICATE = "f22c965467ac4e0eab3eff36f3f34b0e";
		public static final String INVALID_TASK_PROCESSOR_NAME_DUPLICATE = "4006e3a9edc242d282fafdbb7cd79f7b";
		public static final String INVALID_TASK_PROCESSOR_NAME_BLANK = "1a100bea43c648f79857bbb9e7c6049f";
		public static final String INVALID_MAX_CONCURRENT_TASKS = "e4c256f93d5e4f2996c384ab57af39b6";
		public static final String INVALID_ENGINE_CREATION_NOT_ALLOWED = "296ad52f355d4f0593613cc7b5fff5c7";
		public static final String UNSUPPORTED_TASK_PROCESSOR_TYPE_ID = "52c2b335e33d40e4a974f5f8b755dc5a";
		public static final String INVALID_TASK_PROCESSOR_RUNNING = "09fd1df659b14cfdb51b19a2c744920e";
		public static final String INVALID_ENGINE_IP_ADDRESS_ASSIGNMENT_TYPE_ID_NODE_MISSING = "ab84b69849cd47748ac81796cf85a1d8";
		public static final String INVALID_ENGINE_IP_ADDRESS_ASSIGNMENT_TYPE_ID = "f5b63298ffe344e99e129e3855397583";
		public static final String INVALID_DELETE_TASK_PROCESSOR_TYPE = "b8e5c1e1ab0343b8a1bb0e400c052022";
		public static final String INVALID_TASK_PROCESSOR_TYPE_ID_NODE_MISSING = "74ab07d993bc42c4a1ea04e3dc4c0965";
		public static final String INVALID_COMMAND_HISTORY_TYPE = "B0112BC481C5456e82264689C31CDF6E";
		public static final String INVALID_COMMAND_ID = "9e4f50d0d293475c85d551090b9c85da";
		public static final String INVALID_COMMAND_NODE_MISSING = "5281ddfe6e1a41e5b24a6d35d2afd3d0";
		public static final String INVALID_COMMAND_ID_NODE_MISSING = "3b99c1f425724d17abfe395384b2bfb1";
		public static final String INVALID_ROUTING_ENTITY_NODE_MISSING = "ff0a283a789b474b85f0a02a013826b3";
		public static final String INVALID_STATUS_TYPE_NODE_MISSING = "39ffc5a54a93419088228ef4d2ed77b0";
		public static final String INVALID_ROUTING_ENTITY_TYPE_NODE_MISSING = "950810a52f07450eae0af041a48499a6";
		public static final String INVALID_ROUTING_ENTITY_ID_NODE_MISSING = "4aa587f58aa94d019555e812cd565915";
		public static final String INVALID_STATUS_TYPE_ID_NODE_MISSING = "2d88c36f230e450a9c8884be630786a2";
		public static final String INVALID_RESULT_TYPE_NODE_MISSING = "587a1932421c4674883785758e39fb0d";
		public static final String INVALID_RESULT_TYPE_ID_NODE_MISSING = "adbd977d90794c3bb52102c445902596";
		public static final String INVALID_COMMAND_HISTORY_NODE_MISSING = "acc162301c6547be9a67d36b0bafd08b";
		public static final String INVALID_COMMAND_HISTORY_ID_NODE_MISSING = "06a5308202ad47d9b39ea8c29fa5c340";
		public static final String INVALID_EVENT_DEFINITION_DELIVERY_TYPE_ID = "dc9c25f554024350940d4e30fe43062d";
		public static final String INVALID_EVENT_DEFINITION_ID = "c80ac4b4910144439cfa478dea8d30ef";
		public static final String INVALID_EVENT_DEFINITION_NAME_BLANK = "984f76135ebf40028272e1f0a0c619c5";
		public static final String INVALID_EVENT_DEFINITION_NAME_DUPLICATE = "424020ab1d6c4294b16c73252ef832ab";
		public static final String INVALID_EVENT_DEFINITION_PRIORITY = "3bc3a15c131c41aab5fc810095b0db90";
		public static final String INVALID_EVENT_DEFINITION_STATUS_TYPE_ID = "2dfc2d30126e4478a30dcddf3dc761a3";
		public static final String INVALID_EVENT_HISTORY_DATETIME_RANGE = "7c859066209c400d91d2d301c8257df2";
		public static final String INVALID_EVENT_HISTORY_END_DATETIME = "079240dd067b45f4923fc3e717dfda9e";
		public static final String INVALID_EVENT_HISTORY_ID = "362fba7a5b0f406bb8ca69994aa11142";
		public static final String INVALID_EVENT_HISTORY_START_DATETIME = "9fe3a91d64964783b9615678f4c79086";
		public static final String INVALID_HISTORY_NODE_MISSING = "8e96849bba5b4f8fba81368c170e3409";
		public static final String INVALID_HISTORY_ID_NODE_MISSING = "8638987abc414e8394345f0bf766799a";
		public static final String INVALID_EVENT_HISTORY_LIST_NODE_MISSING = "d1bc17d69c4c44a7ae6444af03dea289";
		public static final String INVALID_TASK_ID = "74daaf153c224d20b89c71017cd88644";
		public static final String INVALID_TASK_PRIORITY = "e7b364a912fd45218e03bfe8588d59d2";
		public static final String INVALID_TASK_STATUS_TYPE_ID = "800c9699c97a48b68ef4199e96ade146";
		public static final String INVALID_TASK_TIMEOUT_DATETIME = "b011edcbd39f4fbebdd40db1af5f28e0";
		public static final String INVALID_TASK_TO_DELETE = "ce2d082edbed416dab6d57e597356b51";
		public static final String INVALID_TASK_TO_REQUEUE = "3b407d4982f54a5e929c9fa1dc11aade";
		public static final String INVALID_TASK_TYPE_ID = "ac11725d88594d7db38ec678bc68bb70";
		public static final String INVALID_CREATION_DATETIME_RANGE = "eb713728437c4460842a7e481ab3be4c";
		public static final String INVALID_CREATION_END_DATETIME = "b8035e0d421f4603957b8a931cf5bc32";
		public static final String INVALID_CREATION_START_DATETIME = "0d0335bb90a64503b4bf91223e7a05d6";
		public static final String INVALID_EXECUTION_DATETIME_RANGE = "cf28397e4a9c43eaad15a84df13ab220";
		public static final String INVALID_EXECUTION_END_DATETIME = "5b752d91d486479e8d8134adae552dc6";
		public static final String INVALID_EXECUTION_START_DATETIME = "7e4c212e59f84f18b84c85b6f49ab094";
		public static final String INVALID_TASK_TYPE_ID_NODE_MISSING = "4da3bbed01b34475bd4fd331bc5fccfd";
		public static final String INVALID_ENTITY_NODE_MISSING = "682a12eb39cb4ef2a98437e0a923f79a";
		public static final String INVALID_ENTITY_TYPE_ID_NODE_MISSING = "557abce06368464bacf3745676cdf94e";
		public static final String INVALID_ENTITY_ID_NODE_MISSING = "47e7fcdff1fb4c8481ce8a460b41e24b";
		public static final String INVALID_PARAMETER_NULL = "711fc7c2ef4d4e4581e1d932d7dd8cf1";
		public static final String INVALID_GATEWAY_NAME_NODE_MISSING = "c3e96f30955b4e50895e553be1fe5b62";
		public static final String INVALID_DATA_POINT_NAME_NODE_MISSING = "98a7bf0ec4a24c52b1aeacf5a5b441b9";
		public static final String INVALID_DEVICE_NAME_NODE_MISSING = "deda944a1f5846a4bef71fc1a08a581e";
		public static final String INVALID_NAME_SEARCH_TYPE_ID = "fb149df976074e118efd623470c41a8f";
		public static final String INVALID_RESULT_NODE_MISSING = "4e0e909360de4a9aa1990344c60a3f0c";
		public static final String INVALID_RESULT_ID_NODE_MISSING = "f8ef6fe360c1457eb21843c7fcc75e7b";
		public static final String INVALID_EXECUTION_DATETIME = "672032d0d782410b98194d54f1228601";
		public static final String INVALID_PROCESS_CONFIGURATION_NODE_MISSING = "f21c4390e57b4c4896ae9821bd369fb1";
		public static final String INVALID_PROCESS_CONFIGURATION_ID_NODE_MISSING = "a2e2ba6970d7442ab8d5840dada642ff";
		public static final String INVALID_PROCESS_CONFIGURATION_ID = "65001ddd12ff407b86fbbaae5738ed54";
		public static final String INVALID_PROCESS_CONFIGURATION_COMMAND = "067308d54714419baa2a118c8697dd79";
		public static final String INVALID_STATUS = "0c2f16a513314a73b1b90a085e11af2d";
		public static final String INVALID_RUN_DATE_TIME = "ecbbb55c9a6d495cb4b7a75ec981c4df";
		public static final String INVALID_MINIMUM_ACTIVATION_INTERVAL = "7a852c793ca34c718174d1013cefa271";
		public static final String INVALID_MAXIMUM_ACTIVATION_INTERVAL = "fb6f6c62d1da4d349b1c5d810ec689e3";
		public static final String INVALID_BILLING_DATA_TIERS_NODE_MISSING = "8624fe19725c4916a1d26042265c614e";
		public static final String INVALID_BILLING_DATA_TIERS_INDEX_NODE_MISSING = "e10a95b305c64ef09fded72718fdc835";
		public static final String INVALID_BILLING_DATA_TIER_COUNT = "f5fe6dfc89584e1a892c80fcbc91ca29";
		public static final String INVALID_BILLING_DATA_TIER = "4061f4a6585045c299cae297fc077924";
		public static final String DUPLICATE_BILLING_DATA_TIERS = "b026fb9f8fec428bb51943c4d502f54b";
		public static final String READ_BILLING_DATA_ON_DEMAND_IN_PROCESS = "7DBADD43795C442b80A9095D8559DB02";
		public static final String INVALID_AUTO_DISCOVERY_CONFIGURATION_NODE_MISSING = "2366bee08ea14be7af36beed6e457b76";
		public static final String INVALID_AUTO_DISCOVERY_CONFIGURATION_MBUS_NODE_MISSING = "972cfd797ee945c285ff91ed71ed3dd9";
		public static final String INVALID_AUTO_DISCOVERY_CONFIGURATION_MBUS_STATUS_TYPE_ID_NODE_MISSING = "888fb3b68ba34e3685aaab440641af96";
		public static final String INVALID_AUTO_DISCOVERY_CONFIGURATION_MBUS_STATUS_TYPE_ID = "ffae8ec356364b6ba168e3ff51ea099a";
		public static final String INVALID_UTILITY_INFORMATION_NODE_MISSING = "97eb13deab9246ad99723a24f00b64ae";
		public static final String INVALID_UTILITY_INFORMATION_COMMAND = "6eff73ed29df43fe9c9d167b37c0a9b2";
		public static final String INVALID_UTILITY_INFORMATION_OWNER_NAME = "2aba9732888a45dc87676abe33b6d42b";
		public static final String INVALID_UTILITY_INFORMATION_OWNER_NAME_LENGTH = "f73b444bad3b4b5ea644ac0d4e297e6a";
		public static final String INVALID_UTILITY_INFORMATION_UTILITY_DIVISION = "9d8978e82271492fb8dafbe30c50b886";
		public static final String INVALID_UTILITY_INFORMATION_UTILITY_DIVISION_LENGTH = "2412224f153249119539a03c1768bdad";
		public static final String INVALID_UTILITY_INFORMATION_MISCELLANEOUS_IDENTIFICATION = "d823154b8253404f9801e9ae376b143e";
		public static final String INVALID_UTILITY_INFORMATION_MISCELLANEOUS_IDENTIFICATION_LENGTH = "bea9d6b8336b4d6fbeff2578d50208b1";
		public static final String INVALID_TIME_ZONE_CONFIGURATION_NODE_MISSING = "c358c9f59bba4f9d98b0257b7cbd23ae";
		public static final String INVALID_TIME_ZONE_CONFIGURATION_COMMAND = "ea998d46946041769f3e9cc5836a3415";
		public static final String INVALID_DAYLIGHT_SAVING_TIME = "1fe0525ab5dd4547a0f7a81da40cd91f";
		public static final String INVALID_DAYLIGHT_SAVING_TIME_END_TIME_NODE_MISSING = "2fb6de2bddac4140b540151c9eab31a7";
		public static final String INVALID_DAYLIGHT_SAVING_TIME_START_TIME_NODE_MISSING = "a4c8691cd4bc45f4ad94b72d5e19693d";
		public static final String INVALID_DAYLIGHT_SAVING_TIME_START_TIME = "656756cb79f544c095b32eeff8447fc5";
		public static final String INVALID_DAYLIGHT_SAVING_TIME_END_TIME = "df2e09280db34c5bb5d17c87b4b1e4c5";
		public static final String INVALID_DAYLIGHT_SAVING_TIME_ADJUSTMENT_OFFSET = "338a51c58897466fa110cb3b30a91869";
		public static final String INVALID_DAYLIGHT_SAVING_TIME_TIME_ZONE_OFFSET = "a04cefeb1ea243aa9314fe435759c937";
		public static final String INVALID_DEVICE_MISSING = "7b9bc07aa29347a09e831f1f6185d1c7";
		public static final String INVALID_DEVICE_ID_MISSING = "3829ead36e344e27b0f1bef2783a00d9";
		public static final String INVALID_GATEWAY_MISSING = "ef4d9f24e4e64a6eabe42b4377cf8318";
		public static final String INVALID_GATEWAY_ID_MISSING = "825fdf4bb2ab424185ad274ee89e229b";
		public static final String INVALID_RETURN_CODE = "af230356d61b4e2b950b440a8040de34";
		public static final String INVALID_TOU_RECURRING_DATE_MONTH = "05C8BFB1D5F747f5882634487D4995F1";
		public static final String INVALID_TOU_RECURRING_DATE_OFFSET = "13C87067CC61436c8037F121D908BFB1";
		public static final String INVALID_TOU_RECURRING_DATE_DAY = "AB84C78514AE40a4A7760050D48D7A37";
		public static final String INVALID_TOU_RECURRING_DATE_WEEKDAY = "42AE7A9EA0434e14922DFAA47F44FECF";
		public static final String INVALID_TOU_RECURRING_DATE_PERIOD = "504FA84F478249e9AD716DF7330BB8C4";
		public static final String INVALID_TOU_RECURRING_DATE_DELTA = "F74119C11A0649499F7FDD04D4FD5086";
		public static final String INVALID_TOU_ACTION = "CEA8B3233B14469090DD881D4842175C";
		public static final String INVALID_TOU_PERFORM_BILLING_READ = "E3DC115A9A044224BE292C5D4E2EA27D";
		public static final String INVALID_TOU_SCHEDULE_INDEX = "C930FE4908EF4d9590D01247FDCA16D1";
		public static final String INVALID_TOU_SWITCH_INDEX = "7C14A0BE198248e68FAF644068F27714";
		public static final String INVALID_TOU_TIER = "DEAF2D4059CE445e94BD228478201DAE";
		public static final String INVALID_TOU_START_HOUR = "C64BF0CBA1B84a389DA4562ED31539E4";
		public static final String INVALID_TOU_START_MINUTE = "DC94811B66E84e07923CB7B2EF3F8CED";
		public static final String INVALID_TOU_SATURDAY = "8D05A3F0216744aa978A6DFE0875D559";
		public static final String INVALID_TOU_SUNDAY = "33A8DDD671154e0590BCFE9A86AA58A5";
		public static final String INVALID_TOU_SPECIAL0 = "CA6A5AF5C20C40d4B75986A785D14D53";
		public static final String INVALID_TOU_SPECIAL1 = "5D21A9FC959B4c63A8F33E73E699D012";
		public static final String INVALID_TOU_WEEKDAY = "41eecd391c8e42b0af5f5d297ecd5b08";
		public static final String DUPLICATE_ILON100_NEURON_ID = "27d1f8d25d01441b876b2ddfe9142fcb";
		public static final String INVALID_ILON100_NEURON_ID = "ae7663061d8d4607aa8e39d02ab04f65";
		public static final String INVALID_PPP_LOGIN = "2bd9054a5be8434785446ebbc6a10460";
		public static final String INVALID_PPP_LOGIN_BLANK = "7659b5b48e0d41159321ba012f3e33f6";
		public static final String INVALID_PPP_LOGIN_DUPLICATE = "ec945e116bb04a92a5435be263fac195";
		public static final String INVALID_PPP_LOGIN_AND_PASSWORD_MISSING = "a00bf7180912475593f8dcb1fa78d1f6";
		public static final String INVALID_PPP_LOGIN_MISSING = "bd4e3987ef92475e84248d5d58b2adcb";
		public static final String INVALID_RETRIEVE_BY_PARAMETER_ID_TYPE = "55da2d57a75f44f0a2e747fbea184d5a";
		public static final String INVALID_SECONDARY_CONTROL_OUTPUT_TIER_VALUE = "2b1eb3f5528b4c24b073bda6c4ee57be";
		public static final String INVALID_ENABLE_SECONDARY_CONTROL_OUTPUT_TIERS_VALUE = "9fe600ec48e94bac9d29b0957155e9f9";
		public static final String INVALID_DATABASE_LOCATION_EMPTY = "70daf229c6ae42d5928d7c9d844a2c8b";
		public static final String INVALID_DATABASE_LOGIN_EMPTY = "5460ac0344f54dde8bc741abb95b6c98";
		public static final String INVALID_DATABASE_NAME_EMPTY = "5c0358efe5354b7cba6c42569d25ca26";
		public static final String INVALID_ATTRIBUTE_MISSING = "8fe91c67cc6e48dab1b46fa06e70cc01";
		public static final String INVALID_HIERARCHY_LEVEL_MEMBER_MISSING = "f9b4827652b346b9abe84d9e5f8d0518";
		public static final String INVALID_SECONDARY_CONTROL_OUTPUT_RELAY_STATUS_TYPE_ID = "37381b497105406993fdaa657393ac13";
		public static final String INVALID_LOCAL_TASK_MANAGER_ID_MISSING = "6c8673d65a344b12ac0b8533486166c1";
		public static final String INVALID_PPP_PASSWORD_MISSING = "dbcae2ef87184e65b09cd31c9db098c0";
		public static final String COMMUNICATION_SETTING_ROUTE_NOT_AVAILABLE = "5c88c8dc907b4e33afa5bdbdb97b4775";
		public static final String COMMUNICATION_SETTING_IN_USE = "0795e486c48e4de58bf1f15cce6353b9";
		public static final String INVALID_COMMUNICATION_SETTING_DUPLICATE = "714e628b316946389f41ea8d9775d471";
		public static final String INVALID_GATEWAY_COMMUNICATION_ROUTE_SETTING_ID = "facace4ded534412ac770e1b5eedeb57";
		public static final String INVALID_COMM_SETTING_GATEWAY_COMM_TYPE_MISMATCH = "7589733f21f0498ba3fd2e30e71aa0a4";
		public static final String INVALID_COMMUNICATION_SETTING = "5823e0e6ff3547f484a04ea9afe1c179";
		public static final String INVALID_IP_ADDRESS_IN_CONFLICTING_COMMUNICATION_ROUTE = "dddbb7439b0240b293b068c40d5f98fd";
		public static final String COMMUNICATION_SETTING_ROUTE_NOT_SET_UP = "ae2da0c5e75d43b08d2dc061c3bef7f3";
		public static final String COMMUNICATION_SETTING_NOT_SET_UP = "b40f7e5d97a14d39a6709d64ea012cd0";
		public static final String INVALID_COMMUNICATION_SETTING_TYPE = "d7583461d0934588b4dfdf07dd76435c";
		public static final String INVALID_NEW_COMMUNCATION_ROUTE_SETTING_ID = "3e2b1120b77546f3b5586ac2464374f4";
		public static final String INVALID_RECURRING_DATES_INDEX_MISSING = "BDA1D72F898E4DC3B58151FE3560CABC";
		public static final String INVALID_PREPAY_MAXIMUM_POWER_STATUS_TYPE_ID_NODE_MISSING = "f270a84b294440698d5e69759c5ae26c";
		public static final String INVALID_PREPAY_MAXIMUM_POWER_STATUS_TYPE_ID = "be3fed7edac94922960d35a80bf82921";
		public static final String INVALID_PREPAY_MAXIMUM_POWER_LEVEL_NODE_MISSING = "f7a01bac1a414a05944362b931428dd9";
		public static final String INVALID_PREPAY_MAXIMUM_POWER_LEVEL = "fd74d21ba4a94171b4c613b1201abd5b";
		public static final String INVALID_SEASON_SCHEDULE_NODE_MISSING = "17A69BB8A8D541ED9C8604BC12665103";
		public static final String INVALID_SEASON_SCHEDULE_SCHEDULE_NODE_MISSING = "B6C51E860B8645D1BB43D7E3780826FF";
		public static final String INVALID_PULSE_INPUT_REGULAR_ALARM_STATUS = "79a2676e8fb14d7aa91e07b47fd02d1e";
		public static final String INVALID_PULSE_INPUT_IDLE_STATE_STATUS = "a46d11fdc3f94d6c9e11aa26811b5a8d";
		public static final String INVALID_GATEWAY_COMMUNICATION_HISTORY_REQUEST_TYPE_ID = "9eac04040e4c4119acd50e91bdfa5181";
		public static final String INVALID_UPDATE_DC_1000_FIRMWARE_COMMAND = "62C11ECEDF76441a8CBA14B333BB1CF1";
		public static final String INVALID_TASK_PROCESSOR_PATH = "27ff02cb3b8648269527e6857ac2f33a";
		public static final String INVALID_TASK_PROCESSOR_PATH_MISSING = "c28a8b0897764fc581e38761c94644c5";
		public static final String INVALID_AUTHENTICATION_KEY = "a87498cd5e1d481eadaa4f6f1c1e432e";
		public static final String INVALID_LOAD_PROFILE_CHANNEL_SOURCE = "ed98589734a04abda4fafc778dba1621";
	}

	public final class MBusAutoDiscoveryStatusTypes {
		public static final String ENABLED = "ce8efc7530964e9a917bc70407af21af";
		public static final String DISABLED = "4672466240864adeaa7d16008551e272";
	}

	public final class MessageLogStatus {
		public static final String ENABLED = "2f3ab4b458764083b84b2fca15049604";
		public static final String DISABLED = "4b2859f9ba4243f88fbe6a6d64f69616";
	}

	public final class StandardAPIOptions {
		public static final String YES = "5af8a532523c4c6e9f9344c0827391ee";
		public static final String NO = "fe6a84d285d74e06bc52413ffb81151a";
		public static final String DISCONNECT = "7d6b389f36044740bc0e1babb6340b4f";
		public static final String CONNECT = "30bd98ee3bf641798418ee0222d235b6";
		public static final String EXACT = "de334f6f1654471d99e18df1b7c29fdd";
		public static final String INEXACT = "78c2a568a6a04a9e9ccf08329043657f";
	}

	public final class SettingTypes {
		public static final String ARCHIVESETTING = "1cb53121e2bd4c50b78568ca6cba980c";
		public static final String WAN_CONFIGURATION = "ef34587793674fafab8c2d4f77c873a3";
		public static final String WAN_CONFIGURATION_ROUTE = "b3ad8aa480f240538db0df10eaf004c7";
		public static final String SOLUTIONSETTING = "beb7933008fa44c590692991de18fa86";
		public static final String COMMUNICATIONSETTING = "ef34587793674fafab8c2d4f77c873a3";
		public static final String COMMUNICATIONROUTE = "b3ad8aa480f240538db0df10eaf004c7";
	}

	public final class SolutionSettingTypes {
		public static final String API_KEY_TIMEOUT_PERIOD = "6DB13F06AE454d7cB054061A250B76A2";
		public static final String HIERARCHY_PATH_DELIMITER = "7ADD9A1F555E4f879E2F0A8C6204932A";
		public static final String SOLUTION_NAME = "DDB91D53C6044f4eA617D2C274B39B89";
		public static final String AGGREGATION_SOAP_CALL_TIMEOUT_PERIOD = "7E9C00D0C8664d3eA4C3E5500A21F6C0";
		public static final String TCPIP_SOCKET_MESSAGE_TIMEOUT_PERIOD = "94417108619144a2881510CC5E4A8458";
		public static final String ILON100_DATALOG_MAX_UPLOAD_SESSION_TIMEOUT = "09AC43CA44A044b188384AE033913ED1";
		public static final String DATALOG_SOAP_READ_COUNT = "ca2bd557f2ab46aca620f986326157b4";
		public static final String DAILY_SCHEDULE_CALCULATION_TIME = "eea43a3a2bf14490a7726d20aaee8a7f";
		public static final String SUMMARY_AGGREGATION_CALCULATION_HOURS = "8FB08B60BD594ac29963C56A24FD71AA";
		public static final String SUMMARY_GATEWAYS_HOURS = "EF6115D7EDCA43ac8221DF076FE9CD44";
		public static final String ILON100_WEB_SERVICE_LOCATION = "9f2a946206ff47dbbf37047ac6b074e4";
		public static final String HIERARCHY_WILDCARD = "10c13e93eb2340e8ae1f52a903a33e7b";
		public static final String SUMMARY_KEY_DATA_POINT_IDS = "81cc4f8a18e64b308d0f0dd258cb1d21";
		public static final String SOLUTION_MANAGER_SPLASH_TIME = "daee7fe28c734a6aa6a185a050a772f1";
		public static final String DATAMANAGER_INIT_UPDATE_DELAY = "224b3c46eceb43d588f556e26bfad2ae";
		public static final String ILON100_WEB_SERVICE_PORT = "43e382a7a2754960a6156daccb328f82";
		public static final String AGGREGATION_SQL_QUERY_TIMEOUT_PERIOD = "984CCB5060724e57AA6E4161985F69E4";
		public static final String DC1000_ADAPTER_SERVER_URL = "4cf38c4953034ce79ca6fabe98cd1fca";
		public static final String TIME_WAIT_DEPENDENT_TASK_NOT_FOUND = "38bbc28186104981a359b3c18e7e6a2a";
		public static final String DC1000_RESOURCE_READ_COUNT = "72b14bb18d25435eb12b11ef7a87bb08";
		public static final String DC1000_MAX_CONNECTION_INACTIVITY_TIMEOUT = "66688870F9554ee1A3227B0DBDD06E7C";
		public static final String DC1000_DCXP_COMMAND_TIMEOUT = "AAE6926A528D4220822ABF88D8FC2651";
		public static final String DC1000_MAX_OUTSTANDING_DCXP_COMMANDS = "FEC89CF21289495eA455F428327784CA";
		public static final String EVENT_RECEIVER_URL = "7a3405d3ebe2486491cff11bcb7a622d";
		public static final String EVENT_RECEIVER_NAMESPACE = "a0aa36cb53704f01940ea070933b70ff";
		public static final String WINDOWS_RAS_PHONEBOOK = "D51D39D27E154fd39CD76995EB7E01C0";
		public static final String DC1000_FTP_TIMEOUT = "7eed92692f8a4b038b61ef374bc09882";
		public static final String CISCO_ACCESS_SERVER_AUTHENTICATION_ENGINE_SERVER_SECRET = "BC79C2A6AA5D4046A6E18A82E0F64C66";
		public static final String CISCO_ACCESS_SERVER_DIALER_GROUP_NAME = "EC8ABF2C85ED4fd8B1CF5D2465DFC4A4";
		public static final String ORPHAN_CHECK_TIME_RAS_CONNECTED = "55f6a3f38d9240a5a422760b23ec32a2";
		public static final String ORPHAN_CHECK_TIME_CISCO_ACCESS_SERVER_ADAPTER_CONNECTED = "0839026f00b4490b89f7a3987c8c7e3d";
		public static final String ORPHAN_CHECK_TIME_ALWAYS_ON_IP_CONNECTED = "88c4045ad843454d9692fc4bf394999c";
		public static final String ORPHAN_CHECK_TIME_RAS_PENDING = "ca968778d96a44799df4b03e475e1e91";
		public static final String ORPHAN_CHECK_TIME_CISCO_ACCESS_SERVER_ADAPTER_PENDING = "753956dcc6c84a07b4f38acc2c013613";
		public static final String ORPHAN_CHECK_TIME_ALWAYS_ON_IP_PENDING = "4d525a491b6247598d7c44181b63e6ae";
		public static final String DC1000_DEVICE_RESOURCE_READ_COUNT = "feb8698552474dceb2fbd71f6f0b7560";
		public static final String DC1000_FTP_RESPONSE_TIMEOUT = "F4CE0256CF374a7d82195941A6A17213";
		public static final String MINIMUM_RECHECK_QUEUE = "7b2527dbfdce4bc59bb842113e69eb1b";
		public static final String MAXIMUM_RECHECK_QUEUE = "6fbf9f7bbe9f4df1a46be2ce89295442";
		public static final String SCHEDULE_DELTA_LOAD_PROFILE_NOW = "2D0BB098EA4B4d0a9F08EA9606D1996D";
		public static final String DEFAULT_DEVICE_DOWN_LIMIT = "9608981a58474e5c99a1a0cb84085fb2";
		public static final String USE_WAN_COMPRESSION = "F82C9628C46440abAB87A8FB547B4D68";
		public static final String DC1000_MINIMUM_WAN_COMPRESSIBLE_BYTES = "65a05337822341c39bbf136da3fcb69d";
		public static final String ARCHIVE_ROW_LIMIT = "e1dec9f41d9240a5908caf27ecf93a18";
		public static final String DISCONNECT_ON_DCXP_TIMEOUT = "fce47d7588ce4076a45c3a33f26da190";
		public static final String MBUS_ON_DEMAND_BILLING_TIMEOUT_PERIOD = "352BB96D62134e9fA6625FDEFDF79F88";
		public static final String SERVER_PERFORMANCE_LOG_INTERVAL = "8bddff448c924d1d8144a0ca17864553";
		public static final String DAYS_UNTIL_STOP_MODE = "ba90116e638f49669fa5f34ea19efb77";
		public static final String FTP_ENCRYPTED_DIRECTORY_LISTING_POLL_DELAY = "3e23b7baff254b61b5f9c76019cfe921";
		public static final String PROXY_SERVER_HOST_NAME = "05205dbf9e00421c9b66b685b95c1d7d";
		public static final String PROXY_SERVER_PORT = "e77cbe864d054075971b7e8f2a396852";
		public static final String TASK_TIMEOUT_ROW_LIMIT = "7c5a5ff39c3f436680edef291c422c7c";
		public static final String TIME_WAIT_CONNECT_RETRY_CISCO_ACCESS_SERVER_ADAPTER = "2ad853ac737a441597ff4750b1012632";
		public static final String TIME_WAIT_CONNECT_RETRY_RAS = "54EC511FE20E4bffBE055922087B354B";
		public static final String DC1000_REFUSAL_PORT = "ecfc4369cfd946729e48b41087e7bc29";
		public static final String CISCO_ACCESS_SERVER_CONNECTION_DELAY = "e4078ed3809f4cf595f978eeb7c7cb98";
		public static final String TIME_WAIT_CONNECT_RETRY_ALWAYS_ON_IP = "e1712659569f4cefa429566ecaf8da8a";
		public static final String DISCONNECT_DELAY_RAS = "95a4b77e7eca4947a4796648ab231812";
		public static final String READ_METER_LOCAL_DATE_TIME = "a0af9e54378043aea9a7512c5434089d";
		public static final String EVENT_SOAP_CALL_ADDRESS = "7a3405d3ebe2486491cff11bcb7a622d";
		public static final String DC1000_ADAPTER_NON_URGENT_READ_COUNT = "72b14bb18d25435eb12b11ef7a87bb08";
		public static final String SECURITYKEY_INACTIVITY_LIFESPAN = "6DB13F06AE454d7cB054061A250B76A2";
		public static final String HIERARCHY_DELIMITER = "7ADD9A1F555E4f879E2F0A8C6204932A";
		public static final String EMAIL_SENTBY_ADDRESS = "752896D3EA534a6f8814366C97C8EE55";
		public static final String SMTP_SERVER_ADDRESS = "E8890EEF580C44fe8557A2E1F481F91D";
		public static final String SOAP_CALL_TIMEOUT_PERIOD = "7E9C00D0C8664d3eA4C3E5500A21F6C0";
		public static final String TCPIP_SOCKET_TIMEOUT_PEROID = "94417108619144a2881510CC5E4A8458";
		public static final String DATALOG_UPLOAD_SESSION_MAX_LIFETIME = "09AC43CA44A044b188384AE033913ED1";
		public static final String DATALOG_SOAP_RETRIEVE_COUNT = "ca2bd557f2ab46aca620f986326157b4";
		public static final String SUMMARY_KEY_DATAPOINTIDS = "81cc4f8a18e64b308d0f0dd258cb1d21";
		public static final String TIME_WAIT_DEPENDENCY_NOT_FOUND = "38bbc28186104981a359b3c18e7e6a2a";
		public static final String DC1000_DISASTERTIMEOUT = "66688870F9554ee1A3227B0DBDD06E7C";
		public static final String DC1000_RESPONSETIMEOUT = "AAE6926A528D4220822ABF88D8FC2651";
		public static final String DC1000_MAX_OUTSTANDING = "FEC89CF21289495eA455F428327784CA";
		public static final String DC1000_FTPTIMEOUT = "7eed92692f8a4b038b61ef374bc09882";
		public static final String CISCO_ACCESS_SERVER_AUTHENTICATION_ENGINE_SECRET = "BC79C2A6AA5D4046A6E18A82E0F64C66";
		public static final String DC_REFUSAL_PORT = "ecfc4369cfd946729e48b41087e7bc29";
		public static final String CISCO_ACCESS_SERVER_LAST_CONNECTION_ATTEMPT_DATE_TIME = "392749bc81a94576ac65a1c32db9de27";
		public static final String TIME_WAIT_CONNECT_RETRY_ALWAYSONIP = "e1712659569f4cefa429566ecaf8da8a";
		public static final String MAXIMUM_TIME_CONNECT_CISCO_ACCESS_SERVER = "b10dffa5c7bc4ecea09d782a13096066";
		public static final String APP_LVL_AUTH_CHALLENGE_DATA = "187cd1eaed4842a1ac4fcd4238170753";
		public static final String SESSION_SEQUENCE_NUMBER = "03d1326b4d654436905d0c2fa74efd81";
		public static final String FTP_SEQUENCE_NUMBER = "fdbacd47f1214b88bd98a6d964c830ef";
		public static final String DC1000_ADAPTER_LOCATION = "4cf38c4953034ce79ca6fabe98cd1fca";
		public static final String EVENT_SOAP_CALL_URL = "7a3405d3ebe2486491cff11bcb7a622d";
		public static final String EVENT_SOAP_CALL_NAMESPACE = "a0aa36cb53704f01940ea070933b70ff";
		public static final String WINDOWS_RAS_PHONE_BOOK = "D51D39D27E154fd39CD76995EB7E01C0";
		public static final String LAST_METER_HANDLE_USED = "43e75bb5db724684a2e0b7b1c71c5ba9";
		public static final String LAST_PANORAMIX_DOMAIN_ID_USED = "cf2dc39053a44d3da35f3feb6bc0a1f8";
		public static final String CISCO_ACCESS_SERVER_LAST_DATE_TIME_CONNECTION_ATTEMPT = "392749bc81a94576ac65a1c32db9de27";
		public static final String REGISTRATION = "a064392b33a84dcb8a1b1e1fb2f5174d";
	}

	public final class SolutionSettingValueTypes {
		public static final String SETTING_NUMERIC = "129ce55698fa498cb9209becf83e2bcc";
		public static final String SETTING_STRING = "72a077b7c1fc441cbe96552634fb2dbe";
		public static final String SETTING_EMAIL = "fa7402aaf48d465091a60f16e13dce1b";
		public static final String ENCRYPTED_STRING = "eb59d1351cbf4560a9cec2ccac802aca";
	}

	public final class DeviceStatus {
		public static final String ENABLED = "F33C5299FCC14a6198230E1CA7B97AED";
		public static final String ADD_PENDING = "5E96BEFC1BFD4c6cA42CE2F9BF86FA37";
		public static final String REMOVE_PENDING = "34c9333f91984faf9369aed0fb8cf09d";
		public static final String FIRMWARE_UPDATE_PENDING = "6BF14C06561E4300923AB74877EB324A";
	}

	public final class GatewayCommunicationHistoryStatus {
		public static final String ERROR = "6814825A626A46aaAA7F6AE8252AAA08";
		public static final String SOAP_FAILED = "2603FA0E27DD401fAB42FBBBB4281072";
		public static final String CONNECTED = "7E58E836F4A24535BEE97404B4E519B0";
		public static final String PENDING = "fc3f906abbe04110bd760e32fa7e5994";
		public static final String COMPLETE = "020f3a24af6f4f30bb7446e0147e7438";
	}

	public final class GatewayStatus {
		public static final String ENABLED = "6F9EFDF055C04031947FAE0C65814C78";
		public static final String DISABLED = "D1F878A4F5594dd0886B31C9CDAF62CC";
		public static final String FIRMWARE_UPDATE_PENDING = "45B20F36413F4f57BE474C19C3054ABA";
	}

	public final class DataLogRetrievalMethodTypeID {
		public static final String FTP_THEN_SOAP = "51F3232443974c1c9316EA4B8AB2D8F2";
		public static final String SOAP_ONLY = "049D074B92F1417b9407F840722E3839";
	}

	public final class ScheduleAssignmentStatus {
		public static final String ENABLED = "D256F7755E044cdf832631C3BF026CF4";
		public static final String DISABLED = "4F70DCA609D34e099D0310BBA025C381";
	}

	public final class ScheduleStatus {
		public static final String ENABLED = "1621F0AB77604f8c923A553AAC2076BD";
		public static final String DISABLED = "C2E734072C574d08A67FABB6E5D000A0";
	}

	public final class ScheduleRecurrenceTypes {
		public static final String MINUTE = "C46C46455C77431aBE570936306DC4A5";
		public static final String HOUR = "E606F3057B68493bB39B2BC27592BDAF";
		public static final String DAY = "86206BCE754B414eBAF7BFC2900667DC";
		public static final String WEEK = "319DD42D25A1405a94B3DADDE34CABC1";
		public static final String MONTH = "E8E946937A464c1b81F4E2C441E9A107";
		public static final String YEAR = "1C583DD1E1C244528D30714A8144F532";
		public static final String NONE = "082BC98D245E44aa9FB821E49503D20F";
	}

	public final class ArchiveSettingsExpiredIntervalTypes {
		public static final String MINUTE = "0083AF706B93468c96C066F0CE5869C3";
		public static final String HOUR = "794BE94A059D4bce8DC93AEE88E9AE79";
		public static final String DAY = "477AE70FB6984f658187575572696A07";
	}

	public final class ScheduleTimeoutIntervalTypes {
		public static final String MINUTE = "BC661FDB62074409A2B33E96BC8CBD7C";
		public static final String HOUR = "5D5E0B604162464d8ED018691BB64CA6";
		public static final String DAY = "4478D5FAC7684b6fBF022C9B8908F51E";
	}

	public final class ScheduledTasksPendingStatus {
		public static final String AVAILABLE = "B9DFAF8E5A3B4cd098F33422AED35734";
		public static final String PROCESSING = "6A71A54D26B64efb8DED405B8925D385";
		public static final String WAITING = "1D186DA2DB58484697F1A87A360D8083";
		public static final String PAUSED = "f99912a32e344496a7750e1614084e26";
	}

	public final class GatewayTypes {
		public static final String ILON100 = "f4ff86c4263c433ca8352b965985b9f0";
		public static final String DC1000 = "7428ddbc573941f683c28212f8a0a746";
	}

	public final class ReservedIDs {
		public static final String UNASSIGNED_HIERARCHY_LEVEL_MEMBER_ID = "00f2a36c09c244ac87e90f7f0e83878e";
		public static final String UNASSIGNED_ATTRIBUTE_ID = "7a934e71dc574795a12577db22ee86d3";
	}

	public final class HierarchySearchOptions {
		public static final String EQUAL = "c0413dbb7e0b4d15a3a09f7e0efda93d";
		public static final String EQUALORUNDER = "549515bbb3644027a7a56ef4d352bedd";
	}

	public final class DataPointSourceTypeID {
		public static final String RAW = "FD240DE7307B4f9eBFAB29EAB5AB9E0E";
		public static final String AGGREGATE = "5281F3BAE4B54b569AFF27B43421007A";
	}

	public final class TimeZoneDstTypes {
		public static final String NO_DST = "3798357b8ff8450daa66d54c717e1624";
		public static final String UNKNOWN_DST = "8aa83546e7d548f2b760fe1036bc666c";
		public static final String US_DST = "7a8fe89cbf7c4803a042b4671a767b19";
		public static final String EUROPEAN_DST = "e853364bb7274cf6ae62ee60f3fe8afe";
	}

	public final class DataPointRestrictionTypes {
		public static final String ONLY_DATAPOINTS_WITH_VALUES = "A00DEFC923084f6fAF177F31E181AA93";
		public static final String ALL_DATAPOINTS = "E0E720CB95EA46c49463316A6FED4705";
	}

	public final class ExpressionTypes {
		public static final String ACTUAL = "7F5955E03D09451485047DE7870716B5";
		public static final String INTENT = "7AC5339ADEF1406987F7CB9427F6C4B9";
		public static final String DEPENDENCY = "1E3AEE3CAE0743748FD923C682353FF3";
	}

	public final class HierarchyLevelMemberDeleteTypes {
		public static final String AUTO_REASSIGN_CHILDREN = "04235c93ce3e4bbabd93a0fddcea730a";
		public static final String DELETE_DESCENDENTS = "0c0f514eaf824097881899b12ded5fca";
		public static final String MOVE_CHILDREN_TO_NEW_PARENT = "46e53e63e29d4d86b671ec733322f329";
	}

	public final class DataPointCalculationTypes {
		public static final String IGNORE_DEPENDENCIES = "A22ABBCD0F2940988E1CD0E1181A07B5";
		public static final String WAIT_ON_DEPENDENCIES = "C10F8A4502BF43789229673853707948";
		public static final String FAIL_ON_DEPENDENCIES = "C24C9CFC98004ca69F8FFB3F240BE149";
	}

	public final class UserAuthenticationTypes {
		public static final String DEFAULT = "C8D75848B3A9441f8B0165EF75554C0D";
	}

	public final class TaskPriorities {
		public static final int LOW = 10;
		public static final int MEDIUM = 5;
		public static final int MEDIUM_HIGH = 3;
		public static final int HIGH = 1;
	}

	public final class GatewayCommunicationTypes {
		public static final String PPP_WINDOWS_RAS = "1b7d87c1405d4620b9538754b86867c2";
		public static final String ALWAYS_ON_IP = "3c8f6832eceb41deb0834c707af9ad3e";
		public static final String CISCO_ACCESS_SERVER = "51013344C14245dc8E010B3132DE6DEC";
	}

	public final class GatewayTemplateTypes {
		public static final String TEMPLATE_ONLY = "aacfbb5418924690958b826cede639c7";
		public static final String GATEWAY_AND_TEMPLATE = "afc0a772dac647faa4db9c541a38de4f";
		public static final String GATEWAY_ONLY = "d9d8596d0af04b3083822a4bf6d9be43";
	}

	public final class IDTypes {
		public static final String GATEWAY_ID = "444b729f4d7a4b5e90f5296e7c62ed78";
		public static final String TRANSFORMER_ID = "0aadee3706ce479a9fbb769b91ee7cf9";
		public static final String NEURON_ID = "85a93675602b49a5a3ecbc7b66a513db";
		public static final String DEVICE_ID = "7bf34c6d91f64ff4904411b72874ddf9";
		public static final String SERIAL_NUMBER = "8448b06de2f34e838914d50021053ee8";
	}

	public final class DeviceTypes {
		public static final String METER = "fdfac94660b04fdcbdfc399cbb2c743d";
		public static final String ILON100 = "7e31e58fac69427caa8305bb40158c17";
		public static final String MBUS = "ec40abc46a524f478f67f0823e38a558";
	}

	public final class EntityTypes {
		public static final String DATAPOINT = "2f3e88220d0f4deb9de60e545e80d414";
		public static final String DEVICE = "da18b9cdd06e4d2ea31439625431b775";
		public static final String EVENT = "5cf615c6be054f0f890f743c64a42d85";
		public static final String GATEWAY = "6a66c9e80bc64b1e918c65c1787c1e3c";
		public static final String ENGINE = "dbe17830518a48e18904322e8d22506f";
		public static final String SETTING = "dbf2f9b39b4e4dd9b504fe5d5a4196d9";
		public static final String SCHEDULE_ASSIGNMENT = "E3F076689EB84094B6A06FC71EF81214";
		public static final String SCHEDULE = "8800D8DB1841441cB15A3FD1F3D24B52";
	}

	public final class GatewayCommunicationRequestTypes {
		public static final String SERVER_INITIATED_HIGH_PRIORITY = "8262edfc771547a2ad4467e97b2c5479";
		public static final String SERVER_INITIATED_NORMAL_PRIORITY = "ebcec8adecb142c48fb3951818db1fc4";
		public static final String GATEWAY_INITIATED = "a6fbe085fba047a3ad7a1cfcbcd86227";
	}

	public final class CommandHistoryStatus {
		public static final String SUCCESS = "b43637dc8ddb487aabbcf9752556762f";
		public static final String FAILURE = "28b70caf8360420982f4d1ebac66a39c";
		public static final String WAITING = "3dc39216a0a64a19974826f3fec44368";
		public static final String DELETED = "a796f572761a4ea6b3a6cab751a4e8e7";
		public static final String IN_PROGRESS = "7f31e3d98cfb478f8155d3851053a607";
		public static final String CANCELLED = "FA74ABC8031F4ffb9F0C37A9720BB249";
	}

	public final class GatewayDataAvailableTypes {
		public static final String DISCOVERED_DEVICES = "44d5344152694179a78c482e6d81ca7e";
	}

	public final class DeviceCommands {
		public static final String CONNECT_LOAD = "8ff7f8145166477a86afebd1052682ab";
		public static final String DISCONNECT_LOAD = "55c9a443124e42308681eb8ebe17c067";
		public static final String READ_FULL_LOAD_PROFILE = "c060ae65f0a24a74a3aa783e4820b8ad";
		public static final String READ_POWER_QUALITY = "efc57978bcd6481db4df5e267340248d";
		public static final String READ_BILLING_DATA_ON_DEMAND = "876d72f4ae384434a87b61c095ca9037";
		public static final String SET_PRIMARY_MAXIMUM_POWER_LEVEL = "6a84f553420d412c932ac9945753d8e0";
		public static final String SET_MAXIMUM_POWER_LEVEL_DURATION = "c8a34bc2193e47baab32fdc809bec8a8";
		public static final String READ_PRIMARY_MAXIMUM_POWER_LEVEL = "cf9681d07e294b4195736362e7de80e0";
		public static final String READ_MAXIMUM_POWER_LEVEL_DURATION = "bc6ef513d5f3440baf6f25dc1405a93c";
		public static final String SET_TOU_SEASON_SCHEDULES = "95B45DEEE1CC4f0dB945D439E9C245DF";
		public static final String SET_TOU_DAY_SCHEDULES = "EDB47BBCDB5F44989CA460F736232D05";
		public static final String SET_TOU_RECURRING_DATES = "CDBB8E08358F4488A2869D123976F86B";
		public static final String READ_TOU_DAY_SCHEDULES = "36C6E601E4A94c21B819647ACFE5D6DA";
		public static final String READ_TOU_SEASON_SCHEDULES = "6BE158081CEC4444B1B519BAE6AB53BB";
		public static final String READ_TOU_RECURRING_DATES = "27E730D4120D483fB18E961A032EE9A2";
		public static final String SET_PRIMARY_MAXIMUM_POWER_STATUS = "8abfe06564bc4543b04198c9c792238a";
		public static final String READ_SELF_BILLING_DATA = "06790b8c487c41e5a9a2466309fee4c3";
		public static final String READ_SECONDARY_BILLING_REGISTERS = "141da4844e5e44d8a1961506d25ddf28";
		public static final String CONNECT_CONTROL_RELAY = "971dc6e881a14317a5ae4936fdf2734f";
		public static final String DISCONNECT_CONTROL_RELAY = "5137fd076f6c4be69da39c1928d1251d";
		public static final String SET_CONTROL_RELAY_TIERS_STATUS = "44101289ac4749d4924fde9ffbda0edb";
		public static final String SET_CONTROL_RELAY_TIERS = "a47d6f51a1744654a06212116d05d3bb";
		public static final String SET_LOAD_PROFILE_CONFIGURATION = "ce7242bd9b7a4824a3e4ab390b1de400";
		public static final String READ_POWER_QUALITY_OUTAGE_DURATION_THRESHOLD = "D93F4FBEB72A44bdA2D7506B15DED8E2";
		public static final String READ_POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD = "91868BA3ABEE4f01A8DFCE625A6EBF22";
		public static final String READ_POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD = "8DFFC5DA18B54f90B18909D3186F5189";
		public static final String READ_POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD = "1F6A974893314fe0BC160A268A032F50";
		public static final String READ_POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD = "34613E88E4254ea9AE1C516B10C97B7F";
		public static final String SET_POWER_QUALITY_OUTAGE_DURATION_THRESHOLD = "6AC87E7BFFD34f7c853DDC28B41E93FB";
		public static final String SET_POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD = "C8D241398E8D4e1fB17928746402AD85";
		public static final String SET_POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD = "96C130FE709F4598B58F92A69F3823F7";
		public static final String SET_POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD = "7F9B565530E84a1b87680B6CDC037E48";
		public static final String SET_POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD = "42559B73860C440d8F5970F2BFF3D5D3";
		public static final String READ_DELTA_LOAD_PROFILE = "640998221D7545a1ABED17D67008831E";
		public static final String READ_CONTINUOUS_DELTA_LOAD_PROFILE = "7114127AD12B4854A7487B6A617866B0";
		public static final String UPDATE_FIRMWARE = "4DFE4EF6120144fcBB472986848D3C25";
		public static final String READ_DISPLAY_CONFIGURATION = "A125838270E94e70AAF8AD5997219F66";
		public static final String SET_DISPLAY_CONFIGURATION = "ADCBF99F87E04c4bBA1D8B268892728F";
		public static final String SET_ALARM_DISPLAY_CONFIGURATION = "2582A29F0CE3480590FC9E3A58E9B739";
		public static final String READ_PRIMARY_MAXIMUM_POWER_STATUS = "F5A1A390BEF244cc8271A54D88AB2310";
		public static final String READ_PULSE_INPUT_CONFIGURATION = "125875c4075d4673840cf3e85140eaae";
		public static final String READ_INSTANTANEOUS_POWER = "929174F4A6864c00853CA07C25F21A93";
		public static final String READ_CONTROL_RELAY = "a1b3ecb71d4c4eaa914b8cb1b4e4809a";
		public static final String READ_LOAD_STATUS = "00ee66bae6ec4e6e94c474031f9ad171";
		public static final String READ_FIRMWARE_VERSION = "38bf1957518e4b85888634a62534f6e6";
		public static final String SET_PULSE_INPUT_CONFIGURATION = "928b63e248874c0d8992f80189f90fe6";
		public static final String READ_ACTIVE_TOU_CALENDAR = "851007C70FF344799DE53FA72FCC2FFD";
		public static final String READ_PENDING_TOU_CALENDAR = "9169C4DC6417453cA8A5C439537AE038";
		public static final String SET_PENDING_TOU_CALENDAR = "93C5CEDA5BE444d0B277E3CB77C64426";
		public static final String READ_DOWN_LIMIT = "7546196b682e409e8929cc099ab8764a";
		public static final String SET_DOWN_LIMIT = "3850dc63144846cab9cd4f0478c69c49";
		public static final String SET_DATE_TIME = "bfdd8bdc648247febeb8876ce5aaf184";
		public static final String READ_HISTORICAL_BILLING_DATA = "37c6c1b2c957439d9b9ef2bf7473e747";
		public static final String READ_ALARM_POLLING_RATE = "4360a6993e2a480dae781f9e7830f367";
		public static final String SET_ALARM_POLLING_RATE = "9720799680614c4abf305eeabc06be94";
		public static final String READ_BILLING_SCHEDULE = "3E838D9FD4AE42318D1C4F2C2C571C1F";
		public static final String SET_BILLING_SCHEDULE = "0F5CD7D9B7E145828AD50174D7BE1D87";
		public static final String READ_MEP_CARD_CONFIGURATION = "0f6583b94dfa442689e71e08b2956470";
		public static final String READ_STATISTICS = "39d7f2bbce2443cebafc664a169d3163";
		public static final String READ_AUTO_DISCOVERY_CONFIGURATION = "fb4bc6d5429846778601aa7b8c40fcb2";
		public static final String SET_AUTO_DISCOVERY_CONFIGURATION = "46c63c1745594e118138e1eb9a7f7a47";
		public static final String READ_EVENT_LOG = "3514e14b1e9c465cb7a69a329a47db3d";
		public static final String READ_UTILITY_INFORMATION = "ffb823d7cbb7481388be415b8604ba7c";
		public static final String SET_UTILITY_INFORMATION = "a8a9bbd9b2cd43b0814bbc64b0e59208";
		public static final String ADD_PREPAY_CREDIT = "3A7BB83EDB714856B9D3532D4BC5485C";
		public static final String CLEAR_PREPAY_CREDIT = "B8485CC3630B40a99142D80581E2C1E4";
		public static final String READ_PREPAY_CONFIGURATION = "652E05E87D864a488CAF502C6718DEF0";
		public static final String SET_PREPAY_CONFIGURATION = "CA90E67E311A4c95891D7CF8A9F4AB18";
		public static final String READ_TIME_ZONE_CONFIGURATION = "229f94ce3bdb47da98288a08acaf1a50";
		public static final String SET_TIME_ZONE_CONFIGURATION = "345649807dc04c758ca4a795d03f98ee";
		public static final String READ_PREPAY_CREDIT = "da5e63c3e2934be48ffd3da30d8600c6";
		public static final String REMOTE_METER_CONNECT = "8ff7f8145166477a86afebd1052682ab";
		public static final String REMOTE_METER_DISCONNECT = "55c9a443124e42308681eb8ebe17c067";
		public static final String READ_METER_LOAD_PROFILE = "c060ae65f0a24a74a3aa783e4820b8ad";
		public static final String READ_BILLING_DATA = "876d72f4ae384434a87b61c095ca9037";
		public static final String SET_SEASON_SCHEDULES = "95B45DEEE1CC4f0dB945D439E9C245DF";
		public static final String SET_DAY_SCHEDULES = "EDB47BBCDB5F44989CA460F736232D05";
		public static final String RETRIEVE_DAY_SCHEDULES = "36C6E601E4A94c21B819647ACFE5D6DA";
		public static final String RETRIEVE_SEASON_SCHEDULES = "6BE158081CEC4444B1B519BAE6AB53BB";
		public static final String RETRIEVE_TOU_RECURRING_DATES = "27E730D4120D483fB18E961A032EE9A2";
		public static final String ENABLE_MAXIMUM_POWER = "8abfe06564bc4543b04198c9c792238a";
		public static final String PERFORM_SELF_BILLING_READ = "06790b8c487c41e5a9a2466309fee4c3";
		public static final String CLOSE_SECONDARY_CONTROL_OUTPUT = "971dc6e881a14317a5ae4936fdf2734f";
		public static final String OPEN_SECONDARY_CONTROL_OUTPUT = "5137fd076f6c4be69da39c1928d1251d";
		public static final String ENABLE_SECONDARY_CONTROL_OUTPUT_TIERS = "44101289ac4749d4924fde9ffbda0edb";
		public static final String SET_SECONDARY_CONTROL_OUTPUT_TIERS = "a47d6f51a1744654a06212116d05d3bb";
		public static final String READ_POWER_QUALITY_TIME_THRESHOLD = "D93F4FBEB72A44bdA2D7506B15DED8E2";
		public static final String READ_POWER_QUALITY_SAG_THRESHOLD = "8DFFC5DA18B54f90B18909D3186F5189";
		public static final String READ_POWER_QUALITY_SURGE_THRESHOLD = "1F6A974893314fe0BC160A268A032F50";
		public static final String READ_POWER_QUALITY_OVERCURRENT_THRESHOLD = "34613E88E4254ea9AE1C516B10C97B7F";
		public static final String SET_POWER_QUALITY_TIME_THRESHOLD = "6AC87E7BFFD34f7c853DDC28B41E93FB";
		public static final String SET_POWER_QUALITY_SAG_THRESHOLD = "96C130FE709F4598B58F92A69F3823F7";
		public static final String SET_POWER_QUALITY_SURGE_THRESHOLD = "7F9B565530E84a1b87680B6CDC037E48";
		public static final String SET_POWER_QUALITY_OVERCURRENT_THRESHOLD = "42559B73860C440d8F5970F2BFF3D5D3";
		public static final String UPDATE_METER_FIRMWARE = "4DFE4EF6120144fcBB472986848D3C25";
		public static final String READ_METER_DISPLAY_CONFIGURATION = "A125838270E94e70AAF8AD5997219F66";
		public static final String SET_METER_DISPLAY_CONFIGURATION = "ADCBF99F87E04c4bBA1D8B268892728F";
		public static final String READ_MAXIMUM_POWER_ENABLE = "F5A1A390BEF244cc8271A54D88AB2310";
		public static final String READ_SECONDARY_CONTROL_OUTPUT_RELAY = "a1b3ecb71d4c4eaa914b8cb1b4e4809a";
		public static final String READ_PRIMARY_CONTROL_OUTPUT = "00ee66bae6ec4e6e94c474031f9ad171";
		public static final String READ_METER_FIRMWARE_VERSION = "38bf1957518e4b85888634a62534f6e6";
		public static final String GET_LOAD_PROFILES = "38A0EAD083E5489a9A8A1E50F6F84D4F";
		public static final String READ_PREPAY_MAXIMUM_POWER_STATUS = "696bf1f6eeee44d2831ff41f09ed097f";
		public static final String SET_PREPAY_MAXIMUM_POWER_STATUS = "474045f96d1f4b9fb0e79e7ec9b454e5";
		public static final String READ_PREPAY_MAXIMUM_POWER_LEVEL = "d26849dfc53d4d208cdc64e46be42a45";
		public static final String SET_PREPAY_MAXIMUM_POWER_LEVEL = "3c69304af03f48bfa2d82f9fbcea9ce8";
		public static final String SET_MAXIMUM_POWER_LEVEL = "6a84f553420d412c932ac9945753d8e0";
		public static final String READ_MAXIMUM_POWER_LEVEL = "cf9681d07e294b4195736362e7de80e0";
		public static final String SET_MAXIMUM_POWER_STATUS = "8abfe06564bc4543b04198c9c792238a";
		public static final String READ_MAXIMUM_POWER_STATUS = "F5A1A390BEF244cc8271A54D88AB2310";
	}

	public final class GatewayCommands {
		public static final String UPDATE_FIRMWARE = "23c147b71a91412488147ac6213bf7e3";
		public static final String SET_PPP_USERNAME = "c45d90fb3cb943878db6fe5a4c5158be";
		public static final String SET_PPP_PASSWORD = "2bb14b677e0f421f9162908f08271661";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_INIT_STRING = "d4f1d34bffef48b89bc21aaeb5fb9c25";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_TYPE = "56fc08b83b76416495ed68e48a265438";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_1 = "1434818290d04270a108d7e15e29f911";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_2 = "2f010587b2714d3892acc38f5b35c41f";
		public static final String READ_GATEWAY_TO_SERVER_IP_ADDRESS = "09897dc31b504a34835cf68ce5933a04";
		public static final String SET_GATEWAY_TO_SERVER_IP_ADDRESS = "38dfce8925934314a7d4b17409779917";
		public static final String SET_IP_ADDRESS = "4d854628421046919135dbc1379dc5b2";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_1 = "ffaea45d1072471fbdd0d5ed1aa0b2f8";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_2 = "ffeba0d6cabd4dbb8630fcaaf8d82e9a";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_TYPE = "3e42ca2578f44ad5a038867b0bb11a17";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_INIT_STRING = "d257f4ae90354cf2a70186c7a79f6eea";
		public static final String READ_STATISTICS = "d60e2a94ab694a5c82c69868c1dd7c38";
		public static final String SET_TOTAL_ENERGY_STATUS = "de31bda71ed74c60a8b14b5612520bb8";
		public static final String SET_SECURITY_OPTIONS = "9cf6934e44bf42ca9589a2d98045df70";
		public static final String SET_WAN_PHONE_NUMBER = "c990d4e48b5d4001b7b6413c7e7ba25e";
		public static final String READ_FIRMWARE_VERSION = "b6722ae4913148b6bf41c02b7ffec72a";
		public static final String BROADCAST_DISCONNECT_CONTROL_RELAY = "b7f1b9801f6542fe807b573cc688dc81";
		public static final String BROADCAST_CONNECT_CONTROL_RELAY = "00d34196af4a4e5198ff9b280d0e40a0";
		public static final String BROADCAST_SET_PRIMARY_MAXIMUM_POWER_STATUS = "18e8884fbbe649ca8e628d2dd7009fed";
		public static final String READ_REPEATER_PATHS = "3553acb885b9472fb3d28cc4d3172f9b";
		public static final String REBOOT = "7e9fab7db6de4fc6827a023fdb0679ab";
		public static final String DELETE_WAN_CONFIGURATION = "a2995515fd444506a55f4e4f823103e5";
		public static final String SET_PROCESS_CONFIGURATION = "0b72299e12db498182d168858fb4e305";
		public static final String READ_PROCESS_CONFIGURATION = "7ecdec9d1e0b4c1ebccbc378f95b8336";
		public static final String READ_DISCOVERED_DEVICES = "a78acc8d74404a23b213a9ae64145927";
		public static final String READ_WAN_CONFIGURATION = "59149781e9b34c16b2cff5440078beff";
		public static final String SET_WAN_CONFIGURATION = "346A2B7AF85D49b6A9FCBEFB46282B30";
		public static final String SET_DEVICE_LIMIT = "532554d783be46f48b5c6360cb59ee42";
		public static final String READ_DEVICE_LIMIT = "4ef8338f94c044f381a91207a4adb237";
		public static final String CHANGE_GATEWAY_PHONE_NUMBER = "c990d4e48b5d4001b7b6413c7e7ba25e";
		public static final String UPDATE_GATEWAY_FIRMWARE = "23c147b71a91412488147ac6213bf7e3";
		public static final String CHANGE_PPP_LOGIN = "c45d90fb3cb943878db6fe5a4c5158be";
		public static final String CHANGE_PPP_PASSWORD = "2bb14b677e0f421f9162908f08271661";
		public static final String SET_SERVER_TO_GATEWAY_IP_ADDRESS = "4d854628421046919135dbc1379dc5b2";
		public static final String RETRIEVE_GATEWAY_PERFORMANCE_STATISTICS = "d60e2a94ab694a5c82c69868c1dd7c38";
		public static final String ENABLE_TOTAL_ENERGY = "de31bda71ed74c60a8b14b5612520bb8";
		public static final String SET_SERVER_TO_GATEWAY_PHONE_NUMBER = "c990d4e48b5d4001b7b6413c7e7ba25e";
		public static final String READ_GATEWAY_FIRMWARE_VERSION = "b6722ae4913148b6bf41c02b7ffec72a";
		public static final String BROADCAST_OPEN_SECONDARY_CONTROL_OUTPUT = "b7f1b9801f6542fe807b573cc688dc81";
		public static final String BROADCAST_CLOSE_SECONDARY_CONTROL_OUTPUT = "00d34196af4a4e5198ff9b280d0e40a0";
		public static final String BROADCAST_MAXIMUM_POWER_LEVEL_ENABLE = "18e8884fbbe649ca8e628d2dd7009fed";
		public static final String SET_PHONE_NUMBER = "c990d4e48b5d4001b7b6413c7e7ba25e";
		public static final String BROADCAST_SET_MAXIMUM_POWER_LEVEL_STATUS = "18e8884fbbe649ca8e628d2dd7009fed";
	}

	public final class DC1000EventTypes {
		public static final String HARDWARE_FAILURE = "e47ae463688245fca552ddfabeeda394";
		public static final String SOFTWARE_FAILURE = "f847423354bc48e39ca3c3b82d5a10ba";
		public static final String PLANNED_REBOOT = "98ac824aac3f41c9a242b4a2c27dc523";
		public static final String UNPLANNED_REBOOT = "deb8a63238d746c1af343740f2ff0b19";
		public static final String DEVICE_ALARM = "baf6e2cee374487fb2eb43a234c47f53";
		public static final String DEVICE_CLOCK_ERROR = "049eadf50b674e59b0aecb1247ea48a3";
		public static final String DC_TO_METER_COMMUNICATION_STATUS = "a1d05a2738ca4723b2043a79cb4926a6";
		public static final String DEVICE_NACK = "07a8e71726af45159c725051c0a748b6";
		public static final String DEVICE_PHASE_CHANGE = "3c5eca8e3f1349668537959906b391c7";
		public static final String DEVICE_PHASE_INVERSION = "e4b21f8155fa45cba984a68149ea8234";
		public static final String SEGMENT_DOWN = "fe2dba2e51c3434f87f422c12ca965cb";
		public static final String RESOURCE_EXHAUSTION = "40c6b322ec094ceda9ad99a491a7bbff";
		public static final String COMMUNICATION_FAILURE = "b61349547e5a4c74bbe2e124632da3a0";
		public static final String SERVICE_TOOL_CONNECT = "8bb8b2ebc5ae4b1b988f89e28f0a6651";
		public static final String FLASH_LOW = "90b388e5bbfa402c83f07c52a0d318d3";
		public static final String FUNCTION_DONE = "d995608f764c4eed8c47852c30579eb6";
		public static final String SECURITY_EXCEPTION = "9dd32d8f2fcc470ea7b0413fefe1da5c";
		public static final String CA_CERTIFICATE_EXPIRATION = "f366851d89a340f8a9840f5c14395ee0";
		public static final String DC_CERTIFICATE_EXPIRATION = "cb1615cb8f9945c3ab0c50ff2b253014";
		public static final String RSA_KEY_GENERATED = "4f32c12b91854d4aa1d45a08ae75e8dd";
		public static final String DEVICE_DOWN = "a1d05a2738ca4723b2043a79cb4926a6";
	}

	public final class DeviceEvents {
		public static final String ADD_SUCCESS = "bffeafeef0964980926e5e0fc81cb55a";
		public static final String ADD_FAILURE = "e895295532644b899216a2d647da6eb8";
		public static final String MOVE_SUCCESS = "03894AB5B80C4530AE39C256C8EAF735";
		public static final String MOVE_FAILURE = "297F815EFC044713AEF36D994194D296";
		public static final String MOVE_CANCELLED = "751EABB3A118478e8225FD22CD805ED1";
		public static final String ADD_CANCELLED = "90B7406A272A40b8A454282A7EF171A8";
		public static final String REMOVE_SUCCESS = "fb1eb87c9def4190a121aaa2995627f5";
		public static final String REMOVE_FAILURE = "5da8b450b7dd4cb2a982539934a4a2c6";
		public static final String END_OF_BILLING_CYCLE_BILLING_DATA_AVAILABLE = "3b29e905f9764169a999cfc8ad527d2e";
		public static final String READ_BILLING_DATA_ON_DEMAND_COMMAND_COMPLETE = "f309efb1263c4706ac8c1e3a18ec9da9";
		public static final String READ_FULL_LOAD_PROFILE_COMMAND_COMPLETE = "f33490cf1e484c4d817d27b125b1031f";
		public static final String POWER_QUALITY_COMMAND_COMPLETE = "10becfacb599459cacc920cb4fc6bce5";
		public static final String CONNECT_LOAD_COMMAND_COMPLETE = "4bea21adc4474f5fa7f9e412b74d0d11";
		public static final String DISCONNECT_LOAD_COMMAND_COMPLETE = "416517694b9a482f87c5b81be3fee8fa";
		public static final String SET_PRIMARY_MAXIMUM_POWER_LEVEL_COMMAND_COMPLETE = "93af3eebb36742a89d6a12cd9e7274b8";
		public static final String SET_MAXIMUM_POWER_LEVEL_DURATION_COMMAND_COMPLETE = "95e559c12e914ce6be7d199ad9f63c69";
		public static final String SET_TOU_SEASON_SCHEDULES_COMMAND_COMPLETE = "8F11B342841D404a91883626FF3D2704";
		public static final String SET_TOU_RECURRING_DATES_COMMAND_COMPLETE = "F039C7FDC7204c31A576C9AFC24BD951";
		public static final String SET_TOU_DAY_SCHEDULES_COMMAND_COMPLETE = "B5FDEFDF88E348f0824E329778BA78EF";
		public static final String READ_TOU_DAY_SCHEDULES_COMMAND_COMPLETE = "270827C2F082486f81916DE63876D4AE";
		public static final String READ_TOU_SEASON_SCHEDULES_COMMAND_COMPLETE = "9A9A5B023D5C41da9AF0506B271E9CF7";
		public static final String READ_TOU_RECURRING_DATES_COMMAND_COMPLETE = "1AD9795CD5524895AD9B013570CA2ED9";
		public static final String READ_PRIMARY_MAXIMUM_POWER_LEVEL_COMMAND_COMPLETE = "164fb5c6de3546eba985de5d71e984a9";
		public static final String READ_MAXIMUM_POWER_LEVEL_DURATION_COMMAND_COMPLETE = "439b97c9dfdc499289128038ec83985e";
		public static final String SET_PRIMARY_MAXIMUM_POWER_STATUS_COMMAND_COMPLETE = "2bbb57575b8d49e980f1837e9682cc75";
		public static final String READ_SELF_BILLING_DATA_COMMAND_COMPLETE = "0a87ae5fbef54616a74faa3a653250ba";
		public static final String READ_SECONDARY_BILLING_REGISTERS_COMMAND_COMPLETE = "63b3fe0b77e841259d412763b349e2ff";
		public static final String DISCONNECT_CONTROL_RELAY_COMMAND_COMPLETE = "a1a15cdcb98546fb96c6564a288b930e";
		public static final String CONNECT_CONTROL_RELAY_COMMAND_COMPLETE = "d5e307213a2f4452870ffd12ab1bf937";
		public static final String SET_CONTROL_RELAY_TIERS_STATUS_COMMAND_COMPLETE = "9469ea5f72fe451c97a7f58690d57ebe";
		public static final String SET_CONTROL_RELAY_TIERS_COMMAND_COMPLETE = "ca3a8bb6fbc14db9971f2824b0cbbbf2";
		public static final String SET_LOAD_PROFILE_CONFIGURATION_COMMAND_COMPLETE = "18e64ad80a2e4f3691b1065b40c6d3e4";
		public static final String READ_POWER_QUALITY_OUTAGE_DURATION_THRESHOLD_COMMAND_COMPLETE = "DF520B4557734f9e93120044480063A2";
		public static final String READ_POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD_COMMAND_COMPLETE = "2F24531525494369B2C2D75DD7ACF695";
		public static final String READ_POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD_COMMAND_COMPLETE = "CD02921B850748a18A463D24685FA3D6";
		public static final String READ_POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD_COMMAND_COMPLETE = "AF1653E211064c4c821DC5F547D6337F";
		public static final String READ_POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD_COMMAND_COMPLETE = "50A432F5323B4c1cA0B0F064791BA45C";
		public static final String SET_POWER_QUALITY_OUTAGE_DURATION_THRESHOLD_COMMAND_COMPLETE = "A2CE552EAD2C4a3cBA84B54CCAAD5AC1";
		public static final String SET_POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD_COMMAND_COMPLETE = "9554BBE2CD9F464dA9A4F038B794B0F9";
		public static final String SET_POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD_COMMAND_COMPLETE = "9AD8E6F84A1A40ccB5344A0D56B46776";
		public static final String SET_POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD_COMMAND_COMPLETE = "F3DFBF4C5BE340a0BE795431B0EC375A";
		public static final String SET_POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD_COMMAND_COMPLETE = "EF4DAD2B3E264e048D95718055531DA1";
		public static final String READ_DELTA_LOAD_PROFILE_COMMAND_COMPLETE = "8C55269E57A44edf88609794260AD857";
		public static final String READ_CONTINUOUS_DELTA_LOAD_PROFILE_COMMAND_COMPLETE = "AAC59D59669C4e7f9AC4E63782BD9F98";
		public static final String UPDATE_FIRMWARE_COMMAND_COMPLETE = "4da60cb36956422086892e0baa077ac3";
		public static final String CONTINUOUS_DELTA_LOAD_PROFILE_DATA_AVAILABLE = "E73928E49A194259A6C9BA5C987CF3B7";
		public static final String READ_PULSE_INPUT_CONFIGURATION_COMMAND_COMPLETE = "6dc87cd602ff46fa920a072ee584e5e4";
		public static final String READ_INSTANTANEOUS_POWER_COMMAND_COMPLETE = "A5DB38E87B554ebe82058B4EB38CB58C";
		public static final String READ_CONTROL_RELAY_COMMAND_COMPLETE = "8895461b1adf4aa6ad538c22aec447ff";
		public static final String READ_LOAD_STATUS_COMMAND_COMPLETE = "9cd46282124b434499f7e02bf6d5a8d8";
		public static final String READ_FIRMWARE_VERSION_COMMAND_COMPLETE = "194325c249b94c28a2125797d5acdf2f";
		public static final String SET_PULSE_INPUT_CONFIGURATION_COMMAND_COMPLETE = "1e2a6435c4d0467ea01b1e7868708b92";
		public static final String READ_ACTIVE_TOU_CALENDAR_COMMAND_COMPLETE = "AC8EB7183720417fA5083824F5336C39";
		public static final String READ_PENDING_TOU_CALENDAR_COMMAND_COMPLETE = "71887A7C756C42bdA83ACAB49FC099D8";
		public static final String SET_PENDING_TOU_CALENDAR_COMMAND_COMPLETE = "07660D8DEB4F427c9B4FF53E4E83DD41";
		public static final String READ_DISPLAY_CONFIGURATION_COMMAND_COMPLETE = "737FFF081E4D47199D1350B354B43573";
		public static final String SET_DISPLAY_CONFIGURATION_COMMAND_COMPLETE = "AF23E940CC134d45A0C6501CA4D44B0C";
		public static final String SET_ALARM_DISPLAY_CONFIGURATION_COMMAND_COMPLETE = "6D0A1C1D0C2E45e5BF8C87A383090EE4";
		public static final String READ_PRIMARY_MAXIMUM_POWER_STATUS_COMMAND_COMPLETE = "BBE31E1ECE9A44cc91BC66BA9AD89DEA";
		public static final String READ_DOWN_LIMIT_COMMAND_COMPLETE = "2f10429d091a4923963f2f1fc427fb3e";
		public static final String SET_DOWN_LIMIT_COMMAND_COMPLETE = "90cd985936a44233892b2f779171a849";
		public static final String SET_DATE_TIME_COMMAND_COMPLETE = "74d6144653f548e1aeeb70b0cc6a7eef";
		public static final String UNEXPECTED_DELTA_LOAD_PROFILE_AVAILABLE = "a50cb1ae1a5a4c81a5c0f428164d1fda";
		public static final String UNEXPECTED_FULL_LOAD_PROFILE_AVAILABLE = "e29cf712f35e4bc8b183ad3fffd0a869";
		public static final String READ_HISTORICAL_BILLING_DATA_COMMAND_COMPLETE = "4277c41e1a4445b385f00821bd0e1cce";
		public static final String TEMP_DOWN = "84965a6c5ca6447c8983271372d4dad9";
		public static final String PHASE_INACCURACY = "648c506071df4f1984f8d5626226a86a";
		public static final String PHASE_ROTATION = "a152064cf7f54c6290cc9c4e5e209ddd";
		public static final String PREPAY_CREDIT_EXHAUSTED = "a225ba7c5a894589be61aeef58592e9b";
		public static final String PREPAY_WARNING_ACKNOWLEDGEMENT = "dce1d1c4935f40b3a07cf417f41b21ce";
		public static final String READ_ALARM_POLLING_RATE_COMMAND_COMPLETE = "51e41cd6f3344f2aac0cc70204e5fdd5";
		public static final String SET_ALARM_POLLING_RATE_COMMAND_COMPLETE = "fa06b45f85c947e4b5c41aebee2adc00";
		public static final String READ_BILLING_SCHEDULE_COMMAND_COMPLETE = "2E8E13CD32E746d988FC0EB53A20B23E";
		public static final String SET_BILLING_SCHEDULE_COMMAND_COMPLETE = "C00896213B55412f8710DEA77D6F369E";
		public static final String READ_MEP_CARD_CONFIGURATION_COMMAND_COMPLETE = "51fbdf80ca674156aff61f1fc5d67cea";
		public static final String MEP_CARD_INSTALLATION_OR_REMOVAL = "581b5ac07e474811890becb076a8ffeb";
		public static final String MEP_AUTO_DISCOVERY = "faaf691349c54630af1f91f1275cc97a";
		public static final String MBUS_BILLING_READ_OVERFLOW_OCCURRED = "81f436cdb03a42478cab94e128b17986";
		public static final String MBUS_FAILED_COMMUNICATIONS_ON_READ = "3f3a1abce12d4ad78e5095186e685445";
		public static final String MBUS_BILLING_READ_SERIAL_NUMBER_MISMATCH = "2d019ae40d7842a7839b837748369590";
		public static final String MBUS_DEVICE_ALARMS = "aebf13a8eabe451a892de6bf67c1263e";
		public static final String UNEXPECTED_END_OF_CYCLE_BILLING_DATA_AVAILABLE = "8aed0a9a60a9461f954fba8341c53eb9";
		public static final String READ_STATISTICS_COMMAND_COMPLETE = "933d0223e4e7464bb911b81791336773";
		public static final String READ_AUTO_DISCOVERY_CONFIGURATION_COMMAND_COMPLETE = "153ebbb9029f4a2e81808e99ecbd1432";
		public static final String SET_AUTO_DISCOVERY_CONFIGURATION_COMMAND_COMPLETE = "9a2909fcd21645eeacd2dcfb63c5f24c";
		public static final String READ_EVENT_LOG_COMMAND_COMPLETE = "63fa52aad137434c91b8f957c01a13d3";
		public static final String EVENT_LOG_PENDING_OVERFLOW = "a0b97660e3544be7beb0f84bb3e8be44";
		public static final String READ_UTILITY_INFORMATION_COMMAND_COMPLETE = "0982e6400da0461984b37db55e6c6b30";
		public static final String SET_UTILITY_INFORMATION_COMMAND_COMPLETE = "1a7da8013994477681b5ef8cff7d0711";
		public static final String ADD_PREPAY_CREDIT_COMMAND_COMPLETE = "431CD8BB28B1462c946BD70AC24F0B4C";
		public static final String CLEAR_PREPAY_CREDIT_COMMAND_COMPLETE = "DB3A53F6467C4fc2972BF6BB043EC0DA";
		public static final String READ_PREPAY_CONFIGURATION_COMMAND_COMPLETE = "9B1734FEDF4D4cdf8F522B5256167816";
		public static final String SET_PREPAY_CONFIGURATION_COMMAND_COMPLETE = "84BD2180D04D4c7c94BC2F4095545F87";
		public static final String READ_TIME_ZONE_CONFIGURATION_COMMAND_COMPLETE = "3cdf51589c65472ab7bbd9fb76cb6469";
		public static final String SET_TIME_ZONE_CONFIGURATION_COMMAND_COMPLETE = "a5ec927c2e0d4132984ede2ed280eebc";
		public static final String READ_PREPAY_CREDIT_COMMAND_COMPLETE = "6df8c7e3885346879d806e27a6f225de";
		public static final String AUTO_TEST_POINT_STATUS = "0c5d543d093849fab30dc0d16326a4c5";
		public static final String LOAD_PROFILE_OVERFLOW = "3acc6522fcb342748773009d412cd0a2";
		public static final String LOAD_DISCONNECT_STATUS_CHANGE = "8c61b51eeaad414cae2e8a40c758debc";
		public static final String CONTROL_RELAY_ACTIVATED = "aec6c8c2237e469aa0d1081f18486354";
		public static final String LOAD_PROFILE_BACKFILL_FAILED = "a3c4c5e34c62484aaba533c16e5b7b0c";
		public static final String POWER_QUALITY_EVENT_DETECTED = "5830f4de931a434dadd080c011756ae5";
		public static final String UNREAD_EVENT_LOG_ENTRY_EXISTS = "811eb4f6f85e4870bc86dfe9285d6a67";
		public static final String READ_PREPAY_MAXIMUM_POWER_STATUS_COMMAND_COMPLETE = "a1bb59286c5d4747885e33b4cebb59fd";
		public static final String SET_PREPAY_MAXIMUM_POWER_STATUS_COMMAND_COMPLETE = "8dc4416aec64455f818899855adfd318";
		public static final String READ_PREPAY_MAXIMUM_POWER_COMMAND_COMPLETE = "b45bc0a6c43e40a4b427b0f9ae67070e";
		public static final String SET_PREPAY_MAXIMUM_POWER_COMMAND_COMPLETE = "c70f0449741c4c158426f6a059fc4f9b";
		public static final String SET_MAXIMUM_POWER_LEVEL_COMMAND_COMPLETE = "93af3eebb36742a89d6a12cd9e7274b8";
		public static final String READ_MAXIMUM_POWER_LEVEL_COMMAND_COMPLETE = "164fb5c6de3546eba985de5d71e984a9";
		public static final String SET_MAXIMUM_POWER_STATUS_COMMAND_COMPLETE = "2bbb57575b8d49e980f1837e9682cc75";
		public static final String READ_MAXIMUM_POWER_STATUS_COMMAND_COMPLETE = "BBE31E1ECE9A44cc91BC66BA9AD89DEA";
	}

	public final class GatewayEvents {
		public static final String CONNECTION_ESTABLISHED = "16e7a62c458c48189d4ce6249264512d";
		public static final String CONNECTION_RELEASED = "cb9678d369b547d8bf4ee51e6df81455";
		public static final String TOTAL_ENERGY_DATA_AVAILABLE = "a0e09a0454e04a89bceb469ef18b2348";
		public static final String SET_PPP_USERNAME_COMMAND_COMPLETE = "BCE91DAD62804a4e8824E7C3433A269E";
		public static final String SET_PPP_PASSWORD_COMMAND_COMPLETE = "5B4D3E50144D41f68EA1EAFC4EC32F21";
		public static final String UPDATE_FIRMWARE_COMMAND_COMPLETE = "B2F41EB2832744a3AB03B2C1DFD48082";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_INIT_STRING_COMMAND_COMPLETE = "acf0ea51d03d4f2f9220ceffc82ced3f";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_TYPE_COMMAND_COMPLETE = "469eef112d9c43f79d0debbceb4240ac";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_1_COMMAND_COMPLETE = "c63e377e9c0b4459b03f0afd60892004";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_2_COMMAND_COMPLETE = "1640aabde50f4121bf8939bb3f0b7640";
		public static final String READ_GATEWAY_TO_SERVER_IP_ADDRESS_COMMAND_COMPLETE = "2dd2f06283424a749fa4eeead0d54771";
		public static final String SET_GATEWAY_TO_SERVER_IP_ADDRESS_COMMAND_COMPLETE = "bcc89beebfc54943abae9ee1fd51b696";
		public static final String SET_IP_ADDRESS_COMMAND_COMPLETE = "c452c6f1b95840b88a9a8f6ae1b72e52";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_1_COMMAND_COMPLETE = "0a6d540769554399aaa1022df954e6ee";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_2_COMMAND_COMPLETE = "0e71c1e80add4e049e239d49d23654cd";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_TYPE_COMMAND_COMPLETE = "518a7ae6cf3f487690fbecb284426909";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_INIT_STRING_COMMAND_COMPLETE = "8ed0a80aa9774f578479c0e6b5d2da89";
		public static final String READ_STATISTICS_COMMAND_COMPLETE = "cd091b7129aa410e987d00ee4a5e9f94";
		public static final String SET_TOTAL_ENERGY_STATUS_COMMAND_COMPLETE = "7da4d95936df4d5b8951828c562ce7b9";
		public static final String SET_WAN_PHONE_NUMBER_COMMAND_COMPLETE = "338D71EE6D224a10AEA6142A9A3131E0";
		public static final String SET_SECURITY_OPTIONS_COMMAND_COMPLETE = "35a1980a0f574936b10d53df9bcf0a17";
		public static final String BROADCAST_DISCONNECT_CONTROL_RELAY_COMMAND_COMPLETE = "232cb0c5b7b74916b84e559d4f4c0f1f";
		public static final String BROADCAST_CONNECT_CONTROL_RELAY_COMMAND_COMPLETE = "7a056e9329c2425aba609006727f34f9";
		public static final String BROADCAST_SET_PRIMARY_MAXIMUM_POWER_STATUS_COMMAND_COMPLETE = "73fe67b9e6a74b58a3c2e7ee28aece07";
		public static final String READ_FIRMWARE_VERSION_COMMAND_COMPLETE = "022d4e1cbdaa4890b509030354d5f67b";
		public static final String GENERAL_FAILURE_DATA_STILL_AVAILABLE = "ba19e6ab3fec45abac093bb7035ebbc1";
		public static final String READ_REPEATER_PATHS_COMMAND_COMPLETE = "4505d8b112754f2e8f4731313836116d";
		public static final String REBOOT_COMMAND_COMPLETE = "8b2aa26e527b49afa48134cc4115f8d3";
		public static final String DELETE_WAN_CONFIGURATION_COMMAND_COMPLETE = "03f9796464f94bb499145b04491267d3";
		public static final String CONNECTION_FAILURE = "d95cfb52576d4f8f83b37dfff53bffa3";
		public static final String INVALID_IP_ADDRESS = "306af8ec1a0f4d08a58d9d6b7af6497c";
		public static final String MESH_STATUS_DETECTED = "3def0023c2db41cb9d50785d215fc2f4";
		public static final String READ_PROCESS_CONFIGURATION_COMMAND_COMPLETE = "9eede710db664a8a9a01e5f00cdf72b8";
		public static final String SET_PROCESS_CONFIGURATION_COMMAND_COMPLETE = "b31068b8bd6a418d8771080613d70b05";
		public static final String READ_DISCOVERED_DEVICES_COMMAND_COMPLETE = "0f6dc335fe914305ba859b2d35e58433";
		public static final String NEW_DISCOVERED_DEVICES = "397d0d21d5a8484d8e96ad9d60fe1699";
		public static final String NEW_ORPHANED_DEVICES = "a12caef6cafc4c09beb3a1cfcf44db9e";
		public static final String READ_WAN_CONFIGURATION_COMMAND_COMPLETE = "eeefd82d143c4bd598a55eb933a81ac6";
		public static final String WAN_CONFIGURATION_MISMATCH = "31a263a35ada458484b0ad5d5e6fc54a";
		public static final String SET_WAN_CONFIGURATION_COMMAND_COMPLETE = "F27911244AA741899A548521C41D46A3";
		public static final String CAPACITY_CHANGED = "24f9be63b4d1419395abd829779287c4";
		public static final String WAN_FAILURE = "78469a992dbd4323b93b2ab6626e88b6";
		public static final String WAN_SWITCH = "ff3888ccda0f4f1696f2107e225ba4aa";
		public static final String UNANSWERED_CALL_ALERT = "2f6fc88d7b084da8ab2acd6f8bb6c434";
		public static final String SET_DEVICE_LIMIT_COMMAND_COMPLETE = "d457b5109ee04427b4f96164f8f544c1";
		public static final String READ_DEVICE_LIMIT_COMMAND_COMPLETE = "3bd1689a40a942c1ba2a9c1c6be29fb1";
		public static final String UNKNOWN_CONNECTION = "0d68e424019e41dcbdd75677728b798c";
		public static final String IP_ADDRESS_CHANGED = "72dcafb9a1df45ae8beec1d1cfa7285f";
		public static final String SET_PHONE_NUMBER_COMMAND_COMPLETE = "338D71EE6D224a10AEA6142A9A3131E0";
		public static final String BROADCAST_SET_MAXIMUM_POWER_LEVEL_STATUS_COMMAND_COMPLETE = "73fe67b9e6a74b58a3c2e7ee28aece07";
	}

	public final class SystemEvents {
		public static final String COMMAND_FAILURE = "7bd38a557c4641d7bebe59edbb328064";
		public static final String DC1000_EVENT = "748af8ef281c4428b0e3f8cc2d8fe0cf";
		public static final String ENGINE_STARTED = "a89ab0bab7344d26bc4236a0d2c309e1";
		public static final String ENGINE_STOPPED = "11d73ac3f2bb4093b447ba68f5266d8b";
		public static final String SECURITY_ALERT = "3745625CAFA24ad989472607AB0A28E0";
		public static final String SERVER_ERROR = "e3501923ef634306b0c5592ff53b2f00";
		public static final String RECOVER_ENGINES_COMMAND_COMPLETE = "8b41562cb08649ffa2bb780bba38bca6";
		public static final String GATEWAY_IP_ADDRESS_CHANGED = "72dcafb9a1df45ae8beec1d1cfa7285f";
	}

	public final class ProcessConfigurationStatus {
		public static final int ENABLED = 0;
		public static final int DISABLED = 1;
	}

	public final class ApplicationLevelAuthenticationStatusTypes {
		public static final String ENABLED = "dadc479ad4f646eb9028e9f3f0a75bbb";
		public static final String DISABLED = "e8681b9c315d4ef6bb06370684867c1c";
	}

	public final class GatewayEncryptionStatusTypes {
		public static final String ENABLED = "33168a0ef643440fab780af1541f9664";
		public static final String DISABLED = "cca636daca974106a9cef938f48cfa5d";
	}

	public final class GatewayWANConfigurationStatusTypes {
		public static final String ENABLED = "6994d3fecb6243eb8e65e1f3801b4538";
		public static final String DISABLED = "4c0662cc8ba04698914e6e9c6e8521d4";
	}

	public final class GatewayOutboundCHAPStatusTypes {
		public static final String ENABLED = "b310ae6f5520447aafc26fccd984c800";
		public static final String DISABLED = "6f9abc0646b140d88903dc7f4c0036bb";
	}

	public final class GatewayInboundCHAPStatusTypes {
		public static final String ENABLED = "848d52e083b1422997e1524712e883ba";
		public static final String DISABLED = "8769e901729c47b7bf48a63b3c2a8c24";
	}

	public final class GatewayPAPStatusTypes {
		public static final String ENABLED = "6183031e351e46c8bcda3274aba9235c";
		public static final String DISABLED = "8716bb2a316945e486da007b528fbc12";
	}

	public final class GatewayPortSpeedTypes {
		public static final String SLOW = "a14b7e23fa2e44e2b15020e09c92b55a";
		public static final String FAST = "2262d756893248f0a1f0b4f8632de95b";
	}

	public final class GatewayModemCommandTypes {
		public static final String AT_COMMANDS = "f415d58490b9419b8a572cae8cabce32";
		public static final String DIRECT_CONNECTION = "b6aa5e29421749b5bdf6203abc7d8dca";
	}

	public final class GatewayModemConnectTypes {
		public static final String TEMPORARY = "bac326ec2c974c34838511c8b23354de";
		public static final String PERMANENT = "8cf551f390794a0781cf51db5c1bded1";
	}

	public final class GatewayPPPConnectionTypes {
		public static final String NO_RECONNECT = "4b9a3ed202244468a5e7308c7d1c4509";
		public static final String RECONNECT = "847e0a6e585c4fbdaba9ebb77d5816b4";
	}

	public final class GatewayModemAnswerTypes {
		public static final String AUTO_ANSWER = "265884817e204449aec532f0356c3c07";
		public static final String OUTBOUND_ONLY = "f6361f6d5af840289054f16a9592d09a";
	}

	public final class GatewayAuthenticationTypes {
		public static final String PPP = "93f28d87529e40879394577f0b5f2662";
		public static final String ISP = "9a85f829955d41bc9ccc87f727c314a0";
	}

	public final class ProcessConfigurationID {
		public static final int DISCOVER_DEVICES = 18;
		public static final int PRIORITY_FIND_ORPHANS = 19;
	}

	public final class TaskProcessorCommands {
		public static final String START_ENGINE = "ec1ef82ec7ec4704b117bf3315cc89ca";
		public static final String STOP_ENGINE = "338b167a563540a495e38492dc586bfc";
		public static final String RECOVER = "c100d8f599454e308d0a60352c5fc4e5";
	}

	public final class PhaseTypeID {
		public static final String UNKNOWN = "0e05b04fb4a245269e0fd5a7fee43dbf";
		public static final String L1 = "1e5f6731bee04b559e2f16f11a0ce681";
		public static final String L2 = "e1b4d3d6de9d477a9c72e7aa5cf639bd";
		public static final String L3 = "e736139e387d4905a2f9cca4c9b9af05";
		public static final String L1_L2_L3 = "5c90f6ce54ec4e69945299788995d455";
	}

	public final class MeterLonTalkKeyStatus {
		public static final String PENDING = "dae97ad3037d479c84213122e322c187";
		public static final String LAST_KNOWN_GOOD = "4fd8350bf13844549eae72b0540f66cb";
	}

	public final class DeviceServiceStatus {
		public static final String CONNECTED = "9d9867a0f3844b50a3a221c2a94c2567";
		public static final String DISCONNECTED = "1a3adf724b5b4b029f0d83939ab93e67";
	}

	public final class InitialCommunicationTypes {
		public static final String COMPLETE = "7ffc3f8959344fbcbb5f65239630216d";
		public static final String NOT_COMPLETE = "59ca036643f24386bbac495255d992bb";
	}

	public final class EventDeliveryTypes {
		public static final String SOAP = "3c3d5784292a4e3b9224fcda703a6348";
		public static final String NONE = "f5d30980f6794d9299a4153aa01316d8";
	}

	public final class EventDeliveryStatuses {
		public static final String FAILURE = "183a3c523dc6454da08774f5c2ec8c4e";
		public static final String SUCCESS = "5f0392101a404c828d7ed6a81a90afff";
	}

	public final class EventDefinitionStatuses {
		public static final String ENABLED = "9a59ae3e83854a4fbbebde54dc9fe31b";
		public static final String DISABLED = "28dc3a83582b4272a08c3ec438b099d1";
	}

	public final class MaximumPowerEnableTypes {
		public static final String BROADCAST_ENABLE = "4f24e3216ab245cb8c217bb6c8ec5493";
		public static final String BROADCAST_DISABLE = "90774857012f4c22af5bb3e9c393f1d3";
		public static final String ENABLED = "A7F520A5FB1441e5847942045E546202";
		public static final String DISABLED = "78B0E3B009B54a96A21F61D6770F09F2";
	}

	public final class MeterDisplayCategoryTypes {
		public static final String SELF_READ_DATE_TIME = "6713EDAE75A04799A6FC3E25E3C81531";
		public static final String PRESENT_VALUE_INFORMATION = "7853F025C50144b189B5DEF3CBDA412B";
		public static final String CURRENT_DATE_TIME = "22DCB2B2C5AF440b87447B2D2ADED564";
		public static final String SELF_READ_INFORMATION = "DB22E435D9CC412990B8F69BE5CBB599";
		public static final String CURRENT_TOU_CALENDAR_ID = "69D60DF737764855832D4DB936A5AE9F";
		public static final String FIRMWARE_VERSION_NUMBER = "B4C494AEC77F4fa2B73829A67D8EE88E";
		public static final String PREPAY_CREDIT_REMAINING = "065B922548874fc2B5CCB7BB447DAE8A";
	}

	public final class InformationReturnTypes {
		public static final String DETAIL = "1441a464525d43659c02b3d608175be2";
		public static final String GENERAL = "3294f665bc644359bfd15824d2efe29c";
	}

	public final class DeviceResultTypes {
		public static final String END_OF_BILLING_CYCLE_BILLING_DATA = "d357ee7048cd4983a3ee896f0e49c562";
		public static final String FULL_LOAD_PROFILE = "60381e83d07945649a86c193cdc40d6b";
		public static final String BILLING_DATA_ON_DEMAND = "67a59c9ce2d64e98a8fe0bf34a0d8267";
		public static final String POWER_QUALITY = "56f2cb69a8a44247be7a537b754f72af";
		public static final String TOU_SEASON_SCHEDULES = "C2F525891E70439d847AF31F77C09385";
		public static final String TOU_DAY_SCHEDULES = "A3DD474A61324fafB0BE2695DB7FC2A4";
		public static final String TOU_RECURRING_DATES = "37D73FDB453241ffA26B5D4BDFF0A33E";
		public static final String SELF_BILLING_DATA = "ad85befc6cc448bf9863c5cb62007561";
		public static final String SECONDARY_BILLING_REGISTERS = "7cf449f68be04523b3fccef2a7781e4e";
		public static final String DELTA_LOAD_PROFILE = "1707758FC0134fc187B105D50BF33723";
		public static final String DISPLAY_CONFIGURATION = "5FB2472783C745dc8B110BD8E116A99E";
		public static final String PRIMARY_MAXIMUM_POWER_STATUS = "C1340AE2C9B74d088D5B206CB8C8A5C2";
		public static final String PULSE_INPUT_CONFIGURATION = "5ddd67c34b0a419e8b7204d16c308e92";
		public static final String INSTANTANEOUS_POWER = "6072CB933A0840cbA8FEE91456A5F1CB";
		public static final String CONTROL_RELAY = "3b295e082b3d43d2bd2d3908ae7fe164";
		public static final String LOAD_STATUS = "65621819be064241bee46d5a38a67f15";
		public static final String FIRMWARE_VERSION = "5bc2f8cfdbc846148e517a2d9514248e";
		public static final String ACTIVE_TOU_CALENDAR = "b98a6c0ff148489fb4443f654f848bf6";
		public static final String PENDING_TOU_CALENDAR = "d57d9409bdbb425b8ab239c04120da78";
		public static final String DOWN_LIMIT = "690c5eb6f780485bba43f94c916245c2";
		public static final String ALARM_POLLING_RATE = "418fded6134b4ddcbb2333ba6f58e97c";
		public static final String BILLING_SCHEDULE = "ED775679030446568E37EFA97927B8D3";
		public static final String HISTORICAL_BILLING_DATA = "43bc7004769945399a1a1f645ba09af4";
		public static final String MEP_CARD_CONFIGURATION = "96022cc99e80465a84ace39acead1860";
		public static final String STATISTICS = "8978b6ab31ca4a409623a3c977eb0bff";
		public static final String AUTO_DISCOVERY_CONFIGURATION = "b6914d3d1b2c4ceaa2091178846f6da0";
		public static final String POWER_QUALITY_OUTAGE_DURATION_THRESHOLD = "0A92EE53D0674ce0B7DA0A00B8FC40DB";
		public static final String POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD = "00BBBD2D5B7B4c198B7B3E464C233A4F";
		public static final String POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD = "70724FDA9D074cffA060101AA67BCED5";
		public static final String POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD = "C1E8C1157D7A4f13874119B3811A0CDC";
		public static final String POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD = "EFC5B52A066B4f2f82EB5364AE9E24C8";
		public static final String EVENT_LOG = "413761a463d5411db19c3754c983d49a";
		public static final String UTILITY_INFORMATION = "4e16942686b74ce1a8907828aea9779c";
		public static final String PREPAY_CONFIGURATION = "8BD9240D590C4613A3A0DE265501879F";
		public static final String TIME_ZONE_CONFIGURATION = "e5bc9ce182594fa4b342615cc690bdc5";
		public static final String PREPAY_CREDIT = "66bcaa97329b40b2ae1fade1f97a3de1";
		public static final String MAXIMUM_POWER_LEVEL_DURATIONS = "2B7F31C9E9AE4b778E93BAB84725A6C6";
		public static final String PREPAY_MAXIMUM_POWER_STATUS = "64b4504113c24ada8f06b23775ee5f5f";
		public static final String PREPAY_MAXIMUM_POWER_LEVEL = "f12e58ca90d94fa1b24793e2244b50b7";
		public static final String MAXIMUM_POWER_STATUS = "C1340AE2C9B74d088D5B206CB8C8A5C2";
	}

	public final class GatewayResultTypes {
		public static final String STATISTICS = "3a19bf68e06e4879a0cccc0754dae661";
		public static final String TOTAL_ENERGY_DATA = "4bdd989403aa4c1691fce3c104bf09c3";
		public static final String FIRMWARE_VERSION = "5cc432d671ba465ebb6bd990b6901605";
		public static final String REPEATER_PATHS = "220e1da59e534c82ac57ca095b5db969";
		public static final String PROCESS_CONFIGURATION = "9331484664784a3ca4fdb62a0de86c28";
		public static final String DISCOVERED_DEVICES = "ad2bbabf67e549ec8de11e31592313ba";
		public static final String WAN_CONFIGURATIONS = "8efc087116114b91a528ba1ea6691566";
		public static final String DEVICE_LIMIT = "39ca7c607ed7427995d0853fbbe74998";
		public static final String PERFORMANCE_STATISTICS = "3a19bf68e06e4879a0cccc0754dae661";
	}

	public final class RepeaterTypes {
		public static final String DISCOVERED_DEVICE = "8eb085070912454295b35e0740eddd96";
		public static final String EXISTING_DEVICE = "e7bf5c60c98f48f5b39c025106cd1ff4";
		public static final String GATEWAY = "2851897aec124b189e91b136aec679c2";
	}

	public final class DiscoveredTypes {
		public static final String ORPHANED_DEVICE = "f6676a4ae64d475f9a3153da9d847027";
		public static final String NEW_DEVICE = "ef4af3d51f074efc898b6a5a7a368346";
	}

	public final class DC1000HardwareDiagnosticCodes {
		public static final String DRAM_FAILURE = "4b64107457be4180af3bcd97c89d29f7";
		public static final String FLASH_FAILURE = "aea543c7493643b0b4c57d7b50469abe";
		public static final String SRAM_FAILURE = "d6920a13175f410cbdd7d678e202ede3";
		public static final String RTC_FAILURE = "019298bda6e447e4a9192d61531dcf96";
		public static final String NEURON_FAILURE = "5fcfbdc2fd0f4177aa7b9f37adfd4f7e";
		public static final String CLOCK_FAILURE = "9bec8186d8c949c0a7cc5f6bbfad7fc1";
		public static final String PHASE_FAILURE = "cb6204fd63ba42e4937efd7b9ebfd7a1";
	}

	public final class DC1000SoftwareDiagnosticCodes {
		public static final String NONE = "004ca744a09d4733a9d18a428e75967b";
		public static final String BOOT_LOOP = "cf8b328502b5412ebfdcf2e32b41afe1";
		public static final String FILE_ERROR = "f5f9e9d769ba4c44a144d0d6465c7b54";
		public static final String RTC_NOT_SET = "56e19e8d77e54773afd46a5152104bf1";
		public static final String RTC_BATTERY_LOW = "0bd750aed582498782259756e3d819dd";
		public static final String INVALID_PPP_USERNAME_OR_PASSWORD = "5f51f415e4464721b0134987d2f0e9ec";
		public static final String UPDATE_FAILURE = "20827241468b40ec92fa1b21b9afe29e";
		public static final String UPDATE_IMAGE_CRC_FAILURE = "589f193efb2d4ae39ec95c4efb02b2e0";
		public static final String INTERNAL_REPEATING_LISTS_ERROR = "82fd0b512090464b8aca2821675ddcbe";
	}

	public final class GatewayToDeviceCommunicationStatusTypes {
		public static final String UNKNOWN = "f4183c2b23d7458a9e78529ae9c3cc27";
		public static final String UP = "c4a83cbbb8ac4794ab59562bc4b88a32";
		public static final String DOWN = "587080ce53fb4a95ba6dc51838d410c9";
		public static final String CONFIRMED_DOWN = "af5fdf7f4c644936b131e3ac3d9d6926";
		public static final String NO_AGENT = "3680be224f2745e2a132c651d22666dd";
		public static final String NASCENT = "198044101bde44c483325d3a5803e407";
		public static final String WRONG_KEY = "f0b97e6646c8494c964d972c19a634ad";
		public static final String INVALID_NEURON_STATE = "fd345d84467d4841b87f7ea17d5e11d2";
	}

	public final class DC1000DeviceNackCodes {
		public static final String SERVICE_NOT_SUPPORTED = "3b06b5ad902642d4ad5d7637f1bf1fb2";
		public static final String OPERATION_NOT_POSSIBLE = "2bdfd376f3f24f02b4185f9f85d3bb1e";
		public static final String INAPPROPRIATE_ACTION_REQUESTED = "595d74df2b3f427495e819a1eee59f26";
		public static final String DEVICE_BUSY = "832aaf3fa21d44468d50c8caa7c23a77";
		public static final String DIGEST_ERROR = "4ac31c661b35436e84668abb03c474bf";
		public static final String SEQUENCE_NUMBER_ERROR = "af18c500715d4e30b9d77e472adcf575";
		public static final String PROCEDURE_NOT_COMPLETED = "6e204365ef9c4f13a8128735992a8fbf";
		public static final String INVALID_PARAMETER_PROCEDURE_IGNORED = "de4e05b8c0b64948b4185b77a2d42bd0";
		public static final String DEVICE_SETUP_CONFLICT_PROCEDURE_IGNORED = "20136ea064d54945a1ec20b63540d22a";
		public static final String TIMING_CONSTRAINT_PROCEDURE_IGNORED = "6d40fae46ffd4a1f8136bdf4e29c0c21";
		public static final String NO_AUTHORIZATION_PROCEDURE_IGNORED = "903fae11f8254d08ae8c809bddd41a16";
		public static final String PROCEDURE_UNRECOGNIZED = "9f88e6fab35d4715989f0c8cda4b5d58";
	}

	public final class DC1000DeviceAlarms {
		public static final String CONFIGURATION_ERROR = "8ca7f8c35d2247a397c49c9f33d668a0";
		public static final String SELF_CHECK_ERROR = "21b3b465b3e64bce952133f43f706c5c";
		public static final String RAM_FAILURE = "e5a5b0d1f1e44129a98fddaf6ce4398a";
		public static final String NON_VOLATILE_MEMORY_FAILURE = "82a33447b5ab434bb4e578eec104bcea";
		public static final String CLOCK_ERROR = "f316273e6f374e0086710af9d101054f";
		public static final String MEASUREMENT_ERROR = "41e0b86652164cd4a539b226cea18719";
		public static final String LOW_BATTERY = "4ea1f5f2583f4017b8c449d6423123ca";
		public static final String POWER_FAILURE = "b02fb601b98a47ce8de32dbb157fb2a2";
		public static final String TAMPER_DETECT = "6f37d5a7280b405288fdb174c5a1af52";
		public static final String ROM_FAILURE = "79c97a9c3bb147f7a21e921d43d51eb3";
		public static final String CURRENT_FLOW_DETECTED_ON_LOW_VOLTAGE_PHASE = "a7f07be6c8414b79b7b1a3ebbfe60ee2";
		public static final String PLC_CONFIGURATION_FAILURE = "88af05a6ce154fe5a4da71b92b1c6f88";
		public static final String INVALID_PASSWORD_ENTERED_OVER_OPTICAL_PORT = "39320b04e50d41f5ac68391dbd4d0131";
		public static final String REVERSE_ENERGY = "729ba6910acb4426a0a0706005a58976";
		public static final String SAVE_ALL_ABORTED = "0ef90b3b76b94967b0e623386472e25c";
		public static final String DISCONNECT_ADC_ERROR = "75fbf631eb7947d29138327c11291ff1";
		public static final String GENERAL_ERROR = "2dd6e07c1bcc4d78bd4a8014f3fe21b2";
		public static final String REMOTE_COMMUNICATIONS_INACTIVE = "782db533d360401189e5f08b7f895d80";
		public static final String PULSE_INPUT_CHANNEL_1_TAMPER = "e00f616458684d26b4882b5e33af693c";
		public static final String PULSE_INPUT_CHANNEL_2_TAMPER = "0618bed8c9dc4be3941f0207c699632e";
		public static final String PHASE_LOSS_DETECTED = "da41430263f2482c83180c7ec6ca5282";
		public static final String PHASE_INVERSION = "bc648feb5e324de9ae4578bc89a8d557";
		public static final String PRIMARY_BREAKER_SHUTOFF = "cca2df7b0bd54b61afeb42cdddd23cad";
	}

	public final class DC1000RebootCauses {
		public static final String POWER_UP = "b13d8506f1de4e6e8d9dc64180f74505";
		public static final String PANIC_RESET = "d708a5b736b04571bdff9d1ec168bccd";
		public static final String CACHE_FLUSH_FAILURE = "ae2a15e81ba94ac59e87df93ec5be145";
		public static final String CLOCK_WRAPAROUND = "411e00e2b07e48b8b4ec3d9ee5c92b38";
		public static final String EXTERNAL_REQUEST = "8656b3a5d3c44f34bf61f08238e90815";
		public static final String LOCAL_REQUEST = "7c71772af47845579de0fa9f6fdbacef";
		public static final String BOOT_API_RESET = "5b9e5d27cf4a47a6be04d94bab143f4e";
		public static final String NO_MEMORY = "d1b8de3ce66742a494b3c987a10bf0d9";
		public static final String WATCHDOG = "55662d889f4543deb10ed7ce221a8b0c";
		public static final String REASON_OUT_OF_RANGE = "5066441d40d54fa9be56bfc47ecbe6ee";
		public static final String MODEM_INITIALIZATION = "fb7c3e5f1e85465d8204c1bdc47cd189";
		public static final String UNKNOWN_EXCEPTION = "f9846a7806724db8944f6f417e84f9c0";
		public static final String FATAL_TAMPER = "326f491f8a8741b4a41672898630cb5a";
		public static final String BUFFER_EXHAUSTION = "7468ccbce24c405194ac93c8c2cf582a";
		public static final String WAN_INACTIVE = "9a5256eae68d48e694a1126d47c001c5";
	}

	public final class DC1000Resources {
		public static final String NM_NODE = "36a5f19a66bc46f4b13964367ad1021d";
		public static final String DCX_RESOURCE_STATUS = "d4a0d220db26496db1b702833240b7f8";
		public static final String DCX_NODE_STATUS = "396b6b8fb33b48978115235add3c34dc";
		public static final String DCX_DEVICE_CONFIG = "82e64ac83ebc4cc8935ffd41408d3e25";
		public static final String DCX_DEVICE_STATUS = "91e0ec8d1f4a45e3a70e22f0a6bc7fc5";
		public static final String DCX_DEVICE_UPDATE = "86e15389b2664fca8728bc55d19d8eba";
		public static final String DCX_SEGMENT_CONFIG = "a784e1ab989640a9ae186540344805ce";
		public static final String DCX_SEGMENT_STATUS = "3616bc805f8b465aa1a987389b8fbfef";
		public static final String DCX_EVENT_CONFIG = "7205a16eaac348648565a9924ef17f02";
		public static final String DCX_EVENT = "eaf730f1a6064e4aa1117892be28ad57";
		public static final String DCX_FUNCTION_CONFIG = "b8ec04b9d0db4735b836018fcb9e7cc0";
		public static final String DCX_FUNCTION_STATUS = "e54ec57778654403b909a34dd2f71f96";
		public static final String DCX_OPERATION = "fc64e88b608c4220be1297f2f3d34cfd";
		public static final String DCX_OPERATION_STATUS = "988c0212a4224febb38d0348b3606354";
		public static final String DCX_RESULT = "ac826f9a39ea4d1481f533ae46fcce48";
		public static final String DCX_MESSAGE = "ae87304e6fd74bb4976ca484c4b0017b";
		public static final String DCX_TIMEZONE_CONFIG = "165deff7510a4c03bfce7886581b5269";
		public static final String DCX_TIMEZONE_STATUS = "e4924d221c884e98ae26e91eafd8d889";
		public static final String DCX_DATA = "e719104a17a6487ab4f9e5b044f9274f";
		public static final String DCX_MEP_DEVICE_CONFIG = "f1c70f26857d478fa865c25c4ff1563a";
		public static final String DCX_MEP_DEVICE_STATUS = "123cbd87def44a37bf94189ae1850ee8";
		public static final String DCX_DISCOVERED_DEVICE = "dfd18367d5394c96a5f2e8092505ea4d";
		public static final String DCX_WAN_CONFIG = "77f826bf5b2547d881032452729be48a";
		public static final String DCX_WAN_STATUS = "f4c55e21e3f543069d8045811ed974a3";
	}

	public final class DC1000SecurityExceptions {
		public static final String NONE = "b8686056929643738bfe0f77dadf755e";
		public static final String ST_CONSOLE_PASSWORD_FAILURE = "8f8879ab79a54fb6b47d69e181d60d78";
		public static final String ST_PPP_CHAP_FAILURE = "ec30817dab1740a0b201bf9667f0c394";
		public static final String WAN_PPP_CHAP_FAILURE = "4729f2d9cc8d413aa9ee96ae1b4706e5";
		public static final String WAN_APPLICATION_AUTH_FAILURE = "92440929f37347b78a02b5aa96d51fb4";
		public static final String WAN_SSL_FAILURE = "9cc8846087594119b26b8e2d126345df";
		public static final String LOCAL_SSL_FAILURE = "6dcf3a4591824046accafbbd0002e3b2";
		public static final String NES_SECURITY_FAILURE = "490197ec69414958b287ae20144ecd33";
		public static final String PEER_DCXP_SESSION_CAPABILITY_NOT_RECOGNIZED = "59a625ea315e4f05b531d28171b21983";
		public static final String PEER_DISAGREED_ON_DCXP_SESSION_CAPABILITY_REQUIRED = "a9cd9883d44f46e989d20290a3d3dfd6";
	}

	public final class DC1000RsaKeyStates {
		public static final String NO_RSA_KEYS_PENDING = "f19c279741f2458ca30e8624cebf8554";
		public static final String GENERATING_RSA_KEY = "f38aaf3bf84348bbaa7e5ad6b1d9a773";
		public static final String RSA_KEY_GENERATED = "4202e082ffdc40a597ad3fde2c03aaf9";
		public static final String ACTIVATING_RSA_KEY_AND_CERTIFICATES = "0569534a911d43898458baa3d8f3ed05";
		public static final String RSA_KEY_AND_CERTIFICATES_ACTIVE = "830b26c7e3694a2aa4e8ef514cbdd2a4";
		public static final String RSA_KEY_GENERTATED = "4202e082ffdc40a597ad3fde2c03aaf9";
	}

	public final class EventStateTypes {
		public static final String OCCURRED = "71c92fac407f4e25bd77e444899400c9";
		public static final String RESTORED = "8a6130176ee84aff84e2f7249e92d69c";
	}

	public final class CommandFailureTypes {
		public static final String DC1000NACK = "2873e7cb9fd9421ab66af67e1e7783fd";
		public static final String GENERAL_FAILURE = "401a8956d93348058007bf5736c3a261";
		public static final String TIMED_OUT = "7ec1f45a3f204670b03b02714ea15bbf";
		public static final String DC1000_COMMAND_TIMED_OUT = "d509192f4b2e4d9e848a6472060e91cb";
		public static final String DECRYPTION_FAILURE = "2bd0e075546546f6ad24c699ab251034";
		public static final String DECOMPRESSION_FAILURE = "e33f0b2337f546ca827fbb7d5cbbce00";
	}

	public final class DC1000NackCauseCodes {
		public static final String UNKNOWN_CODE = "7bede9d036d645708c254902e43cb85d";
		public static final String RESOURCE_NOT_FOUND = "2ca9c48665d845e8a29fb44f422055d4";
		public static final String NOT_IMPLEMENTED = "e08347ba4e66489c89de83b0bfb61bb6";
		public static final String INVALID_PARAMETER = "a4719031032c4c89833760f6dfca554f";
		public static final String OPERATION_FAILED = "8a0fe7f515584b35a954ff2a4d85a194";
		public static final String INTERNAL_FAILURE = "7fbc6eb80e7a4773a47be53aae83b722";
		public static final String OUT_OF_RANGE = "3a3030aa4d85406dae9d2d4b5cf2da72";
		public static final String INVALID_LENGTH = "44bd8e06b1cc4a8a8635e3be52b0278f";
		public static final String PROPERTY_NOT_FOUND = "773f7e89b3024ff9a9bc54d861732aed";
		public static final String OPERATION_NOT_ALLOWED = "108d4460f16249a68ba04b57e3a3d476";
		public static final String REPEAT_CHAIN_TOO_LONG = "f7c9fc8d7fdb48acaa34fad7a296fc3a";
		public static final String LIMIT_EXCEEDED = "0ce0c89985744d0c96355a1a78151765";
		public static final String RESOURCE_DISABLED = "8e9d68d762f24ae19616aa961373753a";
		public static final String PREVIOUS_SEGMENT_NOT_FOUND = "6f2087e3bb3a4b93845c3d3b006e715d";
		public static final String SEGMENT_LOOP = "3959d9a2df3949b79f886cc7e22211d1";
		public static final String DUPLICATE_SUBNET_NODE_ADDRESS = "0f19631536e34503b6bd87fa7738d76e";
		public static final String SEGMENT_NOT_FOUND = "8ecaf37e9f2848c0a500da1ba9b04e00";
		public static final String RESOURCE_IN_USE = "0122c07e6de9477db10e222b5f5a6ee8";
		public static final String DEVICE_NOT_DEFINED = "5b02f42be0e54120967e237d5773d60d";
		public static final String MESSAGE_SET_NOT_FOUND = "8225d882f2154ddda545e0fad314649f";
		public static final String DEVICE_NO_RESPONSE = "5ccfd4c78cc4486685069818c9f90a03";
		public static final String FUNCTION_ABORTED = "f0e399264ff44fbc9304f643621ff1a4";
		public static final String OPERATION_EXPIRED = "ffc44b7cca3549b8886c03880564cbcb";
		public static final String INVALID_SEGMENT = "055cd2fe5ec144c29ee11f2a6378d85c";
		public static final String INSUFFICIENT_RESOURCES = "299593d7d6ce4383850aee0533f8ca1a";
		public static final String INVALID_DATE_TIME = "5f61bdd1b0154f838bb5af7d83aad616";
		public static final String IMAGE_NOT_FOUND = "1e6dc2ba35c04b448323e83c401a0ade";
		public static final String IMAGE_CRC_FAILURE = "e3889119c0bf48b7b3f8c4e0b5059df5";
		public static final String TOO_MANY_TEST_POINTS_FOR_SEGMENT_PHASE = "49a71a8e2b45452e8691036c04609de3";
		public static final String DEVICE_PROCEDURE_FAILURE = "912b0781d191457d96173c20f309e1ba";
		public static final String TARGET_DISABLED = "d5c93d880bd344fc8526742c262e9b51";
		public static final String ADDRESS_ERROR = "bcd6895af8f04fe68f63939db43b8abf";
		public static final String DEVICE_NOT_REACHABLE = "52487c5461414eaf913ec728ba8218c1";
		public static final String AUTHENTICATED_RESPONSE_FAILURE = "e3482665545c448281312048d31cbb66";
		public static final String RESPONSE_NOT_AUTHENTIC = "55b768b9ee434dae830fd1bea97d0020";
		public static final String RESPONSE_INVALID = "8596c218b86348be854ffa1ce577519a";
		public static final String TARGET_NOT_ANSWERING_AGENT = "335d532ad9fb4935b318a0b0d81f6ccf";
		public static final String TARGET_OFFLINE = "25ce1cbe78744757824fde35b1aa001b";
		public static final String REPEATER1_FAILURE = "8c8d4339487e41378cef725c13124513";
		public static final String REPEATER2_FAILURE = "e24d78f565db4be99d39a4ddfe945ce9";
		public static final String REPEATER3_FAILURE = "c48a77afb7ca408989a1baffae4ef0c9";
		public static final String REPEATER4_FAILURE = "289766e2bc4a4785ae1894a8e5965677";
		public static final String REPEATER5_FAILURE = "12294b1f597c4c939e487964d22a8c13";
		public static final String REPEATER6_FAILURE = "1352c037bac24145b69d4262ccf33740";
		public static final String REPEATER7_FAILURE = "f2b7bdd6a77945909092a98158883179";
		public static final String REPEATER8_FAILURE = "04c3847ff44c4a63b0516e789070fb16";
		public static final String PHASE_NOT_MEASURABLE = "afc90b175f9f41678a53c46271b713e8";
		public static final String INVALID_STATE = "8418b3f062754f0eb4614e37bb3b8041";
		public static final String GENERIC_RESPONSE_FAILURE = "b4d17ed741f94a628e3c91a351485cb5";
		public static final String INVALID_HANDLE = "7a709e175ac241198b8ee33b22cee2bd";
		public static final String INVALID_STATE_FOR_ONLINE = "14ca9c087a8c499e8abe88115fb47635";
		public static final String INVALID_DEVICE_TYPE_FOR_OPERATION = "0bfddc37ed2947b98a06355c5a46aa87";
		public static final String AGENT_HAD_NO_BUFFER_FOR_RESPONSE = "e5039e51c7e94c6d9fe8e8130609cffb";
		public static final String APPLICATION_AUTHENTICATION_FAILURE = "472b59afec734ddfa7e81d15db42e122";
		public static final String DCXP_SOCKET_NOT_SECURED = "c70766aa40c140548988481526d1619a";
		public static final String DCX_NOT_READY_TO_HANDLE_REQUEST = "a0754514f9c14f8fb4c19d77c324ac4d";
		public static final String COMPRESSION_FAILURE = "c961e7e5094a4eafa1aabbf4518d48cf";
		public static final String DATA_OVERFLOW = "1f55c680a9e14c85a5c05a8b67743ec5";
		public static final String INVALID_DATATYPE = "ce73203b971e42ccb6bf314df29c6c35";
		public static final String DUPLICATE_PROPERTY = "84c47d934e164db8989f61a569865206";
		public static final String TRANSACTION_NUMBER_MISMATCH = "06575edeebbe48f4be8077f7c8e898f8";
		public static final String TRANSACTION_NUMBER_INTEGRITY = "0a7f6f8940a44402adfd91cfcab96e99";
		public static final String MEP_DEVICE_NOT_FOUND = "6bb8ac1862c449be9ea146616cf08896";
		public static final String OPERATION_INTERRUPTED = "c090d89ae37e484192a22ea16bf951e5";
		public static final String MIXED_ENCRYPTION = "eb0a1702284c4872beae04c569df6e04";
		public static final String WRONG_KEY = "34b87b9bb7e04a03b0b8a97464a43fde";
		public static final String CAPABILITY_REQUIRED = "875464591544435d9e677655982f231f";
		public static final String DECRYPTION_FAILURE = "af8db2b857954c7bac965fb8fb60372e";
		public static final String OPERATION_REJECTED = "04af3fbcc9d64a32a9cee9fbf6348055";
		public static final String NOT_MODIFIABLE = "69514fc919a54b298ccbc7cd0365f7c9";
		public static final String PHASE_ABORT = "2fedd5d37bc94b1a988c324889e199c2";
		public static final String WAN_CONFIG_ERROR = "17273d9be3574e38995c4a7864e0f87f";
		public static final String DEVICE_RESPONSE_CODE_UNKNOWN = "736313e4a781496598ede74340dadacd";
		public static final String DEVICE_REQUEST_REJECTED = "37e9919f3435457889c9cdca9d97cb0b";
		public static final String DEVICE_SERVICE_NOT_SUPPORTED = "f374d7adb35f4e4f9b8204d8ea998e7a";
		public static final String DEVICE_SECURITY_FAILURE = "16c04c74266c4312b4dbf6c2ff2c00ef";
		public static final String DEVICE_OPERATION_NOT_POSSIBLE = "0be77170579648f7b30433c884ce2235";
		public static final String DEVICE_ACTION_INAPPROPRIATE = "ccfc02e2c0544e33a17554a9d183bc4a";
		public static final String DEVICE_BUSY = "c43acd10253e4b4ea7f3fcb16247df1f";
		public static final String DEVICE_DATA_NOT_READY = "2027621a942a4cbcb818e7a4fd8d5899";
		public static final String DEVICE_DATA_LOCKED = "b969c4b328764ee2902f909c64eb7f49";
		public static final String DEVICE_RENEGOTIATE_REQUEST = "723d1545fe4b4eefb184d17b496235f6";
		public static final String DEVICE_INVALID_SERVICE_SEQUENCE_STATE = "2bd216169b9f49b0805e24db949ab6d0";
		public static final String DEVICE_COULD_NOT_AUTHENTICATE_REQUEST = "e15e1a81a44e425cb8a913d951cf3348";
		public static final String DEVICE_INVALID_AUTHENTICATION_SEQUENCE_NUMBER = "27d23394e031411da029bcdd40d107c4";
		public static final String DEVICE_PROCEDURE_ACCEPTED_BUT_NOT_COMPLETE = "64c9e4f9a2144116883a58ca38b8963f";
		public static final String DEVICE_PROCEDURE_INVALID_PARAMETER = "2e8ea53c80aa43b6ab15c16bfe3cf89f";
		public static final String DEVICE_PROCEDURE_CONFIGURATION_CONFLICT = "939a4b28014d4a108a548c016df067e3";
		public static final String DEVICE_PROCEDURE_TIMING_CONSTRAINT = "b1589efd4707427098b22019122a809e";
		public static final String DEVICE_PROCEDURE_NOT_AUTHORIZED = "de604d7bbe6647bd9736afc9da12de0c";
		public static final String DEVICE_PROCEDURE_ID_INVALID = "3920dd5e02b44503ab4f49e3fa927912";
		public static final String DEVICE_ON_DEMAND_REQUEST_TABLE_FULL = "0ff6e66d18804c65b9dc33725b47e756";
	}

	public final class FirmwareTypes {
		public static final String DC1000_FIRMWARE = "d2c2a55650c44c0eaf1597f29f9647b7";
		public static final String METER_FIRMWARE = "175CAA2B5F804f3095A2242D63A4DBAD";
	}

	public final class TaskProcessorCommandFailureReasons {
		public static final String INVALID_LOCAL_TASK_MANAGER_ID = "51D0838322E7452d9C8D682F69BE7B83";
		public static final String ENGINE_ALREADY_RUNNING = "ECF1941DA5E54ea68645183F471A4CC7";
		public static final String INVALID_TASK_PROCESSOR_ID = "885537BA1DE741a5AC86C082BC74C890";
		public static final String FAILURE_START_ENGINE = "28D3BB102D8E401aA85F857214AE9BC5";
		public static final String ENGINE_NOT_RUNNING = "BC2F7392200F45af9E7F280ACCF37EAF";
		public static final String FAILURE_STOP_ENGINE = "438CCC58E9B84fa29C3791FD0C2764F9";
	}

	public final class SecurityAlertTypes {
		public static final String TACACS_PLUS_AUTHENTICATION_FAILURE = "42432283DD554d788AADAE3ED770029E";
		public static final String API_AUTHENTICATION_FAILURE = "cc9d7f6f5fbd46b7b2ffa69b9b4f16ae";
		public static final String GATEWAY_AUTHENTICATION_FAILURE = "05f1e769ca0d43b2940910b7d34b11b1";
	}

	public final class ScheduleTypes {
		public static final String GATEWAY_NORMAL_CONNECT = "e56ed175268d4d4e85d37cce8aae7e31";
		public static final String ARCHIVE = "49f3c2a29e2e4bba957d3d528e347cb0";
		public static final String ORPHAN_CHECK = "4f5ed07cb355480699d23f48dcdb5cdc";
		public static final String AGGREGATION = "2e4b9bcb6348411ea07c42bf9e9c0c9e";
		public static final String TASK_TIMEOUT = "45c1a77ca40a40eb8d2cd5a8a508c131";
	}

	public final class MeterFirmwareDownloadStatus {
		public static final String SUCCESS = "97E5A2ED2EE744928FFE6136EABA98AC";
		public static final String FAILURE = "57B9E3C731414446A8F7F2645D4E92DF";
	}

	public final class DataPointValueSortTypes {
		public static final String EXPECTED_DATE_TIME_SORT_1 = "159d3c3adc414917b9174c4724b211d8";
		public static final String DATA_POINT_ID_SORT_1 = "fb8e282785774d91a7a620c8bb8bf4f9";
	}

	public final class SortOrderTypes {
		public static final String ASCENDING = "be3c378cf94d4c0882eb1ba141992f20";
		public static final String DESCENDING = "cd6c6ac034334622b4a553a6fccadeb2";
	}

	public final class PulseInputChannelStatus {
		public static final String ACTIVE = "4f0771915d5247de9aefbd882835e211";
		public static final String INACTIVE = "f80e4a8ab88f4caca8f50aba19d63dd7";
	}

	public final class PulseInputChannelIdleState {
		public static final String LOW = "25efd3bbfb4248048b29d4e80ddc469c";
		public static final String HIGH = "49f02294e79b49f39e65091e35493150";
	}

	public final class ControlRelayStatus {
		public static final String BROADCAST_OPEN = "d7ff0b7cdb6d491c90afd700cda4971b";
		public static final String BROADCAST_CLOSED = "15080352c0264373a97501ba9e40517f";
		public static final String OPEN = "6eebd8aceb784b1f826a6cdfaf4f69ae";
		public static final String CLOSED = "f8e560fe460142f996e8374a890161b4";
	}

	public final class TOUCalendarTypes {
		public static final String ACTIVE = "bb8ef5a8233245588a047b77d0ad2a38";
		public static final String PENDING = "6a0b211245e44a5f8d443dbf012956da";
	}

	public final class RepeaterPathStatus {
		public static final String SUCCESS = "b3442ec53a644323a64ac7c8a6712eea";
		public static final String INVALID_LENGTH = "76159b841ca14454a53fcd1b16132b38";
		public static final String REPEATER_LOOP = "ea1194168e7844a89bce8792969dd10a";
		public static final String UNDETERMINED = "c49ad93bbc9c4bb3bdb0707dec1a8481";
		public static final String INVALID_REPEATER = "ae2c0b72ba9f4413a09e605a3d3d4846";
	}

	public final class AddMeterFailureTypes {
		public static final String WRONG_KEY = "51d30c3026e7457d9a65e3a6eeabff7b";
		public static final String INVALID_NEURON_STATE = "ab48409924b14e1c88f56f57bdfc5107";
		public static final String GENERAL_FAILURE = "15583ad9e2374760b3f2df089b966c0b";
		public static final String NASCENT = "394950942cb745f4a2a36243c5c6f7d9";
	}

	public final class GrammarStatusTypes {
		public static final String SUCCESS = "4a5f1225092c49de81f043ed9413f462";
		public static final String SYNTAX_ERROR = "b321d320dd8a41569af3946634a5754d";
		public static final String TYPE_MISMATCH_ERROR = "6d5a95405a95459594ffffb4e5b0104c";
		public static final String DIVIDE_BY_ZERO_ERROR = "acb2a003b0b64b1b9580f2495955b8c1";
		public static final String MATH_ERROR = "93009bfe6e31430da648f48538395fd9";
		public static final String SQL_ERROR = "824b239eef9f407aaeb755f65fbee79e";
		public static final String FUNCTION_ERROR = "1f90d1cc187d41009961d7f679a1c294";
		public static final String SOAP_ERROR = "181b384c327a44549bd9dbb121d8d209";
		public static final String UNKNOWN_ERROR = "6a9615ad625d4b62a15814bcae314c63";
		public static final String PROCESSING = "cc4481d44274443d9714de7de60513c5";
		public static final String TASK_WAIT = "fe6cad8132d94111b5a956e1d8fcaa0e";
		public static final String TASK_FAIL = "d5b58778df414d468c6389c735396900";
		public static final String TASK_TIMEOUT = "18FBAC82858046f3AC1037A45BC00DB0";
	}

	public final class BillingDataStructureTypes {
		public static final String VARIABLE = "de8df181afad466e8816b32fdf2d1e4b";
		public static final String FIXED = "08b17d6af9a546e8a1b7a12f8eff288b";
	}

	public final class MBusDeviceTypes {
		public static final int GAS = 3;
		public static final int HEAT = 4;
		public static final int HOT_WATER = 21;
		public static final int WATER = 7;
	}

	public final class TokenTypes {
		public static final String FUNCTION = "4114DF84A18043fbBCB4A61FFDA7D479";
		public static final String SOAP = "69F8AD561E1147ca802324B86CBCDC8E";
		public static final String SQL = "70850501EDC74bc39FD95E8B715D55F9";
		public static final String DATAPOINT_PARAMETER = "4AD86AFBF2154a50B62E4FAE23919320";
		public static final String EXPRESSION = "7F18F19D8B214f5eBDF2E7F1AB7BE4E7";
	}

	public final class IPAddressTypes {
		public static final String STATIC = "3492178071224eedbe1268a80b354ddd";
		public static final String DYNAMIC = "435555d42e56498db3d20e0dbd7ac961";
	}

	public final class PingGatewayStatusTypes {
		public static final String ENABLED = "637f2d8189c64d91a40b2d7296ec2b0f";
		public static final String DISABLED = "b28660246ee64fdead9b1c53b6f53a07";
	}

	public final class MBusStatusTypes {
		public static final String ENABLED = "42317b88d6d448c591cc3620e4b399d3";
		public static final String DISABLED = "74fe799136ad44719c942ee63880f7bf";
	}

	public final class InternalCommands {
		public static final String REMOVE_DEVICE = "b2e4b7691acf4b2daae5808818ff87d4";
		public static final String ADD_METER = "5536a36488624690a52dbb1a8ca469ca";
		public static final String MOVE_DEVICE_ADD = "7CF97C0997E543198AF4D3C777D69A61";
		public static final String MOVE_DEVICE_REMOVE = "92668107364343ffBC056C8D53CAD378";
		public static final String CONNECT = "bb496629589e4bf59aa83f393042a98c";
		public static final String DISCONNECT = "367d83df9ab74e9d8ea8cd21a7919ac2";
		public static final String REMOVE_METER = "b2e4b7691acf4b2daae5808818ff87d4";
		public static final String METER_STATUS = "5d00b50350924f1993021bd97ccaba64";
		public static final String CLEAR_METER_ALARMS = "f1a123b084324d148a35972cd1181187";
		public static final String READ_EVENTS = "5c434edd1bc54c25ae4eca27c4c27a7e";
		public static final String READ_RESULTS = "9f2e0bc96e0a417ab9cf240944c085ab";
		public static final String READ_DCX_DATA_RESOURCE = "01fd87fdbe5f4517a50c49ec612479ce";
		public static final String TIME_SYNC = "fcb91eea9630431a8cdf27b07fa54b54";
		public static final String INITIAL_COMMUNICATION = "772660C3ED094b548A8F05F5A10A1236";
		public static final String CREATE_ACCOUNT = "5e3117ba59874854bdd2b5dc5913dc5d";
		public static final String DELETE_ACCOUNT = "924465babccb46af9079420b8a9207e7";
		public static final String TERMINATE_SESSION = "00aba36789d647eba3b60e62db1486a1";
		public static final String AUTHENTICATE = "b18a56c164c24d059710ced501f6ce36";
		public static final String READ_METER_FIRMWARE_AND_BOOTROM_VERSIONS = "7c895be303b241c9871bc4671cd64255";
		public static final String ENABLE_METER = "b8255ff459604e48bb0a89537f87e8ce";
		public static final String RETRIEVE_METER_SOFTWARE_VERSION = "D540611DF14F45a6AA141F4A4471A6A6";
		public static final String RETRIEVE_METER_FIRMWARE_VERSION = "6BCA5DA454154fdbBE9D933F8AC32DEF";
		public static final String GET_GATEWAY_VERSION = "D410B8BE717F43749E012E2F276C4F51";
		public static final String DELETE_DCX_EVENTS = "ae5565c2b01a446d85362aedc31f46cb";
		public static final String DELETE_DCX_RESULTS = "68bda33aad2149cf8995183d578bb07e";
		public static final String DELETE_DCX_DATA_RESOURCE = "fdd0c687f67b435099755dbdef1ee59d";
		public static final String SET_STOP_MODE = "598080cfd80544e2a8f54bca7075252e";
		public static final String CLEAR_EVENT_LOG_PENDING_OVERFLOW_ALARM = "17ce1d88cf78470580565dcca36640ad";
	}

	public final class DataOrderTypes {
		public static final String MSB_FIRST = "a0acddee89ef4ef9baa2ca507faf11ad";
		public static final String LSB_FIRST = "e20bcc56866146f2bfc0e83f7aef70b2";
	}

	public final class KeyAvailabilityTypes {
		public static final String KEY_AVAILABLE_TO_DECRYPT_DATA = "993559bc23d84ba4951770ef131c5043";
		public static final String NO_KEY_AVAILABLE_TO_DECRYPT_DATA = "1fa69a08f49b4701804354fb57076551";
	}

	public final class MBusSecurityStatusTypes {
		public static final String PASSED = "bbceb6d86d5d4b64954f4fa2e833fd51";
		public static final String FAILED = "d95545c359174f73ae98c10905330fae";
	}

	public final class MeterDetectedMBusAlarms {
		public static final String BILLING_READ_OVERFLOW_OCCURRED = "81f436cdb03a42478cab94e128b17986";
		public static final String FAILED_COMMUNICATIONS_ON_READ = "3f3a1abce12d4ad78e5095186e685445";
		public static final String BILLING_READ_SERIAL_NUMBER_MISMATCH = "2d019ae40d7842a7839b837748369590";
	}

	public final class MBusBillingScheduleFrequencyTypes {
		public static final String HOURLY = "07794DC86EAE44d595D3D089B37759E3";
		public static final String DAILY = "908761AF6EAA4b61AD55138FC52B6EF9";
		public static final String WEEKLY = "65D8EAEF659E4a6889FDDBA22F20F2B5";
		public static final String MONTHLY = "878D491B5AC948cbA2871EBC61CDB37A";
	}

	public final class ConnectionFailureTypes {
		public static final String SOCKET_CONNECT_ERROR = "da940a38bb2843a39fa9446be13d16ba";
		public static final String DCXP_TIMEOUT = "092ad8db3ae84e76b3a32c7c92f3bed2";
		public static final String INVALID_GATEWAY_VERSION = "78d16dea04c345dfb83ac65b3cb8f7af";
		public static final String GENERAL_FAILURE = "aafb988d858b43a28ff138316d8b9a04";
		public static final String FTP_SOCKET_CONNECT_ERROR = "aa8b96b428f149e3b18d97eced73a922";
	}

	public final class MBusAuthenticationTypes {
		public static final String PASSED = "1687d9c9bb6246f9a95823483640659f";
		public static final String ID_FAILURE = "0075a0ddeb6f4be7ab9d81a05f5e6ea5";
		public static final String DATE_FAILURE = "41c58e7fa72b41c3856dd69cb99ef5fb";
		public static final String PASSED_WITH_ALTERNATE_DATE = "b8f1e12cfd3b49b285fd21fc123d8637";
	}

	public final class PerformanceLogTypes {
		public static final String SERVER = "e99171ad6e694b61aa342b3f9517b0f7";
		public static final String DC1000_ADAPTER = "363e10ad751e4a8fb69d0245ef74b0c8";
	}

	public final class CommunicationFailureStatusTypes {
		public static final String FAILURE = "2C095BAF40844945AD460A3B5C84E3F8";
		public static final String NO_FAILURE = "47CBD5BBF3F14bf9A7B6C6BB284F62F3";
	}

	public final class ServerToGatewayProtocolTypes {
		public static final String TCP = "044d52791fbf427eab4482c188bf2d37";
		public static final String FTP = "7d4a47546e4f4a4090d97da789c9f40e";
	}

	public final class DC1000ResourceEntryPriorities {
		public static final String NORMAL = "0245574809e9419ab834497ca5e7a1e4";
		public static final String HIGH = "e9507e718cf64754b2e8a3fe0dd6e132";
	}

	public final class AgentTypes {
		public static final String DEVICE = "314B72A03CDC40ad95A625CB0602182F";
		public static final String UNKNOWN_DEVICE = "903D59A945604ec8ADE822C0EDC976C0";
		public static final String GATEWAY = "191F595B65E248749BCDDDAD35106E8C";
	}

	public final class PrepayAddCreditOptionTypes {
		public static final String ADD_PREPAY_CREDIT_ONLY = "1D9860F62F304615BA3AE9CB7FC0B2C1";
		public static final String ADD_PREPAY_EMERGENCY_CREDIT_ONLY = "70D5E5497A744f7b90038088C2597C71";
		public static final String ADD_PREPAY_EMERGENCY_CREDIT_THEN_PREPAY_CREDIT = "98079381CD724f32B5CD8FD898AE6EBC";
	}

	public final class PrepayClearCreditOptionTypes {
		public static final String CLEAR_PREPAY_CREDIT_ONLY = "6365823F41B84992B52DD1DF958A6D5F";
		public static final String CLEAR_PREPAY_EMERGENCY_CREDIT_ONLY = "D5DC7740A29A4bf5A49DFE172F877C8B";
		public static final String CLEAR_PREPAY_CREDIT_AND_PREPAY_EMERGENCY_CREDIT = "BFBAD7349E9A456aBCDDCE0D0F60A53C";
	}

	public final class PrepayStatusTypes {
		public static final String ENABLED = "A3C2AD09987941b6A379763A97E4B540";
		public static final String DISABLED = "059C0FA1653B495c834738A7A215321C";
	}

	public final class PrepayReversePowerDeductionStatusTypes {
		public static final String ENABLED = "4388337E206C4552B7C3DF857EFB11E6";
		public static final String DISABLED = "5C7410D887034eef9B6F14CF44739D0E";
	}

	public final class PrepayAudibleAlarmStatusTypes {
		public static final String ENABLED = "F5045FD794EA46bbB295B26E0F72955B";
		public static final String DISABLED = "8913D939B4824b88A4950AF798BED820";
	}

	public final class PrepayMaximumPowerStatusTypeID {
		public static final String ENABLED = "deb4566e07d147b48d3700f49f6e34b2";
		public static final String DISABLED = "f19abc0c5b924234b99a0a10de7ec812";
	}

	public final class PrepayEmergencyCreditStatusTypes {
		public static final String ENABLED = "C60B74ED2D9E430c95B445964BD916AB";
		public static final String DISABLED = "48DB020F29134130B3F14214E8191B53";
	}

	public final class PrepayCreditTypes {
		public static final String REGULAR = "75ac11b6ead14c32ae8fac863bc00dd9";
		public static final String EMERGENCY = "8b69a66966bf47d5aa644cfca7d992e1";
	}

	public final class DeviceTaskTypes {
		public static final String READ_FULL_LOAD_PROFILE = "ffedafe3328c45f3ad01a6fe01290d84";
		public static final String CONNECT_LOAD = "9b076bc9903444899ff2734889aa9086";
		public static final String DISCONNECT_LOAD = "2fab533d860f4cbe9bf85f418e36093a";
		public static final String READ_BILLING_DATA_ON_DEMAND = "6eee1c4551484aa68384b84ef265b088";
		public static final String SET_PRIMARY_MAXIMUM_POWER_LEVEL = "e507e37bb1534d1d98b686efab3fcd2f";
		public static final String SET_MAXIMUM_POWER_LEVEL_DURATION = "c847892e95264216be97cba84388bf0a";
		public static final String READ_PRIMARY_MAXIMUM_POWER_LEVEL = "53dab6299f8548d1b4252955ae452347";
		public static final String READ_MAXIMUM_POWER_LEVEL_DURATION = "b1bd1d54ed5e4b3782b45a31d3dd9b10";
		public static final String SET_TOU_SEASON_SCHEDULES = "018DAF857332403e8EEDAF88AAD7F796";
		public static final String SET_TOU_DAY_SCHEDULES = "E7F9B97C3CFE4b09A94A103F2DED1067";
		public static final String SET_TOU_RECURRING_DATES = "FCDED048A6BF4b8a8FEA2004F40D8D23";
		public static final String READ_TOU_DAY_SCHEDULES = "3080861AAAA1493781605E11B5F6C24F";
		public static final String READ_TOU_SEASON_SCHEDULES = "B32962D9E0234f25A61963B03002E66D";
		public static final String READ_TOU_RECURRING_DATES = "B438B71FD3624ab994D596B29DAD6F84";
		public static final String SET_PRIMARY_MAXIMUM_POWER_STATUS = "6c0dd22755ca4ac69ee3ca7708f8ffdb";
		public static final String READ_SELF_BILLING_DATA = "a92907a371774a748829de52f5ca45bc";
		public static final String READ_SECONDARY_BILLING_REGISTERS = "70818b636c55499f80c2b6028e3861ff";
		public static final String DISCONNECT_CONTROL_RELAY = "a0bfeb73f357479f8bf18c33588f2141";
		public static final String CONNECT_CONTROL_RELAY = "8d684c709dde4ee08c06a574ce62a501";
		public static final String SET_CONTROL_RELAY_TIERS_STATUS = "bffd077bbe6a4d028610bd6617497197";
		public static final String SET_CONTROL_RELAY_TIERS = "ca4c2d1dde0e46e6b54f4b5647875e00";
		public static final String SET_LOAD_PROFILE_CONFIGURATION = "d614b22684cf45d6bc50363b9a6f245e";
		public static final String UPDATE_FIRMWARE = "dd2aa43bd23e4855946785e535e610f0";
		public static final String READ_DISPLAY_CONFIGURATION = "07A8F21239464ab4875C835085B9F495";
		public static final String SET_DISPLAY_CONFIGURATION = "8B66865C7FC54a3aBBAD08738768BC11";
		public static final String SET_ALARM_DISPLAY_CONFIGURATION = "1C2C078AE4B147ddB9CA07F214D3CA0C";
		public static final String READ_PRIMARY_MAXIMUM_POWER_STATUS = "59C52D3F7CF545b2A11F48A45B04FA2C";
		public static final String READ_POWER_QUALITY = "88d02253387e4a789cf721580e0578ed";
		public static final String READ_POWER_QUALITY_OUTAGE_DURATION_THRESHOLD = "261A278CCE224ffeB08051F0E1641365";
		public static final String READ_POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD = "08BAE067222749448FCCADDE09B74A8F";
		public static final String READ_POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD = "DCA6CABC85C8458387EEEA8A1FFF40DB";
		public static final String READ_POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD = "00EEE598B2AC4934A501778C40EDC6BC";
		public static final String READ_POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD = "475316B0A63A4f0d9EBA5E8BF98789B2";
		public static final String SET_POWER_QUALITY_OUTAGE_DURATION_THRESHOLD = "91407B2D6D1B48a1BA4713B094A5949F";
		public static final String SET_POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD = "7A6BD5F1966848a6AF91D9E7DECEE25F";
		public static final String SET_POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD = "A76182A992FD4932B2973DB880AD8585";
		public static final String SET_POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD = "494F9D8827044730B9786B74B82B94C4";
		public static final String SET_POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD = "2A5395280E8F4a1fAEDF3CFAA2E918D9";
		public static final String READ_PULSE_INPUT_CONFIGURATION = "1170e680261f4d75a72e9ca74ae0d7d2";
		public static final String READ_INSTANTANEOUS_POWER = "6659D10F0962447cBD07E169B65D8001";
		public static final String READ_CONTROL_RELAY = "e44f1020a7a14d679d596f5c655bffcb";
		public static final String READ_LOAD_STATUS = "73809a395bbc447a87673bde6739c952";
		public static final String READ_FIRMWARE_VERSION = "ca1ca2cd30444b4a84a0245e9c86f8d2";
		public static final String SET_PULSE_INPUT_CONFIGURATION = "edf915ca40ca44cfbbdd667603138967";
		public static final String READ_ACTIVE_TOU_CALENDAR = "0511085D4DC9442c966106842AB7A6C9";
		public static final String READ_PENDING_TOU_CALENDAR = "1537F6D9BC3C43a697960DB3A5365A35";
		public static final String SET_PENDING_TOU_CALENDAR = "4AE8A9D89305472490D20336FF25AC7E";
		public static final String READ_DOWN_LIMIT = "a9d73ac2abc94ebdbee8731284aebf45";
		public static final String SET_DOWN_LIMIT = "bb45fc6e85c241808a43c770927a3f5b";
		public static final String SET_DATE_TIME = "89c51f87d84847f5a1ea986b7d67638b";
		public static final String READ_DELTA_LOAD_PROFILE = "dc126b9a6224447b901b42687d79f1f2";
		public static final String READ_CONTINUOUS_DELTA_LOAD_PROFILE = "4b6c5b0f93a6427687c8f6d418dd4676";
		public static final String READ_MEP_CARD_CONFIGURATION = "c44d0adfd2444207848b6a35a624a997";
		public static final String READ_STATISTICS = "92ee7e0e0a00414dadb1a199280ec70c";
		public static final String READ_EVENT_LOG = "b1548023071d4fba814a53000a8e5689";
		public static final String READ_ALARM_POLLING_RATE = "f6a27f0049e94f4aa7f944be84699648";
		public static final String SET_ALARM_POLLING_RATE = "92df0f15a4a24429aa566984ee457639";
		public static final String READ_BILLING_SCHEDULE = "78BC0D4C6E684dc3A3AE8242909E114A";
		public static final String SET_BILLING_SCHEDULE = "AE7B6DC406FD40c28CADAF95A74AA7FE";
		public static final String READ_HISTORICAL_BILLING_DATA = "1ff26ee6f4a649eca54d5ce3ee3c780c";
		public static final String READ_AUTO_DISCOVERY_CONFIGURATION = "cadecfc3478d49cca08c6433f7e8a425";
		public static final String SET_AUTO_DISCOVERY_CONFIGURATION = "01de8bb8cfb440a4b292c42708f7cd55";
		public static final String READ_UTILITY_INFORMATION = "d39e9a3a80c0414d85a4bd758091def7";
		public static final String SET_UTILITY_INFORMATION = "b07031d448e740cf90479f6752a9da8e";
		public static final String ADD_PREPAY_CREDIT = "43BD199A540B4f5d98F538C3C149D995";
		public static final String CLEAR_PREPAY_CREDIT = "74012F0E198F4f399ED988C8A21C3BBC";
		public static final String READ_PREPAY_CONFIGURATION = "2669D04D2A5046b18F25710C35C38BC5";
		public static final String SET_PREPAY_CONFIGURATION = "950D0039DE214b42A2888CE294F78ED4";
		public static final String READ_TIME_ZONE_CONFIGURATION = "166fda8672174213823a41c5c77fc9aa";
		public static final String SET_TIME_ZONE_CONFIGURATION = "b40fdef551a84e17b4116fd17e9c059b";
		public static final String READ_PREPAY_CREDIT = "031210f70bec420e8faccea7ba8affa1";
		public static final String SET_MAXIMUM_POWER_LEVEL = "e507e37bb1534d1d98b686efab3fcd2f";
		public static final String READ_MAXIMUM_POWER_LEVEL = "53dab6299f8548d1b4252955ae452347";
		public static final String SET_MAXIMUM_POWER_STATUS = "6c0dd22755ca4ac69ee3ca7708f8ffdb";
		public static final String READ_MAXIMUM_POWER_STATUS = "59C52D3F7CF545b2A11F48A45B04FA2C";
	}

	public final class GatewayTaskTypes {
		public static final String SET_WAN_PHONE_NUMBER = "4eafe7b8e63c4ca39a1dafa921db9595";
		public static final String UPDATE_FIRMWARE = "5797529de55d4598bdbb1565ef64c862";
		public static final String SET_PPP_USERNAME = "929AEE135E1B4ef3A32DFECC83977724";
		public static final String SET_PPP_PASSWORD = "5F7274D13AC143cc86B8DC20FC2ED491";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_INIT_STRING = "50328816894c43d79271e6b950ffc4b5";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_TYPE = "1e6470dd541d4363984a28e1a0ad3bb6";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_1 = "8b9beba68b3944c7948c0a883cbf445b";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_2 = "0b40b4c0c0dc41689e3eeb4b1e8b71d2";
		public static final String READ_GATEWAY_TO_SERVER_IP_ADDRESS = "91c7803e15944c999ca4410755e74a53";
		public static final String SET_GATEWAY_TO_SERVER_IP_ADDRESS = "d64100d65024434bbdff2be23d7d5c64";
		public static final String SET_IP_ADDRESS = "0f12e4bcf7854b218c44747555a46784";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_1 = "dbfde8dd7dd943ceb42c0781a6b571ab";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_2 = "8f54808f5af543499fab4f99c1147964";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_TYPE = "969d665c8da34995999d6102c0d9c548";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_INIT_STRING = "dab6e63d5a254565a8f83d7ff0a4ca85";
		public static final String READ_STATISTICS = "0b31c2f49a4e467fb20f9241153507ea";
		public static final String SET_TOTAL_ENERGY_STATUS = "c2ee73bee9b847fb803ddac8a4d51b1e";
		public static final String SET_SECURITY_OPTIONS = "60e8a8beeb314ce7b5409c4aa693eeae";
		public static final String BROADCAST_DISCONNECT_CONTROL_RELAY = "8836af9905b448b1a2b30dbeb5e052fe";
		public static final String BROADCAST_CONNECT_CONTROL_RELAY = "d186b04ff2b14172abaa3f2a1a6c788e";
		public static final String BROADCAST_SET_PRIMARY_MAXIMUM_POWER_STATUS = "4b69b9e8e6f5401e8b3224649e264c14";
		public static final String READ_FIRMWARE_VERSION = "9d4b5062406b4e37bbfa1e70e9fbd318";
		public static final String READ_REPEATER_PATHS = "15357b6ee3ec46b082e2da8d3c296134";
		public static final String REBOOT = "89e6c8b9963d482b8d87cdf39fa1910c";
		public static final String DELETE_WAN_CONFIGURATION = "ee384c36513448e689f30a26a443ced5";
		public static final String SET_DEVICE_LIMIT = "3c25bdd7fb2e4f88bf96a9132d766a28";
		public static final String READ_DEVICE_LIMIT = "632c4e86aaa84595a07f6f06e8563c9b";
		public static final String SET_PROCESS_CONFIGURATION = "d9d04314b4c447408c4569723777ef60";
		public static final String READ_PROCESS_CONFIGURATION = "ad3a490bd4934f239056a25cdd609324";
		public static final String READ_DISCOVERED_DEVICES = "7700d7a39b24481dab4990980bb4e0da";
		public static final String READ_WAN_CONFIGURATION = "9340c39dac934866aa9c9b459bdf9baf";
		public static final String SET_WAN_CONFIGURATION = "50B7EE5AB227474e9C18361ABEEE6FC6";
		public static final String BROADCAST_SET_MAXIMUM_POWER_LEVEL_STATUS = "4b69b9e8e6f5401e8b3224649e264c14";
	}

	public final class InternalTaskTypes {
		public static final String SCHEDULE = "548D93B15FAE46f1BBE0D8DB3E677D98";
		public static final String RECALCULATION = "E441F39AF80D45c994A652BCEFD2D5A7";
		public static final String DATAPOINT = "E86ACFED1B934f8bB884B2367204DB65";
		public static final String AGGREGATION = "35659D71EC254cabB7F60A8C07811F60";
		public static final String ILON100ADAPTER = "B4D8506E56374612A82DBFAB71842F2C";
		public static final String SCHEDULE_DELETED = "88a5a66d40484482956dca475671bd2c";
		public static final String SCHEDULE_DISABLED = "73de098b579b48edb0e0b8fe5f4e9b15";
		public static final String SCHEDULE_ENABLED = "313bf499ce134fda9c73404be963c05f";
		public static final String SCHEDULE_UPDATE = "361c871367834e279248f6bc613498c3";
		public static final String SCHEDULE_PROCESS_HEARTBEAT_FAILURE = "637d46a7059a4abb8b18112465ce0c61";
		public static final String SCHEDULE_PROCESS_SHUTDOWN = "1680D1D9D30F472cB1C47CE49F3EB28A";
		public static final String SCHEDULE_ASSIGNMENT_CREATED = "ea5e58d936e34325baf93f600c7766f9";
		public static final String SCHEDULE_ASSIGNMENT_REMOVED = "7f76c1dc81e145ea9feb4c8d8b95cf8a";
		public static final String SCHEDULE_OBJECT_DELETED = "8B50020541D84358A09CEAF24EE2DF53";
		public static final String SCHEDULE_OBJECT_ENABLED = "7cafcfdd77734b1f8295fb482912a5c6";
		public static final String SCHEDULE_OBJECT_DISABLED = "ddd6ce3366d844d0acd03e1447669d29";
		public static final String RECALCULATION_STARTED = "df6ee83b167f42b5bb155d334e0fe3a7";
		public static final String RECALCULATION_COMPLETED = "093ea585774e49838c656eec62ca5663";
		public static final String RECALCULATION_STOPPED = "fcd87ee72cc743628eb4ad6ecbf70796";
		public static final String ARCHIVE = "9084f2f2617e44e582a067466c27008d";
		public static final String ADD_METER_DEVICE = "af84239ee5594bd1b425544ec6769f52";
		public static final String MOVE_METER_DEVICE_ADD = "EF5EBA9624DB4b4e89CFE5CD4A136D7C";
		public static final String REMOVE_METER_DEVICE = "27d8944e70bc479ab84543036ad2707b";
		public static final String MOVE_METER_DEVICE_REMOVE = "14A9A96DCA2C42be90C087A66401F03C";
		public static final String EVENT = "e5ed464a3ce64930845eb69df383be87";
		public static final String READ_METER_DEVICE_FIRMWARE_AND_BOOTROM_VERSIONS = "e252b767c42a41a69e5c6ef3c57ef5df";
		public static final String CONNECT_GATEWAY = "c0eca309cb544ebb8fa6f3b7befb4f91";
		public static final String DISCONNECT_GATEWAY = "654bbb9e99e142a69bfee55399680434";
		public static final String CREATE_PPP_ACCOUNT = "C728EA59DE474bee987E9252D9EB113D";
		public static final String DELETE_PPP_ACCOUNT = "8CB7034AB826495888BC6163442249EC";
		public static final String CHECK_FOR_ORPHANED_CONNECTIONS = "4e33ea19ae754711aff6f1792fafe8d1";
		public static final String READ_METER_DEVICE_STATUS = "bb08706eb0274ac392047d67f3064fa8";
		public static final String READ_GATEWAY_VERSION = "035C0D9EA8CB4d6499D98BEF0CDE81E3";
		public static final String READ_METER_DEVICE_SOFTWARE_VERSION = "DE798DD0A022417f9C3A5BB6A05FCBF6";
		public static final String READ_METER_DEVICE_FIRMWARE_VERSION = "940E43D713594c45AEF877087535D7E6";
		public static final String SET_STOP_MODE = "abf544b3bc624adca066eba88369038a";
		public static final String CLEAR_METER_DEVICE_ALARMS = "8dc9a6a52cfa42d685430be251c8670a";
		public static final String SET_LOG_EVENTS_CONFIGURATION = "010FD66BA5C84b508952D5B5761CA21B";
		public static final String REMOVE_MBUS_DEVICE = "1eee13291f804e6692f7c64d252abd74";
		public static final String CLEAR_MBUS_DEVICE_ALARMS = "55031cf93b894e738712d0b1628d4ed6";
		public static final String CLEAR_EVENT_LOG_PENDING_OVERFLOW_ALARM = "9e335d44b6bd44218ac722a8191e6ca6";
		public static final String SYNCHRONIZE_METER_DEVICE_ALARMS = "5e9b7609a8c941488d281ddaeffb19bd";
		public static final String SYNCHRONIZE_MBUS_DEVICE_ALARMS = "0eb93548f5c7449e930d55f08955bbe8";
		public static final String READ_NEW_DISCOVERED_METER_DEVICES = "c600ac0fd58f42328ffaa4ee7774f5ae";
		public static final String TASK_TIMEOUT = "d67f265555e843e5aca2706fde0f0048";
	}

	public final class EngineIPAddressAssignmentTypeID {
		public static final String USER_ASSIGNED = "2c5067c5abc2476fa417dc2e40a68bb5";
		public static final String SERVER_ASSIGNED = "a7aa2e4af6924525b2ad156d5f6ea894";
	}

	public final class DeviceTestPointStatusTypes {
		public static final String NOT_A_TEST_POINT = "fd2c785c0d6b4cc380ac630267d65b85";
		public static final String AUTOMATIC_TEST_POINT = "baf84a58ee1347d1858c242248fee59d";
		public static final String CONFIRMED_TEST_POINT = "f33eb2a356744d5ea70e09d4939def7d";
		public static final String REMOVING_TEST_POINT = "465f506122bc43aab25d15e0453ccead";
	}

	public final class GatewayMeshDetectionStatusTypes {
		public static final String DETECTED_MESH = "7adbd0d225fa48b2a38403f75c193853";
		public static final String DETECTED_NON_MESH = "c44f21d8e8824c86975c5fde82fe1f69";
	}

	public final class MaximumPowerTypes {
		public static final String PRIMARY = "ECDC678900904377959DEFDA361DB929";
		public static final String PREPAY = "C90A51D93AD14770A95CB78A2A58731A";
	}

	public final class ReadTOUCalendarOptionTypes {
		public static final String READ_ENTIRE_CALENDAR = "10DBB92E49AA4ed08A2C2C57BF1A24F1";
		public static final String READ_CALENDAR_ID_ONLY = "F93EBDD5AFCA41629CE2688FDE6EEC95";
	}

	public final class ConnectRequestSourceTypes {
		public static final String CORE_SERVICES_API = "598eaa8f38b84a5d8a91ecece29d08fd";
		public static final String SCHEDULED_COMMUNICATION = "80516e311e204596975123e8567a58ab";
		public static final String GATEWAY = "fc00670f7e7e4b35a2a61983b1e5a1ee";
	}

	public final class TestTcpIpPortStatusTypes {
		public static final String ENABLED = "37dc09364a0f48a79d4470999bcaa93d";
		public static final String DISABLED = "765fef3d066347099b6f0468303ce20b";
	}

	public final class PassiveFtpIpAddressSourceTypes {
		public static final String FTP_REPLY_IP_ADDRESS = "4dbac487e3744ba58f135a3223f7f64f";
		public static final String SERVER_ROUTABLE_IP_ADDRESS = "a41300f1defd4b4da496122c314aeaba";
	}

	public final class RetrieveByParameterIDTypes {
		public static final String GATEWAY_ID = "444b729f4d7a4b5e90f5296e7c62ed78";
		public static final String TRANSFORMER_ID = "0aadee3706ce479a9fbb769b91ee7cf9";
		public static final String NEURON_ID = "85a93675602b49a5a3ecbc7b66a513db";
		public static final String DEVICE_ID = "7bf34c6d91f64ff4904411b72874ddf9";
		public static final String SERIAL_NUMBER = "8448b06de2f34e838914d50021053ee8";
	}

	public final class EventDefinitionID {
		public static final String COMMAND_FAILURE = "7bd38a557c4641d7bebe59edbb328064";
		public static final String END_OF_BILLING_CYCLE_BILLING_DATA_AVAILABLE = "3b29e905f9764169a999cfc8ad527d2e";
		public static final String ON_DEMAND_BILLING_DATA_READ_COMMAND_COMPLETE = "f309efb1263c4706ac8c1e3a18ec9da9";
		public static final String DC1000_EVENT = "748af8ef281c4428b0e3f8cc2d8fe0cf";
		public static final String REQUEST_METER_LOAD_PROFILE_COMMAND_COMPLETE = "f33490cf1e484c4d817d27b125b1031f";
		public static final String POWER_QUALITY_COMMAND_COMPLETE = "10becfacb599459cacc920cb4fc6bce5";
		public static final String CONNECTION_ESTABLISHED = "16e7a62c458c48189d4ce6249264512d";
		public static final String CONNECTION_RELEASED = "cb9678d369b547d8bf4ee51e6df81455";
		public static final String ADD_METER_SUCCESS = "bffeafeef0964980926e5e0fc81cb55a";
		public static final String ADD_METER_FAILURE = "e895295532644b899216a2d647da6eb8";
		public static final String REMOVE_METER_SUCCESS = "fb1eb87c9def4190a121aaa2995627f5";
		public static final String REMOVE_METER_FAILURE = "5da8b450b7dd4cb2a982539934a4a2c6";
		public static final String UPDATE_GATEWAY_LOGIN_COMMAND_COMPLETE = "BCE91DAD62804a4e8824E7C3433A269E";
		public static final String UPDATE_GATEWAY_PASSWORD_COMMAND_COMPLETE = "5B4D3E50144D41f68EA1EAFC4EC32F21";
		public static final String REMOTE_METER_CONNECT_COMMAND_COMPLETE = "4bea21adc4474f5fa7f9e412b74d0d11";
		public static final String REMOTE_METER_DISCONNECT_COMMAND_COMPLETE = "416517694b9a482f87c5b81be3fee8fa";
		public static final String SET_MAXIMUM_POWER_LEVEL_COMMAND_COMPLETE = "93af3eebb36742a89d6a12cd9e7274b8";
		public static final String SET_MAXIMUM_POWER_LEVEL_DURATION_COMMAND_COMPLETE = "95e559c12e914ce6be7d199ad9f63c69";
		public static final String SET_SEASON_SCHEDULES_COMMAND_COMPLETE = "8F11B342841D404a91883626FF3D2704";
		public static final String SET_TOU_RECURRING_DATES_COMMAND_COMPLETE = "F039C7FDC7204c31A576C9AFC24BD951";
		public static final String SET_DAY_SCHEDULES_COMMAND_COMPLETE = "B5FDEFDF88E348f0824E329778BA78EF";
		public static final String RETRIEVE_DAY_SCHEDULES_COMMAND_COMPLETE = "270827C2F082486f81916DE63876D4AE";
		public static final String RETRIEVE_SEASON_SCHEDULES_COMMAND_COMPLETE = "9A9A5B023D5C41da9AF0506B271E9CF7";
		public static final String RETRIEVE_TOU_RECURRING_DATES_COMMAND_COMPLETE = "1AD9795CD5524895AD9B013570CA2ED9";
		public static final String ENGINE_STARTED = "a89ab0bab7344d26bc4236a0d2c309e1";
		public static final String ENGINE_STOPPED = "11d73ac3f2bb4093b447ba68f5266d8b";
		public static final String READ_MAXIMUM_POWER_LEVEL_COMMAND_COMPLETE = "164fb5c6de3546eba985de5d71e984a9";
		public static final String READ_MAXIMUM_POWER_LEVEL_DURATION_COMMAND_COMPLETE = "439b97c9dfdc499289128038ec83985e";
		public static final String DC_FIRMWARE_UPDATE_COMMAND_COMPLETE = "B2F41EB2832744a3AB03B2C1DFD48082";
		public static final String ENABLE_MAXIMUM_POWER_COMMAND_COMPLETE = "2bbb57575b8d49e980f1837e9682cc75";
		public static final String PERFORM_SELF_BILLING_READ_COMMAND_COMPLETE = "0a87ae5fbef54616a74faa3a653250ba";
		public static final String READ_SECONDARY_BILLING_REGISTERS_COMMAND_COMPLETE = "63b3fe0b77e841259d412763b349e2ff";
		public static final String OPEN_SECONDARY_CONTROL_OUTPUT_COMMAND_COMPLETE = "a1a15cdcb98546fb96c6564a288b930e";
		public static final String CLOSE_SECONDARY_CONTROL_OUTPUT_COMMAND_COMPLETE = "d5e307213a2f4452870ffd12ab1bf937";
		public static final String ENABLE_SECONDARY_CONTROL_OUTPUT_TIERS_COMMAND_COMPLETE = "9469ea5f72fe451c97a7f58690d57ebe";
		public static final String SET_SECONDARY_CONTROL_OUTPUT_TIERS_COMMAND_COMPLETE = "ca3a8bb6fbc14db9971f2824b0cbbbf2";
		public static final String SET_LOAD_PROFILE_CONFIGURATION_COMMAND_COMPLETE = "18e64ad80a2e4f3691b1065b40c6d3e4";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_INIT_STRING_COMMAND_COMPLETE = "acf0ea51d03d4f2f9220ceffc82ced3f";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_TYPE_COMMAND_COMPLETE = "469eef112d9c43f79d0debbceb4240ac";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_1_COMMAND_COMPLETE = "c63e377e9c0b4459b03f0afd60892004";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_2_COMMAND_COMPLETE = "1640aabde50f4121bf8939bb3f0b7640";
		public static final String READ_GATEWAY_TO_SERVER_IP_ADDRESS_COMMAND_COMPLETE = "2dd2f06283424a749fa4eeead0d54771";
		public static final String SET_GATEWAY_TO_SERVER_IP_ADDRESS_COMMAND_COMPLETE = "bcc89beebfc54943abae9ee1fd51b696";
		public static final String SET_SERVER_TO_GATEWAY_IP_ADDRESS_COMMAND_COMPLETE = "c452c6f1b95840b88a9a8f6ae1b72e52";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_1_COMMAND_COMPLETE = "0a6d540769554399aaa1022df954e6ee";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_2_COMMAND_COMPLETE = "0e71c1e80add4e049e239d49d23654cd";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_TYPE_COMMAND_COMPLETE = "518a7ae6cf3f487690fbecb284426909";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_INIT_STRING_COMMAND_COMPLETE = "8ed0a80aa9774f578479c0e6b5d2da89";
		public static final String READ_POWER_QUALITY_TIME_THRESHOLD_COMMAND_COMPLETE = "DF520B4557734f9e93120044480063A2";
		public static final String READ_POWER_QUALITY_SAG_THRESHOLD_COMMAND_COMPLETE = "CD02921B850748a18A463D24685FA3D6";
		public static final String READ_POWER_QUALITY_SURGE_THRESHOLD_COMMAND_COMPLETE = "AF1653E211064c4c821DC5F547D6337F";
		public static final String READ_POWER_QUALITY_OVERCURRENT_THRESHOLD_COMMAND_COMPLETE = "50A432F5323B4c1cA0B0F064791BA45C";
		public static final String SET_POWER_QUALITY_TIME_THRESHOLD_COMMAND_COMPLETE = "A2CE552EAD2C4a3cBA84B54CCAAD5AC1";
		public static final String SET_POWER_QUALITY_SAG_THRESHOLD_COMMAND_COMPLETE = "9AD8E6F84A1A40ccB5344A0D56B46776";
		public static final String SET_POWER_QUALITY_SURGE_THRESHOLD_COMMAND_COMPLETE = "F3DFBF4C5BE340a0BE795431B0EC375A";
		public static final String SET_POWER_QUALITY_OVERCURRENT_THRESHOLD_COMMAND_COMPLETE = "EF4DAD2B3E264e048D95718055531DA1";
		public static final String RETRIEVE_GATEWAY_PERFORMANCE_STATISTICS_COMMAND_COMPLETE = "cd091b7129aa410e987d00ee4a5e9f94";
		public static final String READ_DELTA_LOAD_PROFILE_COMMAND_COMPLETE = "8C55269E57A44edf88609794260AD857";
		public static final String READ_CONTINUOUS_DELTA_LOAD_PROFILE_COMMAND_COMPLETE = "AAC59D59669C4e7f9AC4E63782BD9F98";
		public static final String CONTINUOUS_DELTA_LOAD_PROFILE_DATA_AVAILABLE = "E73928E49A194259A6C9BA5C987CF3B7";
		public static final String SECURITY_ALERT = "3745625CAFA24ad989472607AB0A28E0";
		public static final String UPDATE_METER_FIRMWARE_COMMAND_COMPLETE = "4da60cb36956422086892e0baa077ac3";
		public static final String ENABLE_TOTAL_ENERGY_COMMAND_COMPLETE = "7da4d95936df4d5b8951828c562ce7b9";
		public static final String TOTAL_ENERGY_DATA_AVAILABLE = "a0e09a0454e04a89bceb469ef18b2348";
		public static final String SERVER_ERROR_EVENT = "e3501923ef634306b0c5592ff53b2f00";
		public static final String SET_SERVER_TO_GATEWAY_PHONE_NUMBER_COMMAND_COMPLETE = "338D71EE6D224a10AEA6142A9A3131E0";
		public static final String SET_SECURITY_OPTIONS_COMMAND_COMPLETE = "35a1980a0f574936b10d53df9bcf0a17";
		public static final String GATEWAY_IP_ADDRESS_CHANGED = "72dcafb9a1df45ae8beec1d1cfa7285f";
		public static final String READ_PULSE_INPUT_CONFIGURATION_COMMAND_COMPLETE = "6dc87cd602ff46fa920a072ee584e5e4";
		public static final String READ_INSTANTANEOUS_POWER_COMMAND_COMPLETE = "A5DB38E87B554ebe82058B4EB38CB58C";
		public static final String READ_SECONDARY_CONTROL_OUTPUT_RELAY_COMMAND_COMPLETE = "8895461b1adf4aa6ad538c22aec447ff";
		public static final String READ_PRIMARY_CONTROL_OUTPUT_COMMAND_COMPLETE = "9cd46282124b434499f7e02bf6d5a8d8";
		public static final String BROADCAST_OPEN_SECONDARY_CONTROL_OUTPUT_COMMAND_COMPLETE = "232cb0c5b7b74916b84e559d4f4c0f1f";
		public static final String BROADCAST_CLOSE_SECONDARY_CONTROL_OUTPUT_COMMAND_COMPLETE = "7a056e9329c2425aba609006727f34f9";
		public static final String BROADCAST_MAXIMUM_POWER_LEVEL_ENABLE_COMMAND_COMPLETE = "73fe67b9e6a74b58a3c2e7ee28aece07";
		public static final String READ_GATEWAY_FIRMWARE_VERSION_COMMAND_COMPLETE = "022d4e1cbdaa4890b509030354d5f67b";
		public static final String READ_METER_FIRMWARE_VERSION_COMMAND_COMPLETE = "194325c249b94c28a2125797d5acdf2f";
		public static final String SET_PULSE_INPUT_CONFIGURATION_COMMAND_COMPLETE = "1e2a6435c4d0467ea01b1e7868708b92";
		public static final String READ_ACTIVE_TOU_CALENDAR_COMMAND_COMPLETE = "AC8EB7183720417fA5083824F5336C39";
		public static final String READ_PENDING_TOU_CALENDAR_COMMAND_COMPLETE = "71887A7C756C42bdA83ACAB49FC099D8";
		public static final String SET_PENDING_TOU_CALENDAR_COMMAND_COMPLETE = "07660D8DEB4F427c9B4FF53E4E83DD41";
		public static final String READ_METER_DISPLAY_CONFIGURATION_COMMAND_COMPLETE = "737FFF081E4D47199D1350B354B43573";
		public static final String SET_METER_DISPLAY_CONFIGURATION_COMMAND_COMPLETE = "AF23E940CC134d45A0C6501CA4D44B0C";
		public static final String SET_ALARM_DISPLAY_CONFIGURATION_COMMAND_COMPLETE = "6D0A1C1D0C2E45e5BF8C87A383090EE4";
		public static final String READ_MAXIMUM_POWER_ENABLE_COMMAND_COMPLETE = "BBE31E1ECE9A44cc91BC66BA9AD89DEA";
		public static final String GENERAL_FAILURE_DATA_STILL_AVAILABLE = "ba19e6ab3fec45abac093bb7035ebbc1";
		public static final String UPDATE_GATEWAY_PHONENUMBER_COMMAND_COMPLETE = "338D71EE6D224a10AEA6142A9A3131E0";
	}

	public final class ResultTypes {
		public static final String END_OF_BILLING_CYCLE_BILLING_DATA = "d357ee7048cd4983a3ee896f0e49c562";
		public static final String LOAD_PROFILE = "60381e83d07945649a86c193cdc40d6b";
		public static final String ON_DEMAND_BILLING_DATA = "67a59c9ce2d64e98a8fe0bf34a0d8267";
		public static final String POWER_QUALITY_INFORMATION = "56f2cb69a8a44247be7a537b754f72af";
		public static final String SEASON_SCHEDULES = "C2F525891E70439d847AF31F77C09385";
		public static final String DAY_SCHEDULES = "A3DD474A61324fafB0BE2695DB7FC2A4";
		public static final String TOU_RECURRING_DATES = "37D73FDB453241ffA26B5D4BDFF0A33E";
		public static final String SELF_BILLING_READ_DATA = "ad85befc6cc448bf9863c5cb62007561";
		public static final String SECONDARY_BILLING_REGISTERS_DATA = "7cf449f68be04523b3fccef2a7781e4e";
		public static final String GATEWAY_PERFORMANCE_STATISTICS_DATA = "3a19bf68e06e4879a0cccc0754dae661";
		public static final String DELTA_LOAD_PROFILE = "1707758FC0134fc187B105D50BF33723";
		public static final String TOTAL_ENERGY_DATA = "4bdd989403aa4c1691fce3c104bf09c3";
		public static final String METER_DISPLAY_CONFIGURATION = "5FB2472783C745dc8B110BD8E116A99E";
		public static final String MAXIMUM_POWER_ENABLE = "C1340AE2C9B74d088D5B206CB8C8A5C2";
		public static final String PULSE_INPUT_CONFIGURATION = "5ddd67c34b0a419e8b7204d16c308e92";
		public static final String INSTANTANEOUS_POWER = "6072CB933A0840cbA8FEE91456A5F1CB";
		public static final String SECONDARY_CONTROL_OUPUT_RELAY_STATUS = "3b295e082b3d43d2bd2d3908ae7fe164";
		public static final String PRIMARY_CONTROL_OUTPUT_STATUS = "65621819be064241bee46d5a38a67f15";
		public static final String GATEWAY_FIRMWARE_VERSION = "5cc432d671ba465ebb6bd990b6901605";
		public static final String METER_FIRMWARE_VERSION = "5bc2f8cfdbc846148e517a2d9514248e";
		public static final String ACTIVE_TOU_CALENDAR = "b98a6c0ff148489fb4443f654f848bf6";
		public static final String PENDING_TOU_CALENDAR = "d57d9409bdbb425b8ab239c04120da78";
	}

	public final class DC1000DeviceStates {
		public static final String UP = "c4a83cbbb8ac4794ab59562bc4b88a32";
		public static final String DOWN = "587080ce53fb4a95ba6dc51838d410c9";
		public static final String CONFIRMED_DOWN = "af5fdf7f4c644936b131e3ac3d9d6926";
		public static final String NO_AGENT = "3680be224f2745e2a132c651d22666dd";
		public static final String NASCENT = "198044101bde44c483325d3a5803e407";
	}

	public final class SecondaryControlOutputRelayStatus {
		public static final String BROADCAST_OPEN = "d7ff0b7cdb6d491c90afd700cda4971b";
		public static final String BROADCAST_CLOSED = "15080352c0264373a97501ba9e40517f";
		public static final String OPEN = "6eebd8aceb784b1f826a6cdfaf4f69ae";
		public static final String CLOSED = "f8e560fe460142f996e8374a890161b4";
	}

	public final class MeterAuthenticationKeyStatus {
		public static final String PENDING = "dae97ad3037d479c84213122e322c187";
		public static final String LAST_KNOWN_GOOD = "4fd8350bf13844549eae72b0540f66cb";
	}

	public final class TaskTypes {
		public static final String SCHEDULES = "548D93B15FAE46f1BBE0D8DB3E677D98";
		public static final String RECALC = "E441F39AF80D45c994A652BCEFD2D5A7";
		public static final String DATAPOINT = "E86ACFED1B934f8bB884B2367204DB65";
		public static final String AGGREGATION = "35659D71EC254cabB7F60A8C07811F60";
		public static final String ILON100ADAPTER = "B4D8506E56374612A82DBFAB71842F2C";
		public static final String SCHEDULE_DELETE = "88a5a66d40484482956dca475671bd2c";
		public static final String SCHEDULE_DISABLE = "73de098b579b48edb0e0b8fe5f4e9b15";
		public static final String SCHEDULE_ENABLE = "313bf499ce134fda9c73404be963c05f";
		public static final String SCHEDULE_UPDATE = "361c871367834e279248f6bc613498c3";
		public static final String SCHEDULE_PROCESS_HEARTBEAT_FAILURE = "637d46a7059a4abb8b18112465ce0c61";
		public static final String SCHEDULE_PROCESS_SHUTDOWN = "1680D1D9D30F472cB1C47CE49F3EB28A";
		public static final String SCHEDULE_ASSIGNMENT_CREATED = "ea5e58d936e34325baf93f600c7766f9";
		public static final String SCHEDULE_ASSIGNMENT_REMOVED = "7f76c1dc81e145ea9feb4c8d8b95cf8a";
		public static final String SCHEDULE_OBJECT_DELETED = "8B50020541D84358A09CEAF24EE2DF53";
		public static final String SCHEDULE_OBJECT_ENABLED = "7cafcfdd77734b1f8295fb482912a5c6";
		public static final String SCHEDULE_OBJECT_DISABLED = "ddd6ce3366d844d0acd03e1447669d29";
		public static final String RECALCULATION_STARTED = "df6ee83b167f42b5bb155d334e0fe3a7";
		public static final String RECALCULATION_COMPLETE = "093ea585774e49838c656eec62ca5663";
		public static final String RECALCULATION_STOPPED = "fcd87ee72cc743628eb4ad6ecbf70796";
		public static final String ARCHIVE = "9084f2f2617e44e582a067466c27008d";
		public static final String ADD_METER = "af84239ee5594bd1b425544ec6769f52";
		public static final String MOVE_DEVICE_ADD = "EF5EBA9624DB4b4e89CFE5CD4A136D7C";
		public static final String REMOVE_METER = "27d8944e70bc479ab84543036ad2707b";
		public static final String MOVE_DEVICE_REMOVE = "14A9A96DCA2C42be90C087A66401F03C";
		public static final String EVENT = "e5ed464a3ce64930845eb69df383be87";
		public static final String READ_METER_FIRMWARE_AND_BOOTROM_VERSIONS = "e252b767c42a41a69e5c6ef3c57ef5df";
		public static final String CONNECT = "c0eca309cb544ebb8fa6f3b7befb4f91";
		public static final String DISCONNECT = "654bbb9e99e142a69bfee55399680434";
		public static final String CREATE_ENTRY = "C728EA59DE474bee987E9252D9EB113D";
		public static final String DELETE_ENTRY = "8CB7034AB826495888BC6163442249EC";
		public static final String UPDATE_ENTRY_PHONE_NUMBER = "4eafe7b8e63c4ca39a1dafa921db9595";
		public static final String CHECK_FOR_ORPHAN_CONNECTIONS = "4e33ea19ae754711aff6f1792fafe8d1";
		public static final String REQUEST_METER_LOAD_PROFILE = "TaskTypes.REQUEST_METER_FULL_LOAD_PROFILE";
		public static final String REMOTE_METER_CONNECT = "9b076bc9903444899ff2734889aa9086";
		public static final String REMOTE_METER_DISCONNECT = "2fab533d860f4cbe9bf85f418e36093a";
		public static final String READ_BILLING_DATA_ON_DEMAND = "6eee1c4551484aa68384b84ef265b088";
		public static final String UPDATE_GATEWAY_FIRMWARE = "5797529de55d4598bdbb1565ef64c862";
		public static final String SET_MAXIMUM_POWER_LEVEL = "e507e37bb1534d1d98b686efab3fcd2f";
		public static final String SET_MAXIMUM_POWER_LEVEL_DURATION = "c847892e95264216be97cba84388bf0a";
		public static final String CHANGE_PPP_LOGIN = "929AEE135E1B4ef3A32DFECC83977724";
		public static final String CHANGE_PPP_PASSWORD = "5F7274D13AC143cc86B8DC20FC2ED491";
		public static final String READ_MAXIMUM_POWER_LEVEL = "53dab6299f8548d1b4252955ae452347";
		public static final String READ_MAXIMUM_POWER_LEVEL_DURATION = "b1bd1d54ed5e4b3782b45a31d3dd9b10";
		public static final String SET_SEASON_SCHEDULES = "018DAF857332403e8EEDAF88AAD7F796";
		public static final String SET_DAY_SCHEDULES = "E7F9B97C3CFE4b09A94A103F2DED1067";
		public static final String SET_TOU_RECURRING_DATES = "FCDED048A6BF4b8a8FEA2004F40D8D23";
		public static final String RETRIEVE_DAY_SCHEDULES = "3080861AAAA1493781605E11B5F6C24F";
		public static final String RETRIEVE_SEASON_SCHEDULES = "B32962D9E0234f25A61963B03002E66D";
		public static final String RETRIEVE_TOU_RECURRING_DATES = "B438B71FD3624ab994D596B29DAD6F84";
		public static final String ENABLE_MAXIMUM_POWER = "6c0dd22755ca4ac69ee3ca7708f8ffdb";
		public static final String PERFORM_SELF_BILLING_READ = "a92907a371774a748829de52f5ca45bc";
		public static final String READ_SECONDARY_BILLING_REGISTERS = "70818b636c55499f80c2b6028e3861ff";
		public static final String OPEN_SECONDARY_CONTROL_OUTPUT = "a0bfeb73f357479f8bf18c33588f2141";
		public static final String CLOSE_SECONDARY_CONTROL_OUTPUT = "8d684c709dde4ee08c06a574ce62a501";
		public static final String ENABLE_SECONDARY_CONTROL_OUTPUT_TIERS = "bffd077bbe6a4d028610bd6617497197";
		public static final String SET_SECONDARY_CONTROL_OUTPUT_TIERS = "ca4c2d1dde0e46e6b54f4b5647875e00";
		public static final String SET_LOAD_PROFILE_CONFIGURATION = "d614b22684cf45d6bc50363b9a6f245e";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_INIT_STRING = "50328816894c43d79271e6b950ffc4b5";
		public static final String READ_GATEWAY_TO_SERVER_MODEM_TYPE = "1e6470dd541d4363984a28e1a0ad3bb6";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_1 = "8b9beba68b3944c7948c0a883cbf445b";
		public static final String READ_GATEWAY_TO_SERVER_PHONE_NUMBER_2 = "0b40b4c0c0dc41689e3eeb4b1e8b71d2";
		public static final String READ_GATEWAY_TO_SERVER_IP_ADDRESS = "91c7803e15944c999ca4410755e74a53";
		public static final String SET_GATEWAY_TO_SERVER_IP_ADDRESS = "d64100d65024434bbdff2be23d7d5c64";
		public static final String SET_SERVER_TO_GATEWAY_IP_ADDRESS = "0f12e4bcf7854b218c44747555a46784";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_1 = "dbfde8dd7dd943ceb42c0781a6b571ab";
		public static final String SET_GATEWAY_TO_SERVER_PHONE_NUMBER_2 = "8f54808f5af543499fab4f99c1147964";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_TYPE = "969d665c8da34995999d6102c0d9c548";
		public static final String SET_GATEWAY_TO_SERVER_MODEM_INIT_STRING = "dab6e63d5a254565a8f83d7ff0a4ca85";
		public static final String READ_GATEWAY_STATISTICS = "0b31c2f49a4e467fb20f9241153507ea";
		public static final String ENABLE_TOTAL_ENERGY = "c2ee73bee9b847fb803ddac8a4d51b1e";
		public static final String SET_SECURITY_OPTIONS = "60e8a8beeb314ce7b5409c4aa693eeae";
		public static final String UPDATE_METER_FIRMWARE = "dd2aa43bd23e4855946785e535e610f0";
		public static final String READ_METER_DISPLAY_CONFIGURATION = "07A8F21239464ab4875C835085B9F495";
		public static final String SET_METER_DISPLAY_CONFIGURATION = "8B66865C7FC54a3aBBAD08738768BC11";
		public static final String SET_ALARM_DISPLAY_CONFIGURATION = "1C2C078AE4B147ddB9CA07F214D3CA0C";
		public static final String READ_MAXIMUM_POWER_ENABLE = "59C52D3F7CF545b2A11F48A45B04FA2C";
		public static final String READ_METER_STATUS = "bb08706eb0274ac392047d67f3064fa8";
		public static final String READ_POWER_QUALITY_DATA = "88d02253387e4a789cf721580e0578ed";
		public static final String READ_POWER_QUALITY_OUTAGE_DURATION_THRESHOLD = "261A278CCE224ffeB08051F0E1641365";
		public static final String READ_POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD = "08BAE067222749448FCCADDE09B74A8F";
		public static final String READ_POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD = "DCA6CABC85C8458387EEEA8A1FFF40DB";
		public static final String READ_POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD = "00EEE598B2AC4934A501778C40EDC6BC";
		public static final String READ_POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD = "475316B0A63A4f0d9EBA5E8BF98789B2";
		public static final String SET_POWER_QUALITY_OUTAGE_DURATION_THRESHOLD = "91407B2D6D1B48a1BA4713B094A5949F";
		public static final String SET_POWER_QUALITY_SAG_SURGE_DURATION_THRESHOLD = "7A6BD5F1966848a6AF91D9E7DECEE25F";
		public static final String SET_POWER_QUALITY_SAG_VOLTAGE_PERCENT_THRESHOLD = "A76182A992FD4932B2973DB880AD8585";
		public static final String SET_POWER_QUALITY_SURGE_VOLTAGE_PERCENT_THRESHOLD = "494F9D8827044730B9786B74B82B94C4";
		public static final String SET_POWER_QUALITY_OVERCURRENT_PERCENT_THRESHOLD = "2A5395280E8F4a1fAEDF3CFAA2E918D9";
		public static final String READ_PULSE_INPUT_CONFIGURATION = "1170e680261f4d75a72e9ca74ae0d7d2";
		public static final String READ_INSTANTANEOUS_POWER = "6659D10F0962447cBD07E169B65D8001";
		public static final String READ_SECONDARY_CONTROL_OUTPUT_RELAY = "e44f1020a7a14d679d596f5c655bffcb";
		public static final String READ_PRIMARY_CONTROL_OUTPUT = "73809a395bbc447a87673bde6739c952";
		public static final String BROADCAST_OPEN_SECONDARY_CONTROL_OUTPUT = "8836af9905b448b1a2b30dbeb5e052fe";
		public static final String BROADCAST_CLOSE_SECONDARY_CONTROL_OUTPUT = "d186b04ff2b14172abaa3f2a1a6c788e";
		public static final String BROADCAST_MAXIMUM_POWER_LEVEL_ENABLE = "4b69b9e8e6f5401e8b3224649e264c14";
		public static final String READ_GATEWAY_FIRMWARE_VERSION = "9d4b5062406b4e37bbfa1e70e9fbd318";
		public static final String READ_METER_FIRMWARE_VERSION = "ca1ca2cd30444b4a84a0245e9c86f8d2";
		public static final String GET_GATEWAY_VERSION = "035C0D9EA8CB4d6499D98BEF0CDE81E3";
		public static final String RETRIEVE_METER_SOFTWARE_VERSION = "DE798DD0A022417f9C3A5BB6A05FCBF6";
		public static final String RETRIEVE_METER_FIRMWARE_VERSION = "940E43D713594c45AEF877087535D7E6";
		public static final String SET_PULSE_INPUT_CONFIGURATION = "edf915ca40ca44cfbbdd667603138967";
		public static final String READ_ACTIVE_TOU_CALENDAR = "0511085D4DC9442c966106842AB7A6C9";
		public static final String READ_PENDING_TOU_CALENDAR = "1537F6D9BC3C43a697960DB3A5365A35";
		public static final String SET_PENDING_TOU_CALENDAR = "4AE8A9D89305472490D20336FF25AC7E";
		public static final String SET_STOP_MODE = "abf544b3bc624adca066eba88369038a";
		public static final String READ_DEVICE_DOWN_LIMIT = "a9d73ac2abc94ebdbee8731284aebf45";
		public static final String SET_DEVICE_DOWN_LIMIT = "bb45fc6e85c241808a43c770927a3f5b";
		public static final String SET_DEVICE_DATE_TIME = "89c51f87d84847f5a1ea986b7d67638b";
		public static final String READ_REPEATER_PATHS = "15357b6ee3ec46b082e2da8d3c296134";
		public static final String CLEAR_METER_ALARMS = "8dc9a6a52cfa42d685430be251c8670a";
		public static final String REBOOT_GATEWAY = "89e6c8b9963d482b8d87cdf39fa1910c";
		public static final String REQUEST_METER_FULL_LOAD_PROFILE = "ffedafe3328c45f3ad01a6fe01290d84";
		public static final String REQUEST_METER_DELTA_LOAD_PROFILE = "dc126b9a6224447b901b42687d79f1f2";
		public static final String REQUEST_METER_CONTINUOUS_DELTA_LOAD_PROFILE = "4b6c5b0f93a6427687c8f6d418dd4676";
		public static final String SET_LOG_EVENTS_CONFIGURATION = "010FD66BA5C84b508952D5B5761CA21B";
		public static final String REMOVE_MBUS = "1eee13291f804e6692f7c64d252abd74";
		public static final String DELETE_GATEWAY_WAN_CONFIGURATION = "ee384c36513448e689f30a26a443ced5";
		public static final String READ_MEP_CARD_CONFIGURATION = "c44d0adfd2444207848b6a35a624a997";
		public static final String SET_GATEWAY_DEVICE_LIMIT = "3c25bdd7fb2e4f88bf96a9132d766a28";
		public static final String READ_GATEWAY_DEVICE_LIMIT = "632c4e86aaa84595a07f6f06e8563c9b";
		public static final String CLEAR_MBUS_ALARMS = "55031cf93b894e738712d0b1628d4ed6";
		public static final String READ_DEVICE_STATISTICS = "92ee7e0e0a00414dadb1a199280ec70c";
		public static final String READ_DEVICE_EVENT_LOG = "b1548023071d4fba814a53000a8e5689";
		public static final String CLEAR_EVENT_LOG_PENDING_OVERFLOW_ALARM = "9e335d44b6bd44218ac722a8191e6ca6";
		public static final String SYNCHRONIZE_DEVICE_ALARMS = "5e9b7609a8c941488d281ddaeffb19bd";
		public static final String SYNCHRONIZE_MBUS_DEVICE_ALARMS = "0eb93548f5c7449e930d55f08955bbe8";
		public static final String SET_GATEWAY_PROCESS_CONFIGURATION = "d9d04314b4c447408c4569723777ef60";
		public static final String READ_GATEWAY_PROCESS_CONFIGURATION = "ad3a490bd4934f239056a25cdd609324";
		public static final String READ_GATEWAY_DISCOVERED_DEVICES = "7700d7a39b24481dab4990980bb4e0da";
		public static final String READ_NEW_DISCOVERED_DEVICES = "c600ac0fd58f42328ffaa4ee7774f5ae";
		public static final String READ_DEVICE_ALARM_POLLING_RATE = "f6a27f0049e94f4aa7f944be84699648";
		public static final String SET_DEVICE_ALARM_POLLING_RATE = "92df0f15a4a24429aa566984ee457639";
		public static final String READ_DEVICE_BILLING_SCHEDULE = "78BC0D4C6E684dc3A3AE8242909E114A";
		public static final String SET_DEVICE_BILLING_SCHEDULE = "AE7B6DC406FD40c28CADAF95A74AA7FE";
		public static final String READ_DEVICE_HISTORICAL_BILLING_DATA = "1ff26ee6f4a649eca54d5ce3ee3c780c";
		public static final String READ_GATEWAY_WAN_CONFIGURATION = "9340c39dac934866aa9c9b459bdf9baf";
		public static final String SET_GATEWAY_WAN_CONFIGURATION = "50B7EE5AB227474e9C18361ABEEE6FC6";
		public static final String READ_DEVICE_AUTO_DISCOVERY_CONFIGURATION = "cadecfc3478d49cca08c6433f7e8a425";
		public static final String SET_DEVICE_AUTO_DISCOVERY_CONFIGURATION = "01de8bb8cfb440a4b292c42708f7cd55";
		public static final String READ_DEVICE_UTILITY_INFORMATION = "d39e9a3a80c0414d85a4bd758091def7";
		public static final String SET_DEVICE_UTILITY_INFORMATION = "b07031d448e740cf90479f6752a9da8e";
		public static final String TASK_TIMEOUT = "d67f265555e843e5aca2706fde0f0048";
		public static final String RETRIEVE_GATEWAY_PERFORMANCE_STATISTICS = "0b31c2f49a4e467fb20f9241153507ea";
		public static final String READ_POWER_QUALITY_TIME_THRESHOLD = "261A278CCE224ffeB08051F0E1641365";
		public static final String READ_POWER_QUALITY_SAG_THRESHOLD = "DCA6CABC85C8458387EEEA8A1FFF40DB";
		public static final String READ_POWER_QUALITY_SURGE_THRESHOLD = "00EEE598B2AC4934A501778C40EDC6BC";
		public static final String READ_POWER_QUALITY_OVERCURRENT_THRESHOLD = "475316B0A63A4f0d9EBA5E8BF98789B2";
		public static final String SET_POWER_QUALITY_TIME_THRESHOLD = "91407B2D6D1B48a1BA4713B094A5949F";
		public static final String SET_POWER_QUALITY_SAG_THRESHOLD = "A76182A992FD4932B2973DB880AD8585";
		public static final String SET_POWER_QUALITY_SURGE_THRESHOLD = "494F9D8827044730B9786B74B82B94C4";
		public static final String SET_POWER_QUALITY_OVERCURRENT_THRESHOLD = "2A5395280E8F4a1fAEDF3CFAA2E918D9";
		public static final String ENABLE_METER = "b6d3a13e7d034b11be859943e6ae9387";
		public static final String ENABLE_MBUS_DEVICES = "f093b806d6884baaa39b08682a0847b5";
		public static final String READ_DEVICE_PREPAY_MAXIMUM_POWER_STATUS = "598d24405eec4a94b575388080f067cd";
		public static final String SET_DEVICE_PREPAY_MAXIMUM_POWER_STATUS = "29f68b0428e24849bf689219b8b1a172";
		public static final String READ_DEVICE_PREPAY_MAXIMUM_POWER_LEVEL = "bbff1fd404e243be979338f9cdffb27e";
		public static final String SET_DEVICE_PREPAY_MAXIMUM_POWER_LEVEL = "ff2948952118428aa5e93220e7edd6f1";
	}

	public final class CommissionStatusTypes {
		public static final String COMMISSION_NOT_COMPLETE = "C6FF1B4DD3BC4e29A43A09C6483EDBDA";
		public static final String INITIAL_COMMISSION_COMPLETE = "87A4CE6351E54974B4FE216261C89940";
		public static final String MULTIPLE_COMMISSIONS_COMPLETE = "9BC3BD5429E4498089D7DC1F52B7CFA9";
	}
}
