Ext.define('Mdc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Mdc.controller.setup.SetupOverview',
        'Mdc.controller.setup.ComServers',
        'Mdc.controller.setup.ComPortPools',
        'Mdc.controller.history.Setup',
        'Mdc.controller.setup.LicensedProtocol',
        'Mdc.controller.setup.DeviceTypes',
        'Mdc.controller.setup.RegisterTypes',
        'Mdc.controller.setup.RegisterMappings',
        'Mdc.controller.setup.DeviceConfigurations',
        'Mdc.controller.setup.DeviceCommunicationProtocols',
        'Mdc.controller.setup.RegisterGroups',
        'Mdc.controller.setup.ProtocolDialects',
        'Mdc.controller.setup.Devices',
        'Mdc.controller.setup.LogbookTypes',
        'Mdc.controller.setup.AddLogbookTypes',
        'Mdc.controller.setup.LogbookConfigurations',
        'Mdc.controller.setup.AddLogbookConfigurations',
        'Mdc.controller.setup.EditLogbookConfiguration',
        'Mdc.controller.setup.LogbookTypesOverview',
        'Mdc.controller.setup.SecuritySettings',
        'Mdc.controller.setup.CommunicationTasks',
        'Mdc.controller.setup.LogForm'
    ],

    controllers: [
        'Mdc.controller.setup.ComPortPools',
        'Mdc.controller.setup.DeviceTypes',
        'Mdc.controller.setup.SetupOverview',
        'Mdc.controller.setup.ComServers',
        'Mdc.controller.history.Setup',
        'Mdc.controller.setup.DeviceCommunicationProtocols',
        'Mdc.controller.setup.LicensedProtocol',
        'Mdc.controller.setup.RegisterTypes',
        'Mdc.controller.setup.RegisterMappings',
        'Mdc.controller.setup.DeviceConfigurations',
        'Mdc.controller.setup.RegisterGroups',
        'Mdc.controller.setup.ProtocolDialects',
        'Mdc.controller.setup.Devices',
        'Mdc.controller.setup.LogbookTypes',
        'Mdc.controller.setup.AddLogbookTypes',
        'Mdc.controller.setup.LogbookConfigurations',
        'Mdc.controller.setup.AddLogbookConfigurations',
        'Mdc.controller.setup.EditLogbookConfiguration',
        'Mdc.controller.setup.LogbookTypesOverview',
        'Mdc.controller.setup.SecuritySettings',
        'Mdc.controller.setup.CommunicationTasks',
        'Mdc.controller.setup.LogForm'
    ],

    config: {
        navigationController: null
    },

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        var me = this;
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
                    text: Uni.I18n.translate('searchItems.searchItems', 'MDC', 'Search items'),
                    href: '#/administration/searchitems',
                    route: 'searchitems'
                },
                {
                    text: Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
                    href: '#/administration/logbooktypes',
                    route: 'logbooktypes'
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
                    text: Uni.I18n.translate('general.deviceComProtocols', 'MDC', 'Device communication protocols'),
                    href: '#/administration/devicecommunicationprotocols',
                    route: 'devicecommunicationprotocols'
                },
                {
                    text: Uni.I18n.translate('general.comSchedules', 'MDC', 'Communication schedules'),
                    href: '#/administration/communicationschedules',
                    route: 'communicationschedules'
                }
            ]
        });

        Uni.store.PortalItems.add(
            deviceManagementItem,
            deviceCommunicationItem
        );

        this.initNavigation();
        this.initDefaultHistoryToken();
    },

    initNavigation: function () {
        var controller = this.getController('Uni.controller.Navigation');
        this.setNavigationController(controller);
    },

    initDefaultHistoryToken: function () {
        var setupController = this.getController('Mdc.controller.history.Setup'),
            eventBus = this.getController('Uni.controller.history.EventBus'),
            defaultToken = setupController.tokenizeShowOverview();

        eventBus.setDefaultToken(defaultToken);
    },

    showContent: function (widget) {
        this.clearContentPanel();
        this.getContentPanel().add(widget);
        this.getContentPanel().doComponentLayout();
    },

    clearContentPanel: function () {
        var widget;
        while (widget = this.getContentPanel().items.first()) {
            this.getContentPanel().remove(widget, true);
        }
    }
});
