/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceMenu', {
    extend: 'Uni.view.menu.SideMenu',
    xtype: 'deviceMenu',
    requires: ['Mdc.util.LinkPurpose'],
    uniqueMenuId: 'device-side-menu',
    device: null,
    objectType: Uni.I18n.translate('devicemenu.title', 'MDC', 'Device'),

    initComponent: function () {
        var me = this,
            deviceId = me.device.get('name'),
            menu = {
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('devicemenu.overviewx', 'MDC', 'Overview'),
                        itemId: 'deviceOverviewLink',
                        href: '#/devices/' + encodeURIComponent(deviceId)
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.deviceAttributes', 'MDC', 'Device attributes'),
                        privileges: Mdc.privileges.Device.viewOrAdministrateDeviceData,
                        itemId: 'device-attributes-link',
                        href: '#/devices/' + encodeURIComponent(deviceId) + '/attributes'
                    },
                    {
                        text: Uni.I18n.translate('devicemenu.zones', 'MDC', 'Zones'),
                        privileges: Mdc.privileges.Device.viewOrAdministrateDeviceData,
                        itemId: 'device-zones-link',
                        href: '#/devices/' + encodeURIComponent(deviceId) + '/zones'
                    },
                    {
                        text: Uni.I18n.translate('general.history', 'MDC', 'History'),
                        privileges: Mdc.privileges.Device.viewDeviceData,
                        itemId: 'device-history-link',
                        href: '#/devices/' + encodeURIComponent(deviceId) + '/history'
                    }
                ]
            };
        me.title = deviceId || Uni.I18n.translate('devicemenu.title', 'MDC', 'Device');

        if ((!Ext.isEmpty(me.device.get('isDataLogger')) && me.device.get('isDataLogger')) || (!Ext.isEmpty(me.device.get('isMultiElementDevice')) && me.device.get('isMultiElementDevice'))) {
            menu.items.push(
                {
                    text: Mdc.util.LinkPurpose.forDevice(me.device).pageTitle,
                    itemId: 'device-dataLoggerSlaves-link',
                    href: '#/devices/' + encodeURIComponent(deviceId) + '/slaves',
                    privileges: Mdc.privileges.Device.viewDevice
                }
            );
        }

        menu.items.push(
            {
                text: Uni.I18n.translate('devicemenu.processes', 'MDC', 'Processes'),
                privileges: Mdc.privileges.Device.deviceProcesses,
                itemId: 'device-processes-link',
                href: '#/devices/' + encodeURIComponent(deviceId) + '/processes'
            },
            {
                text: Uni.I18n.translate('devicemenu.serviceCalls', 'MDC', 'Service calls'),
                privileges: Mdc.privileges.Device.viewDevice,
                itemId: 'device-servicecalls-link',
                href: '#/devices/' + encodeURIComponent(deviceId) + '/servicecalls'
            },
            {
                text: Uni.I18n.translate('devicemenu.exclusionsFromAutoclosure', 'MDC', 'Exclusions from autoclosure'),
                privileges: Mdc.privileges.CreationRule.viewCreationRule,
                itemId: 'device-autoclosure-exclusions-link',
                href: '#/devices/' + encodeURIComponent(deviceId) + '/autoclosureexclusions'
            }
        );

        me.menuItems = [];
        me.menuItems.push(menu);

        if (me.device.get('hasLoadProfiles') || me.device.get('hasRegisters') || me.device.get('hasLogBooks')) {
            me.menuItems.push(
                {
                    title: Uni.I18n.translate('device.dataSources', 'MDC', 'Data sources'),
                    xtype: 'menu',
                    items: [
                        {
                            text: Uni.I18n.translate('devicemenu.loadProfiles', 'MDC', 'Load profiles'),
                            itemId: 'loadProfilesLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/loadprofiles',
                            showCondition: me.device.get('hasLoadProfiles')
                        },
                        {
                            text: Uni.I18n.translate('general.channels', 'MDC', 'Channels'),
                            privileges: Mdc.privileges.Device.viewDevice,
                            itemId: 'channelsLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/channels',
                            showCondition: me.device.get('hasLoadProfiles')
                        },
                        {
                            text: Uni.I18n.translate('general.logbooks', 'MDC', 'Logbooks'),
                            itemId: 'logbooksLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/logbooks',
                            showCondition: me.device.get('hasLogBooks')
                        },
                        {
                            text: Uni.I18n.translate('general.events', 'MDC', 'Events'),
                            itemId: 'events',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/events',
                            showCondition: me.device.get('hasLogBooks')
                        },
                        {
                            text: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                            itemId: 'registersLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/registers',
                            showCondition: me.device.get('hasRegisters')
                        }
                    ]
                }
            );
        }

        if ( (me.device.get('hasValidationRules') || me.device.get('hasEstimationRules')) &&
             (me.device.get('hasLoadProfiles') || me.device.get('hasRegisters')) ) {
            me.menuItems.push(
                {
                    title: Uni.I18n.translate('device.readingQuality', 'MDC', 'Reading quality'),
                    items: [
                        {
                            text: Uni.I18n.translate('devicemenu.dataValidation', 'MDC', 'Validation configuration'),
                            itemId: 'dataValidationLink',
                            privileges: Cfg.privileges.Validation.fineTuneValidation,
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/datavalidation',
                            showCondition: me.device.get('hasValidationRules')
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.validationResults', 'MDC', 'Validation results'),
                            itemId: 'validationResultsLink',
                            hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration', 'privilege.view.fineTuneValidationConfiguration']),
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/validationresults/data',
                            showCondition: me.device.get('hasValidationRules')
                        },
                        {
                            text: Uni.I18n.translate('general.dataEstimation', 'MDC', 'Data estimation'),
                            itemId: 'dataEstimationLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/dataestimation',
                            privileges: Mdc.privileges.DeviceConfigurationEstimations.view,
                            showCondition: me.device.get('hasEstimationRules')
                        }
                    ]
                }
            );
        }

        if (!me.device.get('isDataLoggerSlave') && !me.device.get('isMultiElementSlave')) {
            me.menuItems.push(
                {
                    title: Uni.I18n.translate('device.communication', 'MDC', 'Communication'),
                    items: [
                        {
                            text: Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes'),
                            itemId: 'deviceGeneralAttributesLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/generalattributes'
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.communicationPlanning', 'MDC', 'Communication planning'),
                            itemId: 'communicationPlanningLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/communicationplanning',
                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.communicationPlanningPages
                        },
                        {
                            text: Uni.I18n.translate('general.communicationTasks', 'MDC', 'Communication tasks'),
                            itemId: 'communicationTasksLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/communicationtasks'
                        },
                        {
                            text: Uni.I18n.translate('general.connectionMethods', 'MDC', 'Connection methods'),
                            itemId: 'connectionMethodsLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/connectionmethods'
                        },
                        {
                            text: Uni.I18n.translate('general.securitySets', 'MDC', 'Security sets'),
                            itemId: 'securitySettingLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/securitysettings'
                        },
                        {
                            text: Uni.I18n.translate('general.securityAccessors', 'MDC', 'Security accessors'),
                            itemId: 'securityAccessorsLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/keys'
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.protocols', 'MDC', 'Protocol dialects'),
                            itemId: 'protocolLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/protocols'
                        },
                        {
                            text: Uni.I18n.translate('devicemenu.commands', 'MDC', 'Commands'),
                            itemId: 'deviceCommands',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/commands'
                        },
                        {
                            text: Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'),
                            itemId: 'topologyLink',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/topology',
                            showCondition: me.device.get('isGateway') || !me.device.get('isDirectlyAddressed')
                        },
                        {
                            text: Uni.I18n.translate('title.registeredDevices', 'MDC', 'Registered devices'),
                            itemId: 'registeredDevices',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/regdevices',
                            showCondition: me.device.get('isGateway') && me.device.get('gatewayType') === 'LAN'
                        }
                    ]
                },
                {
                    title: Uni.I18n.translate('general.configuration', 'MDC', 'Configuration'),
                    items: [
                        {
                            text: Uni.I18n.translate('general.firmware', 'MDC', 'Firmware'),
                            itemId: 'device-firmware-link-menu',
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/firmware',
                            showCondition: me.device.get('isFirmwareManagementAllowed')
                        },
                        {
                            text: Uni.I18n.translate('general.timeOfUse', 'MDC', 'Time of use'),
                            itemId: 'deviceTimeOfUseLink',
                            privileges: Mdc.privileges.Device.viewDevice,
                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.timeOfUseAllowed,
                            href: '#/devices/' + encodeURIComponent(deviceId) + '/timeofuse'
                        }
                    ]
                }
            );
        }

        me.callParent(arguments);
    },

    preProcessFullTokenForSelection: function(fullToken) {
        if (Ext.String.endsWith(fullToken, '/certificates')) {
            fullToken = fullToken.replace('/certificates', '/keys');
        }
        return fullToken;
    }
});