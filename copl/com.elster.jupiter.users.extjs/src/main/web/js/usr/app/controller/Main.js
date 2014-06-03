Ext.define('Usr.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Usr.controller.Home',
        'Usr.controller.User',
        'Usr.controller.UserEdit',
        'Usr.controller.Group',
        'Usr.controller.GroupEdit',
        'Usr.controller.history.UserManagement'
    ],

    controllers: [
        'Usr.controller.Login',
        'Usr.controller.Home',
        'Usr.controller.User',
        'Usr.controller.UserEdit',
        'Usr.controller.Group',
        'Usr.controller.GroupEdit',
        'Usr.controller.history.UserManagement'
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
        var me = this;
        this.initNavigation();

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'User management',
            glyph: 'settings',
            portal: 'usermanagement',
            index: -10
        });

        Uni.store.MenuItems.add(menuItem);

        var users = Ext.create('Uni.model.PortalItem', {
            title: 'User management',
            portal: 'usermanagement',
            route: 'usermanagement',
            items: [
                {
                    text: 'Users',
                    href: '#usermanagement/users'
                },
                {
                    text: 'Roles',
                    href: '#usermanagement/roles'
                }
            ]
        });

        Uni.store.PortalItems.add(
            users
        );
    },

    initNavigation: function () {
        var controller = this.getController('Uni.controller.Navigation');
        this.setNavigationController(controller);
    },

    showContent: function (widget) {
        this.clearContentPanel();
        this.getContentPanel().add(widget);
        this.getContentPanel().doComponentLayout();
    },

    clearContentPanel: function () {
        this.getContentPanel().removeAll(false);
    }
});