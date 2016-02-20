Ext.define('Imt.privileges.MetrologyConfig', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.metrologyConfiguration', 'privilege.administer.metrologyConfiguration'],
    admin: ['privilege.administer.metrologyConfiguration'],

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