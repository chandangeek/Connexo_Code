Ext.define('Cfg.controller.Main', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.Navigation'
    ],
    controllers: [
        'Cfg.controller.history.Validation',
        'Cfg.controller.history.EventType'
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
            text: Uni.I18n.translate('general.administration', 'CFG', 'Administration'),
            href: me.getApplication().getController('Cfg.controller.history.Validation').tokenizeShowOverview(),
            portal: 'administration',
            glyph: 'settings'
        });

        Uni.store.MenuItems.add(menuItem);

        var portalItem1 = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.validation', 'CFG', 'Validation'),
            portal: 'administration',
            items: [
                {
                    text: Uni.I18n.translate('general.validationRuleSets', 'CFG', 'Validation rule sets'),
                    href: '#/administration/validation'
                }
            ]
        });

        Uni.store.PortalItems.add(
            portalItem1
        );

        this.initNavigation();
        this.initDefaultHistoryToken();
    },

    initNavigation: function () {
        var controller = this.getController('Uni.controller.Navigation');
        this.setNavigationController(controller);
    },

    initDefaultHistoryToken: function () {
        var controller = this.getController('Cfg.controller.history.Validation'),
            eventBus = this.getController('Uni.controller.history.EventBus'),
            defaultToken = controller.tokenizeShowOverview();

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
