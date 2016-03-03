/**
 * @class Scs.privileges.ServiceCall
 *
 * Class that defines privileges for ServiceCall
 */

Ext.define('Scs.privileges.ServiceCall', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.serviceCalls', 'privilege.administrate.serviceCall'],

    admin: ['privilege.administrate.serviceCall'],

    all: function () {
        return Ext.Array.merge(Scs.privileges.ServiceCall.view);
    },

    canView: function () {
        return Uni.Auth.checkPrivileges(Scs.privileges.ServiceCall.view);
    },

    canAdministrate: function() {
        return Uni.Auth.checkPrivileges(Scs.privileges.ServiceCall.admin);
    }
});