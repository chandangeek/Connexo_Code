Ext.define('Imt.privileges.UsagePoint', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administer.anyUsagePoint', 'privilege.view.anyUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.view.ownUsagePoint'],
    admin: ['privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],

    all: function() {
        return Ext.Array.merge(Imt.privileges.UsagePoint.view, Imt.privileges.UsagePoint.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.view);
    },
    canAdministrate:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePoint.admin);
    },

});