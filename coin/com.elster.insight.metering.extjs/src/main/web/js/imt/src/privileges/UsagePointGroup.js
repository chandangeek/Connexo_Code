/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.privileges.UsagePointGroup', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    
    view: ['privilege.administer.usagePointGroup', 'privilege.administer.usagePointEnumeratedGroup', 'privilege.view.usagePointGroupDetail'],
    administrate: ['privilege.administer.usagePointGroup'],
    viewGroupDetails : ['privilege.view.usagePointGroupDetail'],
    administrateOrViewDetails: ['privilege.administer.usagePointGroup', 'privilege.view.usagePointGroupDetail'],
    administrateUsagePointOfEnumeratedGroup: ['privilege.administer.usagePointEnumeratedGroup'],
    administrateAnyOrStaticGroup: ['privilege.administer.usagePointGroup', 'privilege.administer.usagePointEnumeratedGroup'],
    flag: ['privilege.administer.usagePointGroup', 'privilege.administer.usagePointEnumeratedGroup', 'privilege.view.usagePointGroupDetail'],
    all: function () {
        return Ext.Array.merge(Imt.privileges.UsagePointGroup.view, Imt.privileges.UsagePointGroup.administrate);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointGroup.view);
    },
    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointGroup.administrate);
    },
    canViewGroupDetails: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointGroup.viewGroupDetails);
    },
    canAdministrateUsagePointOfEnumeratedGroup: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointGroup.administrateUsagePointOfEnumeratedGroup);
    },
    canFlag: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointGroup.flag);
    }
});
