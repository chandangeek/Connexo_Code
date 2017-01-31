/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    executeLevel1: ['privilege.execute.processes.lvl.1'],
    executeLevel2: ['privilege.execute.processes.lvl.2'],
    executeLevel3: ['privilege.execute.processes.lvl.3'],
    executeLevel4: ['privilege.execute.processes.lvl.4'],

    assignOrExecute: ['privilege.assign.task', 'privilege.execute.task'],

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
    },
    canAssignOrExecute: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.assignOrExecute);
    },
    canExecuteLevel1: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.executeLevel1);
    },
    canExecuteLevel2: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.executeLevel2);
    },
    canExecuteLevel3: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.executeLevel3);
    },
    canExecuteLevel4: function () {
        return Uni.Auth.checkPrivileges(Dbp.privileges.DeviceProcesses.executeLevel4);
    }

});