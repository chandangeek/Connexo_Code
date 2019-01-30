(function () {
    var glossary = {
        "type": "data",
        "entrys": [{
            "type": "entry",
            "name": "register types",
            "value": "A register type contains all essential register parameters so that this register type can be reused for different device types and device configurations, but it doesn’t contain values."
        }, {"type": "entry", "name": "release date", "value": "Is the date when a command comes available to be executed when there is a connection."}, {
            "type": "entry",
            "name": "rule template",
            "value": "A rule template is a collection of parameters that are specific for a certain rule. The exact values of the parameters are defined in the rule itself. An examples of such a template is the template to create an issue when a slope is detected. All parameters will be shown, but the user will have to compete the reading type, the trend period and the threshold."
        }, {"type": "entry", "name": "shared communication schedule", "value": "Is a system wide communication schedule that can be used for multiple devices."}, {
            "type": "entry",
            "name": "state",
            "value": "Is the current status in the device life cycle of a device. The state defines what you can see on the device, which actions the device can execute and what you can do on the device. The concept of states also applies to service calls."
        }, {
            "type": "entry",
            "name": "static device group",
            "value": "is a group of devices that was selected out of a search result based on different search criteria. The composition of this group does not change over time, to add new devices to the group, the search has to be repeated and devices manually added."
        }, {
            "type": "entry",
            "name": "transition",
            "value": "Is the action where a device goes when one state to another in a device life cycle. This transition is linked with pretransition checks and auto-actions. This concept also applies to service calls."
        }, {"type": "entry", "name": "validation rule set", "value": "Is a collection of validation rules that should be applied together."}, {
            "type": "entry",
            "name": "validation rules",
            "value": "Rules to check whether collected data is valid by verifying if the data is in accordance with specific market rules."
        }, {"type": "entry", "name": "validator", "value": "The validator is a template that sets the rules to check if the collected data is correct."}]
    };
    window.rh.model.publish(rh.consts('KEY_TEMP_DATA'), glossary, {sync: true});
})();