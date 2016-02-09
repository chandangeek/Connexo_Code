/**
 * @class Sct.privileges.ServiceCallType
 *
 * Class that defines privileges for ServiceCallType
 */

Ext.define('Sct.privileges.ServiceCallType', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administrate.serviceCallType',
        'privilege.view.serviceCallType', 'privilege.view.ViewServiceCallTypeOverview'],

    admin: ['privilege.administrate.serviceCallType'],

    typeOverview: ['privilege.view.ViewServiceCallTypeOverview'],

    all: function () {
        return Ext.Array.merge(Sct.privileges.ServiceCallType.view);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Sct.privileges.ServiceCallType.view);
    },

    canAdministrate: function() {
        return Uni.Auth.checkPrivileges(Sct.privileges.ServiceCallType.admin);
    }
});