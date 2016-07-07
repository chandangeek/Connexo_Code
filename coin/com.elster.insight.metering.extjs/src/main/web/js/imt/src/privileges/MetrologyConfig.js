Ext.define('Imt.privileges.MetrologyConfig', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.metrologyConfiguration', 'privilege.administer.metrologyConfiguration'],
    viewValidation: ['privilege.administrate.metrologyConfiguration.validation', 'privilege.view.metrologyConfiguration.validation'],
    admin: ['privilege.administer.metrologyConfiguration'],
    adminValidation: ['privilege.administrate.metrologyConfiguration.validation'],    

    all: function() {
        return Ext.Array.merge(Imt.privileges.MetrologyConfig.view, Imt.privileges.MetrologyConfig.admin, Imt.privileges.MetrologyConfig.viewValidation, Imt.privileges.MetrologyConfig.adminValidation);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.view);
    },
    canAdministrate:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.admin);
    },
    canViewValidation:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.viewValidation);
    },
    canAdministrateValidation:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.MetrologyConfig.adminValidation);
    }
});