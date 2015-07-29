/**
 * @class Fim.privileges.DataImport
 *
 * Class that defines privileges for DataImport
 */

Ext.define('Apr.privileges.AppServer', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administrate.appServer',
        'privilege.view.appServer'],

    admin: ['privilege.administrate.appServer'],

    all: function () {
        return Ext.Array.merge(Apr.privileges.AppServer.view);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Apr.privileges.AppServer.view);
    }

});