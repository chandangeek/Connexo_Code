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
        'privilege.view.importServices',
        'privilege.view.mdc.importServices'],
    viewAdmin: ['privilege.administrate.importServices',
        'privilege.view.importServices'],
    viewMdc: ['privilege.view.mdc.importServices'],

    admin: ['privilege.administrate.importServices'],

    all: function () {
        return Ext.Array.merge(Fim.privileges.DataImport.view);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(typeof(MdcApp) != 'undefined' ? Fim.privileges.DataImport.viewMdc : typeof(SystemApp) != 'undefined' ? Fim.privileges.DataImport.viewAdmin : []);
    },
    getAdmin: function () {
        return typeof(MdcApp) != 'undefined' ? false : typeof(SystemApp) != 'undefined' ? Uni.Auth.checkPrivileges(Fim.privileges.DataImport.admin) : false;
    },
    getAdminPrivilege: function () {
        return typeof(MdcApp) != 'undefined' ? false : typeof(SystemApp) != 'undefined' ? Fim.privileges.DataImport.admin : false;
    }

});