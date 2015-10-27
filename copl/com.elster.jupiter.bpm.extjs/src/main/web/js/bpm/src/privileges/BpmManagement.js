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
    all: ['privilege.view.task', 'privilege.assign.task', 'privilege.execute.task'],
    view: ['privilege.view.task'],
    assign: ['privilege.assign.task'],
    execute: ['privilege.execute.task'],
    assignAndExecute: ['privilege.assign.task', 'privilege.execute.task'],

    allProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    viewProcesses: ['privilege.view.bpm'],
    administrateProcesses: ['privilege.administrate.bpm'],

    all: function () {
        return Ext.Array.merge(Bpm.privileges.BpmManagement.all);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Bpm.privileges.BpmManagement.view);
    },
    canAssign: function () {
        return Uni.Auth.checkPrivileges(Bpm.privileges.BpmManagement.assign);
    },
    canExecute: function () {
        return Uni.Auth.checkPrivileges(Bpm.privileges.BpmManagement.execute);
    },
    allProcesses: function () {
        return Ext.Array.merge(Bpm.privileges.BpmManagement.allProcesses);
    },
});