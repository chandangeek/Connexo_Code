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
        me.initNavigation();

        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.userManagement', 'USR', 'User management'),
            glyph: 'settings',
            portal: 'usermanagement',
            index: -10,
            hidden: Uni.Auth.hasNoPrivilege('privilege.view.user') & Uni.Auth.hasNoPrivilege('privilege.view.group')
        });

        Uni.store.MenuItems.add(menuItem);

        var users = Ext.create('Uni.model.PortalItem', {
            title: Uni.I18n.translate('general.userManagement', 'USR', 'User management'),
            portal: 'usermanagement',
            route: 'usermanagement',
            items: [
                {
                    text: Uni.I18n.translate('general.users', 'USR', 'Users'),
                    href: '#/usermanagement/users',
                    hidden: Uni.Auth.hasNoPrivilege('privilege.view.user')
                },
                {
                    text: Uni.I18n.translate('general.roles', 'USR', 'Roles'),
                    href: '#/usermanagement/roles',
                    hidden: Uni.Auth.hasNoPrivilege('privilege.view.group')
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