/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.privileges.UsagePoint', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administer.anyUsagePoint', 'privilege.view.anyUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.view.ownUsagePoint'],
    admin: ['privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    adminTimeSlicedCps: ['privilege.administer.usage.point.time.sliced.cps'],
    adminCalendars: ['privilege.administrate.touCalendars'],
    manageAttributes: ['privilege.administer.usage.point.manage.attributes'],
    flag: ['privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint', 'privilege.view.anyUsagePoint', 'privilege.view.ownUsagePoint'],
    viewValidationConfiguration: ['privilege.administer.anyUsagePoint', 'privilege.view.anyUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.view.ownUsagePoint',
        'privilege.administer.usagepoint.validationConfiguration'],
    administerValidationConfiguration: ['privilege.administer.usagepoint.validationConfiguration'],
    viewEstimationConfiguration: ['privilege.administer.anyUsagePoint', 'privilege.view.anyUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.view.ownUsagePoint',
        'privilege.administer.usagepoint.estimationConfiguration'],
    administerEstimationConfiguration: ['privilege.administer.usagepoint.estimationConfiguration'],
    all: function() {
        return Ext.Array.merge(Imt.privileges.UsagePoint.view, Imt.privileges.UsagePoint.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.view);
    },
    canAdministrate:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.admin);
    },
    canAdministrateTimeSlicedCps: function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.adminTimeSlicedCps);
    },
    hasFullAdministrateTimeSlicedCps: function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.adminTimeSlicedCps) && Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.admin);
    },
    canAdministrateCalendars: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.adminCalendars) && Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.admin)
    },
    hasBulkActionPrivileges: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.adminCalendars) && Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.admin)
    },
    canFlag: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.flag);
    },
    canViewValidationConfiguration: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.viewValidationConfiguration);
    },
    canViewEstimationConfiguration: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.viewEstimationConfiguration);
    },
    canAdministerValidationConfiguration: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.administerValidationConfiguration);
    },
    canAdministerEstimationConfiguration: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.administerEstimationConfiguration);
    }
});