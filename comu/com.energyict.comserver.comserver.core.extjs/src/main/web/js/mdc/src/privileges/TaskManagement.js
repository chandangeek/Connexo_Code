/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Mdc.privileges.Communication
 *
 * Class that defines privileges for Communication
 */
Ext.define('Mdc.privileges.TaskManagement', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    view: ['privilege.view.ViewTaskOverview', 'privilege.edit.AdministerTaskOverview'],
    suspendTaskOverview: ['privilege.suspend.SuspendTaskOverview'],
    administrateTaskOverview: ['privilege.edit.AdministerTaskOverview'],

    all: function () {
        return Ext.Array.merge(Mdc.privileges.TaskManagement.view);
    },

    canView: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.TaskManagement.view);
    }
});
