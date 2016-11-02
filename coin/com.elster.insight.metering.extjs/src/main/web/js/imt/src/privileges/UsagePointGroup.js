Ext.define('Imt.privileges.UsagePointGroup', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    
    view: ['privilege.administer.usagePointGroup', 'privilege.administrate.usagePointOfEnumeratedGroup', 'privilege.view.usagePointGroupDetail'],
    administrate: ['privilege.administrate.usagePointGroup'],
    viewGroupDetails : ['privilege.view.usagePointGroupDetail'],
    administrateOrViewDetails: ['privilege.administrate.usagePointGroup', 'privilege.view.usagePointGroupDetail'],
    administrateUsagePointOfEnumeratedGroup: ['privilege.administrate.usagePointOfEnumeratedGroup'],

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
    canAdministrateOrViewDetails:function(){
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointGroup.administrateOrViewDetails );
    },
    canAdministrateUsagePointOfEnumeratedGroup: function () {
        return Uni.Auth.checkPrivileges(Imt.privileges.UsagePointGroup.administrateUsagePointOfEnumeratedGroup);
    }
});
