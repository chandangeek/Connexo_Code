/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.privileges.Issue', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    viewDeviceCommunication: ['privilege.administrate.deviceData', 'privilege.view.device', 'privilege.administrate.deviceCommunication', 'privilege.operate.deviceCommunication'],
    viewLogbook: ['privilege.administrate.masterData', 'privilege.view.masterData'],
    viewUsagePoint: ['privilege.view.anyUsagePoint', 'privilege.view.ownUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.administer.anyUsagePoint'],
    viewAdminProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    viewAdminIssue: ['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'],
    viewAdminIssueCreationRule: ['privilege.view.issue.creationRule', 'privilege.administrate.issue.creationRule'],
    createIssueRule: ['privilege.administrate.issue.creationRule'],
    adminDevice: ['privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'],
    commentOrAssing: ['privilege.comment.issue', 'privilege.assign.issue'],
    closeOrAssing: ['privilege.close.issue', 'privilege.assign.issue'],
    comment: ['privilege.comment.issue'],
    assign: ['privilege.assign.issue'],
    snooze: ['privilege.snooze.issue'],
    action: ['privilege.action.issue'],
    close: ['privilege.close.issue'],
    setPriority: ['privilege.setPriority.issue'],
    viewProcesses: ['privilege.view.bpm'],
    executeProcesses: ['privilege.execute.processes.lvl.1',
        'privilege.execute.processes.lvl.2',
        'privilege.execute.processes.lvl.3',
        'privilege.execute.processes.lvl.4'],

    executeLevel1: ['privilege.execute.processes.lvl.1'],
    executeLevel2: ['privilege.execute.processes.lvl.2'],
    executeLevel3: ['privilege.execute.processes.lvl.3'],
    executeLevel4: ['privilege.execute.processes.lvl.4'],
    insightView: ['privilege.administer.anyUsagePoint', 'privilege.view.anyUsagePoint', 'privilege.administer.ownUsagePoint', 'privilege.view.ownUsagePoint'],
    all: function () {
        return Ext.Array.merge(Itk.privileges.Issue.viewDeviceCommunication,
            Itk.privileges.Issue.viewLogbook,
            Itk.privileges.Issue.viewUsagePoint,
            Itk.privileges.Issue.viewAdminProcesses,
            Itk.privileges.Issue.viewAdminIssue,
            Itk.privileges.Issue.viewAdminIssueCreationRule,
            Itk.privileges.Issue.adminDevice,
            Itk.privileges.Issue.createIssueRule,
            Itk.privileges.Issue.comment,
            Itk.privileges.Issue.assign);
    },


    canViewAdmimIssue: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.viewAdminIssue);
    },
    canViewDeviceCommunication: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.viewDeviceCommunication);
    },
    canViewLogbook: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.viewLogbook);
    },
    canViewUsagePoint: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.viewUsagePoint);
    },
    canViewProcesses: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.viewAdminProcesses);
    },
    canComment: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.comment);
    },
    canAssign: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.assign);
    },
    canSnooze: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.snooze);
    },
    canDoAction: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.action);
    },
    canExecuteLevel1: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.executeLevel1);
    },
    canExecuteLevel2: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.executeLevel2);
    },
    canExecuteLevel3: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.executeLevel3);
    },
    canExecuteLevel4: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.executeLevel4);
    },
    canViewProcessMenu: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.viewAdminProcesses) && Uni.Auth.checkPrivileges(Itk.privileges.Issue.executeProcesses);
    },
    canViewAdminIssueCreationRule: function () {
        return Uni.Auth.checkPrivileges(Itk.privileges.Issue.viewAdminIssueCreationRule);
    },
    canViewUsagePointByApp: function () {
        return !Uni.util.CheckAppStatus.insightAppIsActive() && Uni.Auth.checkPrivileges(Itk.privileges.Issue.viewUsagePoint);
    },
    canViewUsagePointInInsight: function () {
        var result = false;
        Itk.privileges.Issue.insightView.forEach(function (item) {
            if (Uni.Auth.hasPrivilegeInApp(item, 'INS')) {
                result = true;
            }
        });
        return result;
    }
});
