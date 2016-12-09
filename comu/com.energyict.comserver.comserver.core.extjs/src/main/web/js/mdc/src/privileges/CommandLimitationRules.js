Ext.define('Mdc.privileges.CommandLimitationRules', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.commandLimitationRule', 'privilege.view.commandLimitationRule'],
    admin: ['privilege.administrate.commandLimitationRule'],
    all: function() {
        return Ext.Array.merge(Mdc.privileges.CommandLimitationRules.view, Mdc.privileges.CommandLimitationRules.admin);
    },
    canView: function(){
        return Uni.Auth.checkPrivileges(Mdc.privileges.CommandLimitationRules.view );
    },
    canAcceptReject: function() {
        // TODO: later on, an extra Accept/Reject privilege should be used here
        return Uni.Auth.checkPrivileges(Mdc.privileges.CommandLimitationRules.admin );
    }
});
