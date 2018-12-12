/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                                    route.setTitle(Uni.I18n.translate('general.editx', 'USR', "Edit '{0}'", [record.get('name')], false));
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
                    privileges: Usr.privileges.Users.viewUsers,
                    items: {
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                            route: '{id}/edit',
                            controller: 'Usr.controller.UserEdit',
                            privileges: Usr.privileges.Users.adminUsers,
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
                },
                userdirectories: {
                    title: Uni.I18n.translate('general.userDirectories', 'USR', 'User directories'),
                    route: 'userDirectories',
                    controller: 'Usr.controller.UserDirectories',
                    action: 'showUserDirectories',
                    privileges: Usr.privileges.Users.view,
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addUserDirectory', 'USR', 'Add user directory'),
                            route: 'add',
                            controller: 'Usr.controller.UserDirectories',
                            privileges: Usr.privileges.Users.admin,
                            action: 'showAddUserDirectory'
                        },
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                            route: '{userDirectoryId}/edit',
                            controller: 'Usr.controller.UserDirectories',
                            privileges: Usr.privileges.Users.admin,
                            action: 'showEditImportService',
                            callback: function (route) {
                                this.getApplication().on('editUserDirectory', function (record) {
                                    route.setTitle(Ext.String.format(Uni.I18n.translate('userDirectories.edit', 'USR', 'Edit \'{0}\''), record.get('name')));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        },
                        synchronize: {
                            title: Uni.I18n.translate('general.synchronize', 'USR', 'Synchronize'),
                            route: '{userDirectoryId}/synchronize',
                            controller: 'Usr.controller.UserDirectories',
                            privileges: Usr.privileges.Users.admin,
                            action: 'showSynchronizeUserDirectory',
                            callback: function (route) {
                                this.getApplication().on('synchronizeUserDirectory', function (record) {
                                    route.setTitle(Ext.String.format(Uni.I18n.translate('userDirectories.editSynchronize', 'USR', 'Synchronize \'{0}\''), record.get('name')));
                                    return true;
                                }, {single: true});
                                return this;
                            },
                            items: {
                                addUsers: {
                                    title: Uni.I18n.translate('general.selectUsers', 'USR', 'Select users'),
                                    route: 'addUsers',
                                    controller: 'Usr.controller.UserDirectories',
                                    privileges: Usr.privileges.Users.admin,
                                    action: 'showSelectUsers'
                                }
                            }
                        }

                    }
                },
                workgroups: {
                    title: Uni.I18n.translate('general.workgroups', 'USR', 'Workgroups'),
                    route: 'workgroups',
                    controller: 'Usr.controller.Workgroups',
                    action: 'showWorkgroups',
                    privileges: Usr.privileges.Users.view,
                    items: {
                        add: {
                            title: Uni.I18n.translate('general.addWorkgroup', 'USR', 'Add workgroup'),
                            route: 'add',
                            controller: 'Usr.controller.Workgroups',
                            privileges: Usr.privileges.Users.admin,
                            action: 'showAddWorkgroup',
                            items: {
                                users: {
                                    title: Uni.I18n.translate('general.selectUsers', 'USR', 'Select users'),
                                    route: 'users',
                                    controller: 'Usr.controller.Workgroups',
                                    privileges: Usr.privileges.Users.admin,
                                    action: 'addUsers'
                                }
                            }
                        },
                        edit: {
                            title: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                            route: '{workgroupId}/edit',
                            controller: 'Usr.controller.Workgroups',
                            privileges: Usr.privileges.Users.admin,
                            action: 'showEditWorkgroup',
                            callback: function (route) {
                                this.getApplication().on('editWorkgroup', function (record) {
                                    route.setTitle(Ext.String.format(Uni.I18n.translate('workgroups.edit', 'USR', 'Edit \'{0}\''), record.get('name')));
                                    return true;
                                }, {single: true});

                                return this;
                            },
                            items: {
                                users: {
                                    title: Uni.I18n.translate('general.selectUsers', 'USR', 'Select users'),
                                    route: 'users',
                                    controller: 'Usr.controller.Workgroups',
                                    privileges: Usr.privileges.Users.admin,
                                    action: 'addUsers'
                                }
                            }
                        },

                    }
                }
            }
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.getApplication().getController('Uni.controller.history.EventBus').previousPath);
    }
});