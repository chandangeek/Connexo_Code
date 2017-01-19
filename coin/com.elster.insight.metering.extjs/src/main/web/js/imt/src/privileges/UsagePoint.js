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
    }
});