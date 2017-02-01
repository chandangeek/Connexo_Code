/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.privileges.UsagePoint', {
    requires: [
        'Uni.Auth',
        'Uni.store.Apps'
    ],
    singleton: true,
    appsStoreLoaded: false,
    insightStatus: false,

    view: ['privilege.view.anyUsagePoint', 'privilege.view.ownUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    admin: ['privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    insightView: ['privilege.administer.anyUsagePoint', 'privilege.view.anyUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.view.ownUsagePoint'],
    insightAdmin: ['privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    all: function () {
        return !Uni.util.CheckAppStatus.insightAppIsActive() && Uni.Auth.checkPrivileges(Ext.Array.merge(Mdc.privileges.UsagePoint.view, Mdc.privileges.UsagePoint.admin));
    },
    canView: function () {
        return !Uni.util.CheckAppStatus.insightAppIsActive() && Uni.Auth.checkPrivileges(Mdc.privileges.UsagePoint.view);
    },

    canAdmin: function () {
        return !Uni.util.CheckAppStatus.insightAppIsActive() && Uni.Auth.checkPrivileges(Mdc.privileges.UsagePoint.admin);
    },

    canViewInInsight: function () {
        var result = false;
        Mdc.privileges.UsagePoint.insightView.forEach(function (item) {
            if (Uni.Auth.hasPrivilegeInApp(item, 'INS')) {
                result = true;
            }
        });
        return result;
    }
});
