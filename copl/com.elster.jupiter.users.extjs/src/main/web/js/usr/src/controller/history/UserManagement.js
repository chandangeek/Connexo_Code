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
                    title: Uni.I18n.translate('group.title', 'USR', 'Roles'),
                    route: 'roles',
                    controller: 'Usr.controller.Group',
                    privileges: ['privilege.administrate.userAndRole','privilege.view.userAndRole'],
                    items: {
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Usr.controller.GroupEdit',
                            privileges: ['privilege.administrate.userAndRole'],
                            action: 'showEditOverview',
                            callback: function (route) {
                                this.getApplication().on('editRole', function (record) {
                                    route.setTitle(Uni.I18n.translate('general.edit', 'USR', 'Edit') + ' \'' + record.get('name') + '\'');
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        add: {
                            title: Uni.I18n.translate('general.add', 'USR', 'Add'),
                            route: 'add',
                            controller: 'Usr.controller.GroupEdit',
                            privileges: ['privilege.administrate.userAndRole'],
                            action: 'showCreateOverview'
                        }
                    }
                },
                users: {
                    title: Uni.I18n.translate('user.title', 'USR', 'Users'),
                    route: 'users',
                    controller: 'Usr.controller.User',
                    privileges: ['privilege.administrate.userAndRole','privilege.view.userAndRole'],
                    items: {
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Usr.controller.UserEdit',
                            privileges: ['privilege.administrate.userAndRole'],
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