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
                    title: 'Roles',
                    route: 'roles',
                    controller: 'Usr.controller.Group',
                    items: {
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Usr.controller.GroupPrivileges',
                            action: 'showEditOverview'
                        },
                        create: {
                            title: 'Create',
                            route: 'create',
                            controller: 'Usr.controller.GroupPrivileges',
                            action: 'showCreateOverview'
                        }
                    }
                },
                users: {
                    title: 'Users',
                    route: 'users',
                    controller: 'Usr.controller.User',
                    items: {
                        edit: {
                            title: 'Edit',
                            route: '{id}/edit',
                            controller: 'Usr.controller.UserGroups',
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