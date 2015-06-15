Ext.define('Mdc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.MenuItems'
    ],

    stores: [
        'Mdc.store.ChannelsOfLoadProfilesOfDevice',
        'Mdc.store.LoadProfilesOfDevice'
    ],

    controllers: [
        'Mdc.controller.history.Setup',
        'Mdc.controller.setup.AddLogbookConfigurations',
        'Mdc.controller.setup.AddLogbookTypes',
        'Mdc.controller.setup.CommunicationSchedules',
        'Mdc.controller.setup.CommunicationTasks',
        'Mdc.controller.setup.ComPortPoolComPortsView',
        'Mdc.controller.setup.ComPortPoolEdit',
        'Mdc.controller.setup.ComPortPoolOverview',
        'Mdc.controller.setup.ComPortPools',
        'Mdc.controller.setup.ComServerComPortsEdit',
        'Mdc.controller.setup.ComServerComPortsView',
        'Mdc.controller.setup.ComServerEdit',
        'Mdc.controller.setup.ComServerOverview',
        'Mdc.controller.setup.ComServersView',
        'Mdc.controller.setup.Comtasks',
        'Mdc.controller.setup.ConnectionMethods',
        'Mdc.controller.setup.DeviceCommunicationProtocols',
        'Mdc.controller.setup.DeviceCommunicationSchedules',
        'Mdc.controller.setup.DeviceCommunicationTasks',
        'Mdc.controller.setup.DeviceConfigurationLogbooks',
        'Mdc.controller.setup.DeviceConfigurations',
        'Mdc.controller.setup.DeviceConnectionMethods',
        'Mdc.controller.setup.DeviceConnectionHistory',
        'Mdc.controller.setup.DeviceDataValidation',
        'Mdc.controller.setup.DeviceGroups',
        'Mdc.controller.setup.DeviceChannelData',
        'Mdc.controller.setup.DeviceChannels',
        'Mdc.controller.setup.DeviceLoadProfileData',
        'Mdc.controller.setup.DeviceLoadProfileOverview',
        'Mdc.controller.setup.DeviceLoadProfiles',
        'Mdc.controller.setup.DeviceEvents',
        'Mdc.controller.setup.DeviceLogbookOverview',
        'Mdc.controller.setup.DeviceLogbooks',
        'Mdc.controller.setup.DeviceProtocolDialects',
        'Mdc.controller.setup.DeviceRegisterConfiguration',
        'Mdc.controller.setup.DeviceRegisterData',
        'Mdc.controller.setup.DeviceRegisterDataEdit',
        'Mdc.controller.setup.Devices',
        'Mdc.controller.setup.DevicesAddGroupController',
        'Mdc.controller.setup.DevicesSearchController',
        'Mdc.controller.setup.DeviceSecuritySettings',
        'Mdc.controller.setup.DeviceTopology',
        'Mdc.controller.setup.DeviceTypeLogbooks',
        'Mdc.controller.setup.DeviceTypes',
        'Mdc.controller.setup.GeneralAttributes',
        'Mdc.controller.setup.EditLogbookConfiguration',
        'Mdc.controller.setup.LicensedProtocol',
        'Mdc.controller.setup.LoadProfileConfigurationDetails',
        'Mdc.controller.setup.LoadProfileConfigurations',
        'Mdc.controller.setup.LoadProfileTypes',
        'Mdc.controller.setup.LoadProfileTypesOnDeviceType',
        'Mdc.controller.setup.LogbookTypes',
        'Mdc.controller.setup.Properties',
        'Mdc.controller.setup.PropertiesView',
        'Mdc.controller.setup.ProtocolDialects',
        'Mdc.controller.setup.RegisterConfigs',
        'Mdc.controller.setup.RegisterGroups',
        'Mdc.controller.setup.RegisterMappings',
        'Mdc.controller.setup.RegisterTypes',
        'Mdc.controller.setup.RuleDeviceConfigurations',
        'Mdc.controller.setup.SearchItems',
        'Mdc.controller.setup.SearchItemsBulkAction',
        'Mdc.controller.setup.SecuritySettings',
        'Mdc.controller.setup.SetupOverview',
        'Mdc.controller.setup.ValidationRuleSets',
        'Mdc.controller.setup.DeviceCommands',
        'Mdc.controller.setup.Messages',
        'Mdc.controller.setup.DeviceCommunicationTaskHistory',
        'Mdc.controller.setup.DeviceConnectionHistory',
        'Mdc.controller.setup.DeviceGeneralAttributes',
        'Mdc.controller.setup.AddDeviceGroupAction',
        'Mdc.controller.setup.DeviceRegisterTab',
        'Mdc.controller.setup.DeviceLogBookTab',
        'Mdc.controller.setup.DeviceLoadProfileTab',
        'Mdc.controller.setup.DevicesEditGroupController',
        'Mdc.controller.setup.DeviceLogbookData',
        'Mdc.controller.setup.DataCollectionKpi',
		'Mdc.controller.setup.DeviceValidationResults',
        'Mdc.deviceconfigurationestimationrules.controller.RuleSets',
        'Mdc.deviceconfigurationestimationrules.controller.AddRuleSets',
        'Mdc.controller.setup.EstimationDeviceConfigurations',
        'Mdc.controller.setup.DeviceDataEstimation',
        'Mdc.controller.setup.DeviceValidationResults',
        'Mdc.controller.setup.DeviceHistory',
        'Mdc.controller.setup.ChangeDeviceLifeCycle'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Mdc.controller.history.Setup'); // Forces route registration.

        if (Mdc.privileges.DeviceGroup.canView()) {
            var devicesMenuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('device.devices', 'DVI', 'Devices'),
                href: '#/devices',
                glyph: 'devices',
                portal: 'devices',
                index: 20
            });

            Uni.store.MenuItems.add(devicesMenuItem);

            var portalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.deviceGroups', 'MDC', 'Device group'),
                portal: 'devices',
                route: 'devices',
                items: [
                    {
                        text: Uni.I18n.translate('general.deviceGroups', 'MDC', 'Device groups'),
                        href: '#/devices/devicegroups',
                        route: 'devicegroups'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                portalItem
            );
        }

        if (Mdc.privileges.DeviceType.canView() || Mdc.privileges.MasterData.canView() ||
            Mdc.privileges.Communication.canView() || Mdc.privileges.CommunicationSchedule.canView()) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
                href: me.getApplication().getController('Mdc.controller.history.Setup').tokenizeShowOverview(),
                portal: 'administration',
                glyph: 'settings',
                index: 10
            });

            Uni.store.MenuItems.add(menuItem);

            var deviceManagementItem = null;
            if (Mdc.privileges.DeviceType.canView() || Mdc.privileges.MasterData.canView()) {
                deviceManagementItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.deviceManagement', 'MDC', 'Device management'),
                    portal: 'administration',
                    route: 'devicemanagement',
                    items: [
                        {
                            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
                            href: '#/administration/devicetypes',
                            itemId: 'lnk-device-types',
                            privileges: Mdc.privileges.DeviceType.view,
                            route: 'devicetypes'
                        },
                        {
                            text: Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
                            href: '#/administration/registertypes',
                            itemId: 'lnk-register-types',
                            privileges: Mdc.privileges.MasterData.view,
                            route: 'registertypes'
                        },
                        {
                            text: Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups'),
                            href: '#/administration/registergroups',
                            itemId: 'lnk-register-groups',
                            privileges: Mdc.privileges.MasterData.view,
                            route: 'registergroups'
                        },
                        {
                            text: Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
                            href: '#/administration/logbooktypes',
                            itemId: 'lnk-logbook-types',
                            privileges: Mdc.privileges.MasterData.view,
                            route: 'logbooktypes'
                        },
                        {
                            text: Uni.I18n.translate('general.loadProfileTypes', 'MDC', 'Load profile types'),
                            href: '#/administration/loadprofiletypes',
                            itemId: 'lnk-load-profile-types',
                            privileges: Mdc.privileges.MasterData.view,
                            route: 'loadprofiletypes'
                        }
                    ]
                });
            }

            var deviceCommunicationItem = null;
            if (Mdc.privileges.Communication.canView() || Mdc.privileges.CommunicationSchedule.canView()) {
                deviceCommunicationItem = Ext.create('Uni.model.PortalItem', {
                    title: Uni.I18n.translate('general.deviceCommunication', 'MDC', 'Device communication'),
                    portal: 'administration',
                    route: 'devicecommunication',
                    items: [
                        {
                            text: Uni.I18n.translate('general.comServers', 'MDC', 'Communication servers'),
                            href: '#/administration/comservers',
                            privileges: Mdc.privileges.Communication.view,
                            route: 'comservers'
                        },
                        {
                            text: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
                            href: '#/administration/comportpools',
                            privileges: Mdc.privileges.Communication.view,
                            route: 'comportpools'
                        },
                        {
                            text: Uni.I18n.translate('general.deviceComProtocols', 'MDC', 'Communication protocols'),
                            href: '#/administration/devicecommunicationprotocols',
                            privileges: Mdc.privileges.Communication.view,
                            route: 'devicecommunicationprotocols'
                        },
                        {
                            text: Uni.I18n.translate('general.comSchedules', 'MDC', 'Shared communication schedules'),
                            privileges: Mdc.privileges.CommunicationSchedule.view,
                            href: '#/administration/communicationschedules',
                            route: 'communicationschedules'
                        },
                        {
                            text: Uni.I18n.translate('registerConfig.communicationTasks', 'MDC', 'Communication tasks'),
                            href: '#/administration/communicationtasks',
                            privileges: Mdc.privileges.Communication.view,
                            route: 'communicationtasks'
                        },
                        {
                            text: Uni.I18n.translate('general.dataCollectionKpis', 'MDC', 'Data collection KPIs'),
                            href: '#/administration/datacollectionkpis',
                            route: 'datacollectionkpis'
                        }
                    ]
                });

            }
            if (deviceCommunicationItem !== null) {
                Uni.store.PortalItems.add(deviceCommunicationItem);
            }
            if (deviceManagementItem !== null) {
                Uni.store.PortalItems.add(deviceManagementItem);
            }
        }
    }
});
