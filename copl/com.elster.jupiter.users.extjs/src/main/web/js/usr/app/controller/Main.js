Ext.define('Usr.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation'
    ],

    controllers: [
        'Usr.controller.Home',
        'Usr.controller.User',
        'Usr.controller.UserGroups',
        'Usr.controller.Group',
        'Usr.controller.GroupPrivileges',
        'Usr.controller.history.Group',
        'Usr.controller.history.User',
        'Usr.controller.history.Home'
    ],

    stores: [
        'Usr.store.Users'
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

        var me= this;
        var menuItemGroup = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('group.title', 'USM', 'Roles'),
            href: me.getApplication().getController('Usr.controller.history.Group').tokenizeShowOverview(),
            glyph: 'settings'
        });

        var menuItemUser = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('user.title', 'USM', 'Users'),
            href: me.getApplication().getController('Usr.controller.history.User').tokenizeShowOverview(),
            glyph: 'settings'
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
        var setupController = this.getApplication().getController('Usr.controller.history.Home'),
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
            this.getContentPanel().remove(widget, false);
        }
    },
    signout: function () {
        this.getApplication().getController('Usr.controller.Home').signout();
    }
});