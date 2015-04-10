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
            title: Uni.I18n.translate('firmware.route.firmwareversions', 'FWC', 'Firmware versions'),
            route: 'administration/devicetypes/{deviceTypeId}/firmware/versions',
            controller: 'Fwc.controller.Firmware',
            privileges: ['privilege.administrate.deviceType', 'privilege.view.deviceType'],
            action: 'showFirmwareVersions',
            filter: 'Fwc.model.FirmwareFilter',
            items: {
                add: {
                    title: Uni.I18n.translate('firmware.route.firmwareversions.add', 'FWC', 'Add firmware version'),
                    route: 'add',
                    controller: 'Fwc.controller.Firmware',
                    privileges: ['privilege.administrate.deviceType'],
                    action: 'addFirmware'
                },
                edit: {
                    title: Uni.I18n.translate('firmware.route.firmwareversions.edit', 'FWC', 'Edit firmware version'),
                    route: '{firmwareId}/edit',
                    controller: 'Fwc.controller.Firmware',
                    privileges: ['privilege.administrate.deviceType'],
                    action: 'editFirmware',
                    callback: function (route) {
                        this.getApplication().on('loadFirmware', function (record) {
                            route.setTitle('Edit \'' + record.get('type') + '-' + record.get('firmwareVersion') + '\'');
                            return true;
                        }, {single: true});

                        return this;
                    }
                }
            }
        },
        "administration/devicetypes/view/firmwareoptions": {
            title: Uni.I18n.translate('firmware.route.firmwareoptions', 'FWC', 'Firmware upgrade options'),
            route: 'administration/devicetypes/{deviceTypeId}/firmware/options',
            controller: 'Fwc.controller.Firmware',
            action: 'showFirmwareOptions',
            items: {
                edit: {
                    title: Uni.I18n.translate('firmware.route.firmwareoptions.edit', 'FWC', 'Edit firmware upgrade options'),
                    route: 'edit',
                    controller: 'Fwc.controller.Firmware',
                    action: 'editFirmwareOptions'
                }
            }
        },
        "devices/device/firmware": {
            title: Uni.I18n.translate('firmware.route.devicefirmware', 'FWC', 'Firmware'),
            route: 'devices/{mRID}/firmware',
            controller: 'Fwc.devicefirmware.controller.DeviceFirmware',
            action: 'showDeviceFirmware',
            items: {
                upload: {
                    title: Uni.I18n.translate('firmware.route.devicefirmware.upload', 'FWC', 'Upgrade meter firmware'),
                    route: 'upload',
                    controller: 'Fwc.devicefirmware.controller.DeviceFirmware',
                    action: 'showDeviceFirmwareUpload'
                }
            }
        }
    }
});
