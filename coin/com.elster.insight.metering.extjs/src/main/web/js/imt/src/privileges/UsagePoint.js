Ext.define('Imt.privileges.UsagePoint', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['MTR_ADMIN_ANYUSAGEPOINT','MTR_BROWSE_ANYUSAGEPOINT','MTR_ADMIN_OWN','MTR_BROWSE_OWNUSAGEPOINT'],
    admin: ['MTR_ADMIN_OWN','MTR_ADMIN_ANYUSAGEPOINT'],

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