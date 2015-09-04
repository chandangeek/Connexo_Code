Ext.define('Usr.controller.history.UserManagement', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'USR', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                roles: {
                    title: Uni.I18n.translate('general.roles', 'USR', 'Roles'),
                    route: 'roles',
                    controller: 'Usr.controller.Group',
                    privileges: Usr.privileges.Users.view,
                    items: {
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Usr.controller.GroupEdit',
                            privileges: Usr.privileges.Users.admin,
                            action: 'showEditOverview',
                            callback: function (route) {
                                this.getApplication().on('editRole', function (record) {
                                    route.setTitle(Uni.I18n.translate('general.editx', 'USR', "Edit '{0}'",[record.get('name')]));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        add: {
                            title: Uni.I18n.translate('general.add', 'USR', 'Add'),
                            route: 'add',
                            controller: 'Usr.controller.GroupEdit',
                            privileges: Usr.privileges.Users.admin,
                            action: 'showCreateOverview'
                        }
                    }
                },
                users: {
                    title: Uni.I18n.translate('general.users', 'USR', 'Users'),
                    route: 'users',
                    controller: 'Usr.controller.User',
                    privileges: Usr.privileges.Users.view,
                    items: {
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Usr.controller.UserEdit',
                            privileges: Usr.privileges.Users.admin,
                            action: 'showEditOverview',
                            callback: function (route) {
                                this.getApplication().on('editUser', function (record) {
                                    route.setTitle(Uni.I18n.translate('general.edit', 'USR', 'Edit') + ' \'' + record.get('authenticationName') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                }
            }
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.getApplication().getController('Uni.controller.history.EventBus').previousPath);
    }
});