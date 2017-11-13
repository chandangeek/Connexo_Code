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
    }
});
