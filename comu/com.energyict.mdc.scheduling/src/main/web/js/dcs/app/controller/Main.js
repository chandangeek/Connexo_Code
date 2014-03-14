Ext.define('Cfg.controller.Main', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.Navigation'
    ],
    controllers: [
        'Cfg.controller.history.Schedule'
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
        var me=this;
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'DC Scheduling',
            href: me.getApplication().getController('Cfg.controller.history.Schedule').tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
        this.initNavigation();
        this.initDefaultHistoryToken();
    },

    initNavigation: function () {
        var controller = this.getController('Uni.controller.Navigation');
        this.setNavigationController(controller);
    },

    initDefaultHistoryToken: function () {
        var controller = this.getController('Cfg.controller.history.Schedule'),
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
