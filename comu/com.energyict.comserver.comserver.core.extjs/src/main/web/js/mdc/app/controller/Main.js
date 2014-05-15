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

        var portalItem1 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.rMR', 'MDC', 'RMR'),
            component: 'Mdc.view.setup.portal.RMR',
            portal: 'setup'
        });

        var portalItem2 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.deviceManagement', 'MDC', 'Device management'),
            component: 'Mdc.view.setup.portal.DeviceManagement',
            portal: 'setup'
        });

        var portalItem3 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.deviceCommunication', 'MDC', 'Device communication'),
            component: 'Mdc.view.setup.portal.DeviceCommunication',
            portal: 'setup'
        });

        Uni.store.PortalItems.add(
            portalItem1, portalItem2, portalItem3
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
