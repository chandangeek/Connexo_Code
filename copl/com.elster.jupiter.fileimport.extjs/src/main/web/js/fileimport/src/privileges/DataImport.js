/**
 * @class Fim.privileges.DataImport
 *
 * Class that defines privileges for DataImport
 */

Ext.define('Fim.privileges.DataImport', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administrate.importServices',
        'privilege.view.importServices'],
    viewHistory: ['privilege.view.import.history',
        'privilege.view.importServices'],
    admin: ['privilege.administrate.importServices'],

    all: function () {
        return Ext.Array.merge(Fim.privileges.DataImport.view);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Fim.privileges.DataImport.view);
    },
    canViewHistory: function () {
        return Uni.Auth.checkPrivileges(Fim.privileges.DataImport.viewHistory);
    },
    getAdmin: function () {
        return typeof(MdcApp) != 'undefined' ? false : typeof(MdmApp) != 'undefined' ? false : typeof(SystemApp) != 'undefined' ? Uni.Auth.checkPrivileges(Fim.privileges.DataImport.admin) : false;
    },
    getAdminPrivilege: function () {
        return typeof(MdcApp) != 'undefined' ? false : typeof(MdmApp) != 'undefined' ? false : typeof(SystemApp) != 'undefined' ? Fim.privileges.DataImport.admin : false;
    }

});