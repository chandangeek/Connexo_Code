Ext.define('Usr.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation'
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
        this.initNavigation();
        this.initDefaultHistoryToken();
        this.getApplication().on('changecontentevent', this.showContent, this);

        var menuItemGroup = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('group.title', 'USM', 'Roles'),
            href: Usr.getApplication().getHistoryGroupController().tokenizeShowOverview(),
            glyph: 'xe020@icomoon'
        });

        var menuItemUser = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('user.title', 'USM', 'Users'),
            href: Usr.getApplication().getHistoryUserController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItemUser);
        Uni.store.MenuItems.add(menuItemGroup);

        this.control({
            'viewport menuitem[action=logout]': {
                click: this.signout
            }
        });
    },

    initNavigation: function () {
        var controller = this.getController('Uni.controller.Navigation');
        this.setNavigationController(controller);
    },

    initDefaultHistoryToken: function () {
        var setupController = this.getApplication().getHistoryHomeController(),
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
    },
    signout: function () {
        this.getApplication().getHomeController().signout();
    }
});