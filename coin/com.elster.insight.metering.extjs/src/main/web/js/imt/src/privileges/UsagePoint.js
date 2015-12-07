Ext.define('Imt.privileges.UsagePoint', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['ADMIN_ANY_METROLOGY_CONFIG','BROWSE_ANY_METROLOGY_CONFIG'],
    admin: ['ADMIN_ANY_METROLOGY_CONFIG'],

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