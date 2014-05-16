Ext.define('Sam.controller.Main', {
    extend: 'Ext.app.Controller',
    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.controller.history.EventBus'
    ],

    controllers: [
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
            text: Uni.I18n.translate('general.administration', 'SAM', 'Administration'),
            href: me.getController('Sam.controller.history.Administration').tokenizeShowOverview(),
            portal: 'administration',
            glyph: 'settings'
        });
        Uni.store.MenuItems.add(menuItem);

        var licensingItem = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.licenses', 'SAM', 'Licensing'),
            portal: 'administration',
            route: 'licensing',
            items: [
                {
                    text: Uni.I18n.translate('general.licenses', 'MDC', 'Licenses'),
                    href: '#/administration/licensing/licenses',
                    route: 'licenses'
                }
            ]
        });

        Uni.store.PortalItems.add(
            licensingItem
        );
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

