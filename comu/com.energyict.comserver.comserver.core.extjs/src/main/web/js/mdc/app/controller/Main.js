Ext.define('Mdc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Mdc.controller.setup.SetupOverview',
        'Mdc.controller.setup.ComServers',
        'Mdc.controller.setup.ComPortPools',
        'Mdc.controller.history.Setup',
        'Mdc.controller.setup.DeviceCommunicationProtocol',
        'Mdc.controller.setup.LicensedProtocol',
        'Mdc.controller.setup.DeviceTypes',
        'Mdc.controller.setup.RegisterTypes',
        'Mdc.controller.setup.RegisterMappings',
        'Mdc.controller.setup.DeviceConfigurations'
    ],

    controllers: [
        'Mdc.controller.setup.ComPortPools',
        'Mdc.controller.setup.DeviceTypes',
        'Mdc.controller.setup.SetupOverview',
        'Mdc.controller.setup.ComServers',
        'Mdc.controller.history.Setup',
        'Mdc.controller.setup.DeviceCommunicationProtocol',
        'Mdc.controller.setup.LicensedProtocol',
        'Mdc.controller.setup.RegisterTypes',
        'Mdc.controller.setup.RegisterMappings',
        'Mdc.controller.setup.DeviceConfigurations'
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
        var me= this;
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Setup',
            href: me.getApplication().getController('Mdc.controller.history.Setup').tokenizeShowOverview(),
            glyph: 'xe01d@icomoon'
        });
        debugger;
        Uni.store.MenuItems.add(menuItem);
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
            this.getContentPanel().remove(widget,true);
        }
    }
});
