/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Isu.privileges.Issue
 *
 * Class that defines privileges for Issue
 */
Ext.define('Isu.privileges.Issue', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    runTask: 'privilege.run.ScheduleEstimationTask',
    createRule: ['privilege.administrate.creationRule'],
    comment: ['privilege.comment.issue'],
    adminRule: ['privilege.view.assignmentRule','privilege.administrate.creationRule','privilege.view.creationRule'],
    viewRule: ['privilege.view.assignmentRule'],
    adminCreateRule: ['privilege.administrate.creationRule','privilege.view.creationRule'],
    assign: ['privilege.assign.issue'],
    close: ['privilege.close.issue'],
    commentOrAssing: ['privilege.comment.issue','privilege.assign.issue'],
    notify: ['privilege.action.issue'],
    action: ['privilege.action.issue'],
    adminDevice: ['privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
    viewAdminDevice: ['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'],
    viewAdminProcesses: ['privilege.view.bpm', 'privilege.administrate.bpm'],
    viewProcesses: ['privilege.view.bpm'],
    executeProcesses: ['privilege.execute.processes.lvl.1',
        'privilege.execute.processes.lvl.2',
        'privilege.execute.processes.lvl.3',
        'privilege.execute.processes.lvl.4'],

    executeLevel1: ['privilege.execute.processes.lvl.1'],
    executeLevel2: ['privilege.execute.processes.lvl.2'],
    executeLevel3: ['privilege.execute.processes.lvl.3'],
    executeLevel4: ['privilege.execute.processes.lvl.4'],


    all: function() {
        return Ext.Array.merge(Isu.privileges.Issue.createRule, Isu.privileges.Issue.comment,Isu.privileges.Issue.adminRule,
            Isu.privileges.Issue.viewRule, Isu.privileges.Issue.adminCreateRule,Isu.privileges.Issue.assign,Isu.privileges.Issue.close,Isu.privileges.Issue.notify,
            Isu.privileges.Issue.adminDevice,Isu.privileges.Issue.viewAdminDevice);

    },

    canComment: function(){
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.comment );
    },

    canDoAction: function(){
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.action );
    },

    canAdminRule: function(){
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.adminRule);
    },

    canViewRule: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.viewRule);
    },

    canAdminCreateRule: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.adminCreateRule);
    },

    canViewAdminDevice: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.viewAdminDevice);
    },

    canExecuteLevel1: function () {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.executeLevel1);
    },
    canExecuteLevel2: function () {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.executeLevel2);
    },
    canExecuteLevel3: function () {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.executeLevel3);
    },
    canExecuteLevel4: function () {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.executeLevel4);
    },
    canViewProcessMenu: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.viewAdminProcesses) && Uni.Auth.checkPrivileges(Isu.privileges.Issue.executeProcesses);
    },
    canViewProcesses: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.viewAdminProcesses);
    }

});
