Ext.define('Fwc.controller.History', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    requires: [
        'Fwc.model.FirmwareFilter'
    ],

    routeConfig: {
        "administration/devicetypes/view/firmwareversions": {
            title: 'Firmware versions',
            route: '/administration/devicetypes/{deviceTypeId}/firmware/versions',
            controller: 'Fwc.controller.Firmware',
            action: 'showFirmwareVersions',
            filter: 'Fwc.model.FirmwareFilter',
            items: {
                add: {
                    title: 'Add firmware version',
                    route: 'add',
                    controller: 'Fwc.controller.Firmware',
                    action: 'addFirmware'
                },
                edit: {
                    title: 'Edit firmware version',
                    route: '{firmwareId}/edit',
                    controller: 'Fwc.controller.Firmware',
                    action: 'editFirmware'
                }
            }
        },
        "administration/devicetypes/view/firmwareoptions": {
            title: 'Firmware upgrade options',
            route: '/administration/devicetypes/{deviceTypeId}/firmware/options',
            controller: 'Fwc.controller.Firmware',
            action: 'showFirmwareOptions'
        }
    }
});
