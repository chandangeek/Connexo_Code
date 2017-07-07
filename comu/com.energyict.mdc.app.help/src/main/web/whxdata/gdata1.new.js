/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

(function () {
    var glossary = {
        "type": "data",
        "entrys": [{"type": "entry", "name": "authentication", "value": "Ensures that both ends of the connection are identified and allowed to communicate."}, {
            "type": "entry",
            "name": "auto-action",
            "value": "Are automated activities that ensure the device is ready for the next state."
        }, {
            "type": "entry",
            "name": "channel",
            "value": "A channel is a representation of metered data (a single series of data), where each value is an exact amount of time apart from its neighbouring value. (e.g. 15 minutes)."
        }, {
            "type": "entry",
            "name": "communication port",
            "value": "A communication port is a physical modem (GSM, PSTN, etc.) or a logical connection (TCP, UDP, LAN, etc.). A communication port sets up a connection between the communication server and the device."
        }, {
            "type": "entry",
            "name": "communication port pool",
            "value": "A communications port pool is a set of communication ports. The communication port pools can be grouped in ‘Inbound port pools’ and ‘Outbound port pools’. Typically communication port pools are spread over different communication servers."
        }, {
            "type": "entry",
            "name": "communication protocol",
            "value": "A communication protocol defines the syntax of communication between different systems (e.g. between a communication server and a device) It consists of a system of message formats and rules for exchanging those messages. A single protocol can be used for multiple physical device models."
        }, {
            "type": "entry",
            "name": "communication schedule",
            "value": "A communication schedule defines the time and recurrence for reading a particular set of meter data from a device or a group of devices."
        }, {
            "type": "entry",
            "name": "communication server",
            "value": "Communication servers are the part of the system responsible for communicating with various devices in the field. A communication server is capable of communicating with several devices simultaneously using ports and pools of ports. It reads data from, writes data to or performs actions on these devices."
        }, {
            "type": "entry",
            "name": "communication task",
            "value": "Is a group of commands the user defines and communicates with the device to make the device carry out a specific action, e.g. read registers, reset the clock, update firmware, etc."
        }, {
            "type": "entry",
            "name": "connection method",
            "value": "Allows you to define how to communicate with the device. Some devices can support multiple types of connection methods (GRPS, optical, SMS, etc.)."
        }, {
            "type": "entry",
            "name": "data logger",
            "value": "A data logger is a device that stores data from other devices in its own channels. You do not know upfront which data these slave devices will have. The data logger does not save information of its data logger slave devices. You do not have to specify the slave devices before you can collect data, you can link the slave devices later."
        }, {"type": "entry", "name": "data source", "value": "Registers, load profiles and logbooks"}, {
            "type": "entry",
            "name": "device",
            "value": "A device represents a physical element (in the grid), this can be e.g. a meter, concentrator, gateway, in home display, etc."
        }, {
            "type": "entry",
            "name": "device action",
            "value": "A device action is part of a communication task and needs to be executed on a device when communicating with this device during a single connection."
        }, {
            "type": "entry",
            "name": "device command",
            "value": "Is used for infrequent adjustments of device parameters such as resetting a device. Device commands are only available when they are specified in a device protocol and this protocol is applied to the device type."
        }, {
            "type": "entry",
            "name": "device configuration",
            "value": "A device type can have multiple device configurations. Device configurations are used to customise and further configure the functionality of the device type. The device configuration will determine the actual register specifications together with the number of active channels. It also defines which security settings (authentication and encryption) are used and how a connection is set up (GPRS, GSM, etc.)."
        }, {"type": "entry", "name": "device life cycle", "value": "Is a collection of states a device goes through during its lifespan."}, {
            "type": "entry",
            "name": "device type",
            "value": "A device type defines the capabilities of a physical device. Every device has exactly one device type that defines which data (such as registers, load profiles and logbooks) can be read from it and what communication protocol is used to read it. A device type maps to a single physical device model. but devices of this model can have different capabilities. This is achieved through device configurations."
        }, {
            "type": "entry",
            "name": "dynamic device group",
            "value": "is formed based on different search criteria. Single devices cannot be selected, all devices of the search result are part of the group. This group is dynamic because each time a new device that complies to the search criteria is added to the system, it will also be added to the device group."
        }, {"type": "entry", "name": "encryption", "value": "Ensures that the information within a session is not compromised."}, {
            "type": "entry",
            "name": "firmware campaign",
            "value": "Is the action of uploading firmware onto every device belonging to a specific device group and configured with one specific device type."
        }, {
            "type": "entry",
            "name": "gateway",
            "value": "Device enabling the communication with (sub)meters by routing/translating the communication. A gateway will not store any data and works solely as a pas-through. Consequently the (sub)meter needs a protocol to read the data."
        }, {
            "type": "entry",
            "name": "ghost",
            "value": "Ghost is the status of time of use calendars that originate from the device and that are not known in the Connexo system. For these time of use calendars no detailed information is available and it is not possible to preview such a time of use calendar. It can only be removed. Ghost time of use calendars can be introduced into Connexo by importing them. See “Import time of use calendar”. Import file must be requested from the device manufacturer. The same concept also applies to firmware available on a device."
        }, {"type": "entry", "name": "inbound", "value": "Data is pushed from the device to for example a communication server."}, {
            "type": "entry",
            "name": "issue",
            "value": "An issue represents a problem that occurred during the execution of a manual or automated task. It usually requires human intervention to analyse and fix the problem, although some issues can be fixed simply by automatically retrying them. The details of an issue and how to solve them depends on the type of task from which it originates. Issues can be assigned to a particular user or user group to solve it."
        }, {"type": "entry", "name": "issue creation rules", "value": "Rules to define which events automatically trigger an issue and which actions should be taken."}, {
            "type": "entry",
            "name": "load profile",
            "value": "A load profile is an object containing one or more channels with the same reading interval."
        }, {
            "type": "entry",
            "name": "load profile configuration",
            "value": "A load profile configuration contains further specification of different load profile parameters that are specific for a certain device configuration."
        }, {
            "type": "entry",
            "name": "load profile types",
            "value": "A load profile type contains all essential load profile parameters so that this load profile type can be reused for different device types and device configurations, but it doesn’t contain values."
        }, {
            "type": "entry",
            "name": "logbook",
            "value": "A logbook categorizes events that have occurred on a device. A single logbook can contain different types of events. A device can have more than one logbook. Each logbook has its own unique identifier, which is needed to retrieve its contents from the device. The content of the logbook is recorded during periodic communication to a device. Typical events that are recorded are power outages, connection status, firmware logging, etc."
        }, {
            "type": "entry",
            "name": "logbook configuration",
            "value": "A logbook configuration contains further specification of different logbook parameters that are specific for a certain device configuration."
        }, {
            "type": "entry",
            "name": "logbook types",
            "value": "A logbook type contains all essential logbook parameters so that this logbook type can be reused for different device types and device configurations, but it doesn’t contain values."
        }, {
            "type": "entry",
            "name": "MRID",
            "value": "Master Resource IDentifier, unique identifier for devices and usage point. Complies to the CIM definition of UUID. The UUID is 128 bits long, can guarantee uniqueness across space and time and complies with RFC 4122 standards."
        }, {
            "type": "entry",
            "name": "OBIS code",
            "value": "OBIS stands for Object Identification System, provides standard identifiers for all data originating from the metering devices based on the DLMS standard. An OBIS code will always map to a reading type, but a reading type will not always map to an OBIS code. OBIS codes describes the data that originates from the device and will not be used for further processing."
        }, {"type": "entry", "name": "outbound", "value": "Data from, for example a communication server, is pushed to a device."}, {
            "type": "entry",
            "name": "pretransition check",
            "value": "Are different requirements that need to be met before the device can transition."
        }, {
            "type": "entry",
            "name": "privilege",
            "value": "Designate what rights a user has to perform certain actions on a specific resource. This can be for example the viewing, adding, editing or removing of a communication server. A resource is a component of an application, e.g. role is a resource as well as issue rules, licenses and communication servers."
        }, {
            "type": "entry",
            "name": "protocol dialect",
            "value": "Some tasks require specific properties such as the ability to download a large amount of data as it is often the case for firmware upgrades. Such specific properties can be acquired by configuring protocol dialects."
        }, {
            "type": "entry",
            "name": "reading type",
            "value": "A reading type provides a detailed description of a reading value. It is described using 18 key attributes separated by a dot and is defined by the CIM standard. For more information on reading types see the Admin user guide chapter System configuration: Reading types. Reading types are mainly used to describe data further in the data management process after data collection."
        }, {
            "type": "entry",
            "name": "register",
            "value": "A register is a placeholder for data about a device or data measured by the device. It contains snapshot values that are captured at specific moments in time."
        }, {
            "type": "entry",
            "name": "register configuration",
            "value": "A register configuration contains further specification of different register parameters that are specific for a certain device configuration."
        }]
    };
    window.rh.model.publish(rh.consts('KEY_TEMP_DATA'), glossary, {sync: true});
})();