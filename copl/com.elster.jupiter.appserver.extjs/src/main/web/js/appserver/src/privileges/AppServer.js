/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Fim.privileges.DataImport
 *
 * Class that defines privileges for DataImport
 */

Ext.define('Apr.privileges.AppServer', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administrate.appServer',
        'privilege.view.appServer', 'privilege.view.ViewTaskOverview', 'privilege.edit.AdministrateTaskOverview'],

    admin: ['privilege.administrate.appServer'],

    taskOverview: ['privilege.view.ViewTaskOverview'],
    suspendTaskOverview: ['privilege.suspend.SuspendTaskOverview'],
    administrateTaskOverview: ['privilege.edit.AdministrateTaskOverview'],

    all: function () {
        return Ext.Array.merge(Apr.privileges.AppServer.view);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Apr.privileges.AppServer.view);
    },
    canAdministrate: function() {
        return Uni.Auth.checkPrivileges(Apr.privileges.AppServer.admin);
    }
    // ,
    // canSuspendTask: function() {
    //     return Uni.Auth.checkPrivileges(Apr.privileges.AppServer.suspendTaskOverview);
    // },

});