Ext.define('Usr.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation'
    ],

    controllers: [
        'Usr.controller.Group',
        'Usr.controller.GroupEdit',
        'Usr.controller.User',
        'Usr.controller.UserEdit',
        'Usr.controller.history.UserManagement'
    ],

    stores: [
        'Usr.store.Users'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Usr.controller.history.UserManagement'); // Forces route registration.

        me.initMenu();
    },

    initMenu: function () {
        if (Uni.Auth.hasPrivilege('privilege.view.userAndRole')) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'USR', 'Administration'),
                glyph: 'settings',
                portal: 'administration',
                index: -10
            });

            Uni.store.MenuItems.add(menuItem);

            var usersItems = [];

            if (Uni.Auth.hasPrivilege('privilege.view.userAndRole')) {
                usersItems.push(
                    {
                        text: Uni.I18n.translate('general.users', 'USR', 'Users'),
                        href: '#/administration/users'
                    }
                );
            }

            if (Uni.Auth.hasPrivilege('privilege.view.userAndRole')) {
                usersItems.push(
                    {
                        text: Uni.I18n.translate('general.roles', 'USR', 'Roles'),
                        href: '#/administration/roles'
                    }
                );
            }

            var users = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.userManagement', 'USR', 'User management'),
                portal: 'administration',
                route: 'administration',
                items: usersItems
            });

            Uni.store.PortalItems.add(
                users
            );
        }
    },

    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});