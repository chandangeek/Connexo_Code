Ext.define('Imt.privileges.MetrologyConfig', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['UCR_ADMINISTER_ANY_METROLOGY_CONFIG','UCR_BROWSE_ANY_METROLOGY_CONFIG'],
    admin: ['UCR_ADMINISTER_ANY_METROLOGY_CONFIG'],

    all: function() {
        return Ext.Array.merge(Imt.privileges.MetrologyConfig.view, Imt.privileges.MetrologyConfig.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.view);
    },
    canAdministrate:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.admin);
    }
});