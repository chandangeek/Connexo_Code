/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.deviceTypeSideMenu',
    uniqueMenuId: 'device-type-side-menu',
    title: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
    objectType: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
    deviceTypeId: null,
    isDataLoggerSlave: undefined,
    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                itemId: 'overviewLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId
            },
            {
                title: Uni.I18n.translate('devicetypemenu.datasources', 'MDC', 'Data sources'),
                items: [
                    {
                        text: Uni.I18n.translate('general.loadProfileTypes', 'MDC', 'Load profile types'),
                        itemId: 'loadProfilesLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/loadprofiles'
                    },
                    {
                        text: Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
                        itemId: 'logbooksLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/logbooktypes'
                    },
                    {
                        text: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
                        itemId: 'registerConfigsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/registertypes'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('devicetypemenu.configurations', 'MDC', 'Configurations'),
                items: [
                    {
                        text: Uni.I18n.translate('devicetypemenu.configurations', 'MDC', 'Configurations'),
                        itemId: 'configurationsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations'
                    },
                    {
                        text: Uni.I18n.translate('devicetypemenu.conflictingMappings', 'MDC', 'Conflicting mappings'),
                        itemId: 'conflictingMappingLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/conflictmappings'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('devicetypemenu.customattributes', 'MDC', 'Custom attributes'),
                items: [
                    {
                        text: Uni.I18n.translate('devicetypemenu.customattribute.sets', 'MDC', 'Custom attribute sets'),
                        itemId: 'configurationsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/customattributesets'
                    }
                ]
            }
        ];

        if (me.isDataLoggerSlave === undefined) {
            Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
                success: function (deviceType) {
                    me.isDataLoggerSlave = deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE';
                    if (me.isDataLoggerSlave) {
                        me.down('#logbooksLink').hide();
                        me.down('#conflictingMappingLink').hide();
                    } else {
                        me.executeIfNoDataLoggerSlave();
                    }
                },
                failure: function (deviceType) {
                    me.executeIfNoDataLoggerSlave();
                }

            })
        }


        me.callParent(arguments);
    },

    executeIfNoDataLoggerSlave: function () {
        var me = this;
        if (!me.isDataLoggerSlave) {
            me.addMenuWithFirmware();
        } else {
            me.addMenuWithoutFirmware();
        }
    },

    addMenuWithFirmware: function () {
        var me = this;
        me.addMenuItems([{
            title: Uni.I18n.translate('general.specifications', 'MDC', 'Specifications'),
            itemId: 'specifications-menu-item',
            items: [
                {
                    text: Uni.I18n.translate('general.fileManagement', 'MDC', 'File management'),
                    privileges: Mdc.privileges.DeviceType.view,
                    itemId: 'fileManagementLink',
                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/filemanagement',
                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceTypeCapability.supportsFileManagement
                },
                {
                    text: Uni.I18n.translate('general.firmwareVersions', 'MDC', 'Firmware versions'),
                    privileges: Mdc.privileges.DeviceType.view,
                    itemId: 'firmwareversionsLink',
                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/firmwareversions'
                },
                {
                    text: Uni.I18n.translate('devicetypemenu.timeOfUseCalendars', 'MDC', 'Time of use calendars'),
                    privileges: Mdc.privileges.DeviceType.view,
                    itemId: 'timeOfUseLink',
                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/timeofuse'
                }
            ]
        }]);
    },

    addMenuWithoutFirmware: function () {
        var me = this;
        me.addMenuItems([{
            title: Uni.I18n.translate('devicetypemenu.specifications', 'MDC', 'Specifications'),
            itemId: 'specifications-menu-item',
            items: [
                {
                    text: Uni.I18n.translate('general.fileManagement', 'MDC', 'File management'),
                    privileges: Mdc.privileges.DeviceType.view,
                    itemId: 'fileManagementLink',
                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/filemanagement',
                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceTypeCapability.supportsFileManagement
                },
                {
                    text: Uni.I18n.translate('devicetypemenu.timeOfUseCalendars', 'MDC', 'Time of use calendars'),
                    privileges: Mdc.privileges.DeviceType.view,
                    itemId: 'timeOfUseLink',
                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/timeofuse'
                }
            ]
        }]);
    },

    setDeviceTypeTitle: function (name) {
        this.setHeader(name);
    }
});

