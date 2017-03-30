/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.privileges.MetrologyConfiguration', {
    requires: [
        'Uni.Auth',
        'Uni.util.CheckAppStatus'
    ],
    singleton: true,

    view: ['privilege.view.metrologyConfiguration'],
    admin: ['privilege.administer.metrologyConfiguration'],

    all: function () {
        return Ext.Array.merge(Mdc.privileges.MetrologyConfiguration.view, Mdc.privileges.MetrologyConfiguration.admin);
    },

    full: function () {
        return !Uni.util.CheckAppStatus.insightAppIsActive() && Uni.Auth.checkPrivileges(Mdc.privileges.MetrologyConfiguration.all());
    },

    canView: function () {
        return !Uni.util.CheckAppStatus.insightAppIsActive() && Uni.Auth.checkPrivileges(Mdc.privileges.MetrologyConfiguration.view);
    },

    canAdmin: function () {
        return !Uni.util.CheckAppStatus.insightAppIsActive() && Uni.Auth.checkPrivileges(Mdc.privileges.MetrologyConfiguration.admin);
    }
});