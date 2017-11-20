/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.TaskManagementApp', {
    singleton: true,
    requires: [
        'Ext.util.HashMap'
    ],

    applications: new Ext.util.HashMap,

    addTaskManagementApp: function (name, application) {
        this.applications.add(name, application);
    },

    getTaskManagementApps: function () {
        return this.applications;
    },

    canAdministrate: function () {
        var me = this,
            canAdmin = false;

        me.getTaskManagementApps().each(function (key, value, length) {
            canAdmin |= value.controller && value.controller.canAdministrate();
        });
        return canAdmin;
    }

});
