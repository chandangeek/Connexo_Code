/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.privileges.TaskManagement', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    view: ['privilege.view.ViewTaskOverview'],

    all: function () {
        return Ext.Array.merge(Imt.privileges.TaskManagement.view);
    },

    canView: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.TaskManagement.view);
    }
});