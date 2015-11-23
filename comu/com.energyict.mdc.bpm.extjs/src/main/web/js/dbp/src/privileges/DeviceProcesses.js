/**
 * @class Dbp.privileges.DeviceProcesses
 *
 * Class that defines privileges for DeviceProcesses
 */

Ext.define('Dbp.privileges.DeviceProcesses', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    all: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    allPrivileges: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    view: ['privilege.view.bpm'],
    administrate: ['privilege.administrate.bpm'],

    allProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    viewProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    administrateProcesses: ['privilege.administrate.bpm'],

    all: function () {
        return Ext.Array.merge(Dbp.privileges.DeviceProcesses.all);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.view);
    },
    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.administrate);
    },
    allBpmProcesses: function () {
        return Ext.Array.merge(Dbp.privileges.DeviceProcesses.allProcesses);
    },
    canViewProcesses: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.viewProcesses);
    },
    canAdministrateProcesses: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.administrateProcesses);
    }
});