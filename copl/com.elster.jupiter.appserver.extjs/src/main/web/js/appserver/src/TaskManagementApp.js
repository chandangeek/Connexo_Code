/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.TaskManagementApp', {
    singleton: true,
    requires: [
        'Ext.util.HashMap'
    ],

    applications: new Ext.util.HashMap,
    customTaskTypes: null,
    dependencesCounter: 0,

    addTaskManagementApp: function (name, application) {
        this.applications.add(name, application);
    },

    setCustomTasksTypes: function (records) {
        this.customTaskTypes = records;
    },

    getTaskManagementApps: function () {
        return this.applications;
    },

    canAdministrate: function () {
        var canAdmin = false;

        this.getTaskManagementApps().each(function (key, value, length) {
            canAdmin |= value.controller && value.controller.canAdministrate();
        });
        return canAdmin;
    },

    increaseDependency: function (taskType) {
        this.dependencesCounter++;
    },

    reduceDependency: function (taskType) {
        this.dependencesCounter--;
    },

    dependenciesLoaded: function () {
        return this.dependencesCounter === 0;
    }


});
