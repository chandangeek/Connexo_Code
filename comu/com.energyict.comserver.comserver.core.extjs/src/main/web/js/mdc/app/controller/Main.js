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
            text: Uni.I18n.translate('general.setup', 'MDC', 'Setup'),
            glyph: 'settings',
            // TODO Rename the below properties when merging into 1 menu item.
            href: me.getApplication().getController('Mdc.controller.history.Setup').tokenizeShowOverview(),
            portal: 'setup'
        });
        Uni.store.MenuItems.add(menuItem);

        var deviceManagementItem = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.deviceManagement', 'MDC', 'Device management'),
            portal: 'setup',
            route: 'devicemanagement',
            items: [
                {
                    text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
                    href: '#/setup/devicetypes',
                    route: 'devicetypes'
                },
                {
                    text: Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
                    href: '#/setup/registertypes',
                    route: 'registertypes'
                },
                {
                    text: Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups'),
                    href: '#/setup/registergroups',
                    route: 'registergroups'
                },
                {
                    text: Uni.I18n.translate('searchItems.searchItems', 'MDC', 'Search items'),
                    href: '#/setup/searchitems',
                    route: 'searchitems'
                },
                {
                    text: Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
                    href: '#/setup/logbooktypes',
                    route: 'logbooktypes'
                }
            ]
        });

        var deviceCommunicationItem = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.deviceCommunication', 'MDC', 'Device communication'),
            portal: 'setup',
            route: 'devicecommunication',
            items: [
                {
                    text: Uni.I18n.translate('general.comServers', 'MDC', 'Communication servers'),
                    href: '#/setup/comservers',
                    route: 'comservers'
                },
                {
                    text: Uni.I18n.translate('general.comPortPools', 'MDC', 'Communication port pools'),
                    href: '#/setup/comportpools',
                    route: 'comportpools'
                },
                {
                    text: Uni.I18n.translate('general.deviceComProtocols', 'MDC', 'Device communication protocols'),
                    href: '#/setup/devicecommunicationprotocols',
                    route: 'devicecommunicationprotocols'
                },
                {
                    text: Uni.I18n.translate('general.comSchedules', 'MDC', 'Communication schedules'),
                    href: '#/setup/communicationschedules',
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
