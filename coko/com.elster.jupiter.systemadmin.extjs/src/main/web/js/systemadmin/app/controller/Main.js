Ext.define('Sam.controller.Main', {
    extend: 'Ext.app.Controller',
    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.history.EventBus'
    ],

    controllers: [
        'Sam.controller.Administration',
        'Sam.controller.history.Administration',
        'Sam.controller.licensing.Licenses',
        'Sam.controller.licensing.Upload'
    ],

    stores: [
        'Sam.store.Licensing'
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
        this.initMenu();
        this.initNavigation();
        this.initDefaultHistoryToken();
    },

    initMenu: function () {
        var me = this;

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'System administration',
            href: me.getController('Sam.controller.history.Administration').tokenizeShowOverview(),
            glyph: 'settings'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    initNavigation: function () {
        var navigationController = this.getController('Uni.controller.Navigation');
        this.setNavigationController(navigationController);

    },

    initDefaultHistoryToken: function () {
        var adminController = this.getController('Sam.controller.history.Administration'),
            eventBus = this.getController('Uni.controller.history.EventBus'),
            defaultToken = adminController.tokenizeShowOverview();

        eventBus.setDefaultToken(defaultToken);
    }
});

