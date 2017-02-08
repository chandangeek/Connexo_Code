/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.privileges.Alarm', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    viewDeviceCommunication: ['privilege.administrate.deviceData', 'privilege.view.device', 'privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'],
    viewLogbook: ['Mdc.privileges.MasterData.view'],
    viewUsagePoint: ['privilege.view.anyUsagePoint', 'privilege.view.ownUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    viewAdminProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    viewAdminAlarm: ['privilege.view.alarm', 'privilege.comment.alarm', 'privilege.close.alarm', 'privilege.assign.alarm', 'privilege.action.alarm'],
    viewAdminAlarmCreationRule: ['privilege.view.alarm.creationRule', 'privilege.administrate.alarm.creationRule'],
    createAlarmRule: ['privilege.administrate.alarm.creationRule'],
    adminDevice: ['privilege.comment.alarm', 'privilege.close.alarm', 'privilege.assign.alarm', 'privilege.action.alarm'],
    comment: ['privilege.comment.alarm'],
    assign: ['privilege.assign.alarm'],
    close: ['privilege.close.alarm'],
    viewProcesses: ['privilege.view.bpm'],
    executeProcesses: ['privilege.execute.processes.lvl.1',
        'privilege.execute.processes.lvl.2',
        'privilege.execute.processes.lvl.3',
        'privilege.execute.processes.lvl.4'],

    executeLevel1: ['privilege.execute.processes.lvl.1'],
    executeLevel2: ['privilege.execute.processes.lvl.2'],
    executeLevel3: ['privilege.execute.processes.lvl.3'],
    executeLevel4: ['privilege.execute.processes.lvl.4'],

    all: function () {
        return Ext.Array.merge(Dal.privileges.Alarm.viewDeviceCommunication,
            Dal.privileges.Alarm.viewLogbook,
            Dal.privileges.Alarm.viewUsagePoint,
            Dal.privileges.Alarm.viewAdminProcesses,
            Dal.privileges.Alarm.viewAdminAlarm,
            Dal.privileges.Alarm.viewAdminAlarmCreationRule,
            Dal.privileges.Alarm.adminDevice,
            Dal.privileges.Alarm.createAlarmRule,
            Dal.privileges.Alarm.comment,
            Dal.privileges.Alarm.assign);
    },


    canViewAdmimAlarm: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewAdminAlarm);
    },
    canViewDeviceCommunication: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewDeviceCommunication);
    },
    canViewLogbook: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewLogbook);
    },
    canViewUsagePoint: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewUsagePoint);
    },
    canViewProcesses: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewAdminProcesses);
    },
    canComment: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.comment);
    },
    canAssign: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.assign);
    },
    canDoAction: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.action);
    },
    canExecuteLevel1: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.executeLevel1);
    },
    canExecuteLevel2: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.executeLevel2);
    },
    canExecuteLevel3: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.executeLevel3);
    },
    canExecuteLevel4: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.executeLevel4);
    },
    canViewProcessMenu: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewAdminProcesses) && Uni.Auth.checkPrivileges(Dal.privileges.Alarm.executeProcesses);
    },
    canViewProcesses: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewAdminProcesses);
    },
    canViewAdminAlarmCreationRule: function () {
        return Uni.Auth.checkPrivileges(Dal.privileges.Alarm.viewAdminAlarmCreationRule);
    },
});
