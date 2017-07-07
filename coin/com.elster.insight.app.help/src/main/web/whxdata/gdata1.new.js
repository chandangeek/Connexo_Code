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
            "name": "CIM",
            "value": "The Common Information Model is an international standard created and used by the utility industry to allow better exchange of data between parties."
        }, {"type": "entry", "name": "data source", "value": "Registers, load profiles and logbooks"}, {
            "type": "entry",
            "name": "device",
            "value": "A device represents a physical element (in the grid), this can be e.g. a meter, concentrator, gateway, in home display, etc."
        }, {
            "type": "entry",
            "name": "device configuration",
            "value": "Device configurations are used to customise or limit some of the functionality of the device type, but also configure additional parameters to interact with the physical device. The device configuration will determine the actual register specifications together with the number of channels. It also contains information on security levels, connection methods, etc."
        }, {
            "type": "entry",
            "name": "device type",
            "value": "A device type defines the capabilities of a physical device. Every device has exactly one device type that defines which data (such as registers, load profiles and logbooks) can be read from it and what communication protocol is used to read it. A device type maps to a single physical device model. but devices of this model can have different capabilities. This is achieved through device configurations."
        }, {
            "type": "entry",
            "name": "dynamic usage point group",
            "value": "is formed based on different search criteria. Single usage points can’t be selected. All usage points in the search result are part of the group. This group is dynamic because each time a new usage point that complies to the search criteria is added to the system, it is also added to the usage point group."
        }, {
            "type": "entry",
            "name": "log level",
            "value": "The following log levels are available in Connexo: Error, Warning, Information, Debug and Trace. Error logs exceptions that cause a process to be unable to continue. Contains an error code and an error message. Warning logs exceptions that could cause a problem in the future and may require attention, but at this stage it does not block the ongoing process. For example: creating a device which already exists. Contains a warning code. Information logs information of a step that has been executed and where the process is behaving as expected. Debug logs detailed information of what is being executed. This is extensive context that is useful during testing, debugging and development. It is indispensable for troubleshooting. It contains all message that are helpful in tracking the flow through the system and isolating issues. Trace is the most detailed information which is mainly used during development."
        }, {
            "type": "entry",
            "name": "MRID",
            "value": "Master Resource Identifier, unique identifier for devices and usage points. Complies with the CIM definition of UUID. The UUID is 128 bits long, can guarantee uniqueness across space and time and complies with RFC 4122 standards."
        }, {
            "type": "entry",
            "name": "OBIS code",
            "value": "OBIS stands for Object Identification System, provides standard identifiers for all data originating from the metering devices based on the DLMS standard. An OBIS code will always map to a reading type, but a reading type will not always map to an OBIS code. OBIS codes describes the data that originates from the device and will not be used for further processing."
        }, {"type": "entry", "name": "pretransition check", "value": "Are different requirements that need to be met before the device can transition."}, {
            "type": "entry",
            "name": "privilege",
            "value": "Designate what rights a user has to perform certain actions on a specific resource. This can be for example the viewing, adding, editing or removing of a metrology configuration. A resource is a component of an application, e.g. estimation rule set, import service, etc."
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
        }, {"type": "entry", "name": "release date", "value": "Is the date when a command comes available to be executed when there is a connection."}, {
            "type": "entry",
            "name": "state",
            "value": "Is the current status in the life cycle of a usage point. The state defines what you can see on the device, which actions the device can execute and what you can do on the device. The concept of states also applies to service calls."
        }, {
            "type": "entry",
            "name": "static usage point group",
            "value": "is a group of usage points that was selected out of a search result based on different search criteria. The composition of this group does not change over time. To add new usage points to the group, the search has to be repeated and the new usage points need to be manually added."
        }, {
            "type": "entry",
            "name": "transition",
            "value": "Is the action where a usage point goes from one state to another in a usage point life cycle. This transition is linked with pretransition checks and auto actions. This concept also applies to service calls."
        }, {
            "type": "entry",
            "name": "usage point life cycle",
            "value": "A collection of states that a usage point goes through via transitions during its lifespan. Each state corresponds to a different set of functionalities or restrictions."
        }, {"type": "entry", "name": "validation rule set", "value": "Is a collection of validation rules that should be applied together."}, {
            "type": "entry",
            "name": "validation rules",
            "value": "Rules to check whether the aggregated data is valid by verifying if the data is in accordance with specific market rules."
        }]
    };
    window.rh.model.publish(rh.consts('KEY_TEMP_DATA'), glossary, {sync: true});
})();