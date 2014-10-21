Ext.define('Mdc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.MenuItems'
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
        'Mdc.controller.setup.DeviceLoadProfileChannelData',
        'Mdc.controller.setup.DeviceLoadProfileChannelDataEditReadings',
        'Mdc.controller.setup.DeviceLoadProfileChannelOverview',
        'Mdc.controller.setup.DeviceLoadProfileChannels',
        'Mdc.controller.setup.DeviceLoadProfileData',
        'Mdc.controller.setup.DeviceLoadProfileOverview',
        'Mdc.controller.setup.DeviceLoadProfiles',
        'Mdc.controller.setup.DeviceLogbookData',
        'Mdc.controller.setup.DeviceLogbookOverview',
        'Mdc.controller.setup.DeviceLogbooks',
        'Mdc.controller.setup.DeviceProtocolDialects',
        'Mdc.controller.setup.DeviceRegisterConfiguration',
        'Mdc.controller.setup.DeviceRegisterData',
        'Mdc.controller.setup.DeviceRegisterDataEdit',
        'Mdc.controller.setup.Devices',
        'Mdc.controller.setup.DeviceSecuritySettings',
        'Mdc.controller.setup.DeviceTypeLogbooks',
        'Mdc.controller.setup.DeviceTypes',
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
        'Mdc.controller.setup.SearchItemsBulkAction',
        'Mdc.controller.setup.SearchItems',
        'Mdc.controller.setup.SecuritySettings',
        'Mdc.controller.setup.SetupOverview',
        'Mdc.controller.setup.ValidationRuleSets',
        'Mdc.controller.setup.Messages'
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

        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.device', 'privilege.view.device'])) {
            var devicesMenuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('device.devices', 'DVI', 'Devices'),
                href: '#/devices',
                glyph: 'devices',
                portal: 'devices',
                index: 20
            });

            Uni.store.MenuItems.add(devicesMenuItem);

            var portalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.deviceGroups', 'MDC', 'Device groups'),
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
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: me.getApplication().getController('Mdc.controller.history.Setup').tokenizeShowOverview(),
            portal: 'administration',
            glyph: 'settings',
            index: 10
        });

        Uni.store.MenuItems.add(menuItem);

        var deviceManagementItem = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.deviceManagement', 'MDC', 'Device management'),
            portal: 'administration',
            route: 'devicemanagement',
            items: [
                {
                    text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
                    href: '#/administration/devicetypes',
                    route: 'devicetypes'
                },
                {
                    text: Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
                    href: '#/administration/registertypes',
                    route: 'registertypes'
                },
                {
                    text: Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups'),
                    href: '#/administration/registergroups',
                    route: 'registergroups'
                },
                {
                    text: Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
                    href: '#/administration/logbooktypes',
                    route: 'logbooktypes'
                },
                {
                    text: Uni.I18n.translate('general.loadProfileTypes', 'MDC', 'Load profile types'),
                    href: '#/administration/loadprofiletypes',
                    route: 'loadprofiletypes'
                }
            ]
        });

        var deviceCommunicationItem = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.deviceCommunication', 'MDC', 'Device communication'),
            portal: 'administration',
            route: 'devicecommunication',
            items: [
                {
                    text: Uni.I18n.translate('general.comServers', 'MDC', 'Communication servers'),
                    href: '#/administration/comservers',
                    route: 'comservers'
                },
                {
                    text: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
                    href: '#/administration/comportpools',
                    route: 'comportpools'
                },
                {
                    text: Uni.I18n.translate('general.deviceComProtocols', 'MDC', 'Communication protocols'),
                    href: '#/administration/devicecommunicationprotocols',
                    route: 'devicecommunicationprotocols'
                },
                {
                    text: Uni.I18n.translate('general.comSchedules', 'MDC', 'Shared communication schedules'),
                    href: '#/administration/communicationschedules',
                    route: 'communicationschedules'
                },
                {
                    text: Uni.I18n.translate('registerConfig.communicationTasks', 'MDC', 'Communication tasks'),
                    href: '#/administration/communicationtasks',
                    route: 'communicationtasks'
                }
            ]
        });

        Uni.store.PortalItems.add(
            deviceManagementItem,
            deviceCommunicationItem
        );

        this.getApplication().fireEvent('cfginitialized');
    }
});
