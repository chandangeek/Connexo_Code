/**
 * @class Bpm.privileges.BpmManagement
 *
 * Class that defines privileges for BpmManagement
 */

Ext.define('Bpm.privileges.BpmManagement', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.importServices'],
    admin: ['privilege.administrate.importServices'],

    all: function () {
        return Ext.Array.merge(Bpm.privileges.BpmManagement.view);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Bpm.privileges.BpmManagement.view);
    },
    canAdministrate:function(){
        return Uni.Auth.checkPrivileges(Bpm.privileges.BpmManagement.admin);
    }
});