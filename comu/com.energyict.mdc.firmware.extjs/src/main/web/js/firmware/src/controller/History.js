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
            title: Uni.I18n.translate('general.firmwareVersions', 'FWC', 'Firmware versions'),
            route: 'administration/devicetypes/{deviceTypeId}/firmware/versions',
            controller: 'Fwc.controller.Firmware',
            privileges: Mdc.privileges.DeviceType.view,
            action: 'showFirmwareVersions',
            filter: 'Fwc.model.FirmwareFilter',
            items: {
                add: {
                    title: Uni.I18n.translate('firmwareVersion.add', 'FWC', 'Add firmware version'),
                    route: 'add',
                    controller: 'Fwc.controller.Firmware',
                    privileges: Mdc.privileges.DeviceType.admin,
                    action: 'addFirmware'
                },
                edit: {
                    title: Uni.I18n.translate('firmware.route.firmwareversions.edit', 'FWC', 'Edit firmware version'),
                    route: '{firmwareId}/edit',
                    controller: 'Fwc.controller.Firmware',
                    privileges: Mdc.privileges.DeviceType.admin,
                    action: 'editFirmware',
                    callback: function (route) {
                        this.getApplication().on('loadFirmware', function (record) {
                            route.setTitle('Edit \'' + record.get('firmwareVersion') + '\'');
                            return true;
                        }, {single: true});

                        return this;
                    }
                }
            }
        },
        "administration/devicetypes/view/firmwareoptions": {
            title: Uni.I18n.translate('general.firmwareManagementOptions', 'FWC', 'Firmware management options'),
            route: 'administration/devicetypes/{deviceTypeId}/firmware/options',
            controller: 'Fwc.controller.Firmware',
            action: 'showFirmwareOptions',
            items: {
                edit: {
                    title: Uni.I18n.translate('general.firmwareManagementOptions.edit', 'FWC', 'Edit firmware management options'),
                    route: 'edit',
                    controller: 'Fwc.controller.Firmware',
                    action: 'editFirmwareOptions'
                }
            }
        },
        "devices/device/firmware": {
            title: Uni.I18n.translate('general.firmware', 'FWC', 'Firmware'),
            route: 'devices/{mRID}/firmware',
            controller: 'Fwc.devicefirmware.controller.DeviceFirmware',
            action: 'showDeviceFirmware',
            dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
            items: {
                upload: {
                    title: Uni.I18n.translate('firmware.route.devicefirmware.upload', 'FWC', 'Upgrade meter firmware'),
                    route: 'upload',
                    controller: 'Fwc.devicefirmware.controller.DeviceFirmware',
                    action: 'showDeviceFirmwareUpload',
                    dynamicPrivilegeStores: Mdc.dynamicprivileges.Stores.deviceStateStore,
                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.firmwareManagementActions,
                    callback: function (route) {
                        this.getApplication().on('uploadfirmwareoption', function (title) {
                            route.setTitle(title);
                            return true;
                        }, {single: true});

                        return this;
                    }
                },
                log: {
                    title: Uni.I18n.translate('firmware.route.devicefirmware.log', 'FWC', 'Meter firmware upgrade log'),
                    route: '{firmwareId}/log',
                    controller: 'Fwc.devicefirmware.controller.FirmwareLog',
                    action: 'showDeviceFirmwareLog',
                    callback: function (route) {
                        this.getApplication().on('loadFirmware', function (firmware) {
                            route.setTitle(Uni.I18n.translatePlural(['deviceFirmware',  firmware.getAssociatedData().firmwareType.id, , 'log', 'title'].join('.'),
                                firmware.get('firmwareVersion'),
                                'FWC', 'Meter firmware upgrade log to version "{0}"'
                            ));
                            return true;
                        }, {single: true});

                        return this;
                    }
                }
            }
        },
        workspace: {
            title: Uni.I18n.translate('general.workspace', 'FWC', 'Workspace'),
            route: 'workspace',
            disabled: true,
            items: {
                firmwarecampaigns: {
                    title: Uni.I18n.translate('firmware.campaigns.firmwareCampaigns', 'FWC', 'Firmware campaigns'),
                    route: 'firmwarecampaigns',
                    controller: 'Fwc.firmwarecampaigns.controller.Overview',
                    action: 'showOverview',
                    privileges: Fwc.privileges.FirmwareCampaign.view,
                    items: {
                        add: {
                            title: Uni.I18n.translate('firmware.campaigns.addFirmwareCampaign', 'FWC', 'Add firmware campaign'),
                            route: 'add',
                            controller: 'Fwc.firmwarecampaigns.controller.Add',
                            action: 'showAdd',
                            privileges: Fwc.privileges.FirmwareCampaign.administrate
                        },
                        firmwarecampaign: {
                            title: Uni.I18n.translate('firmware.campaigns.firmwareCampaign', 'FWC', 'Firmware campaign'),
                            route: '{firmwareCampaignId}',
                            controller: 'Fwc.firmwarecampaigns.controller.Detail',
                            action: 'showDetail',
                            privileges: Fwc.privileges.FirmwareCampaign.view,
                            callback: function (route) {
                                this.getApplication().on('loadFirmwareCampaign', function (record) {
                                    route.setTitle(record.get('name'));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                devices: {
                                    title: Uni.I18n.translate('general.devices', 'FWC', 'Devices'),
                                    route: 'devices',
                                    controller: 'Fwc.firmwarecampaigns.controller.Devices',
                                    action: 'showDevices',
                                    privileges: Fwc.privileges.FirmwareCampaign.view
                                }
                            }
                        }
                    }
                }
            }
        }
    }
});
