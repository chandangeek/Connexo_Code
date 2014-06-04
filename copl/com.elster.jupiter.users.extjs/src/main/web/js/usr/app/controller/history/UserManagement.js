Ext.define('Usr.controller.history.UserManagement', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'usermanagement',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        usermanagement: {
            title: 'User Management',
            route: 'usermanagement',
            disabled: true,
            items: {
                roles: {
                    title: Uni.I18n.translate('group.title', 'USM', 'Roles'),
                    route: 'roles',
                    controller: 'Usr.controller.Group',
                    items: {
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Usr.controller.GroupEdit',
                            action: 'showEditOverview'
                        },
                        create: {
                            title: Uni.I18n.translate('general.create', 'USM', 'Create'),
                            route: 'create',
                            controller: 'Usr.controller.GroupEdit',
                            action: 'showCreateOverview'
                        }
                    }
                },
                users: {
                    title: Uni.I18n.translate('user.title', 'USM', 'Users'),
                    route: 'users',
                    controller: 'Usr.controller.User',
                    items: {
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Usr.controller.UserEdit',
                            action: 'showEditOverview'
                        }
                        /*login: {
                            title: 'login',
                            route: 'login',
                            controller: 'Usr.controller.Login'
                        }*/
                    }
                }
            }
        }
    },

    //init: function () {
    //    this.getController('Uni.controller.history.Router').addConfig(this.routeConfig);
    //},

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.getApplication().getController('Uni.controller.history.EventBus').previousPath);
    }
});