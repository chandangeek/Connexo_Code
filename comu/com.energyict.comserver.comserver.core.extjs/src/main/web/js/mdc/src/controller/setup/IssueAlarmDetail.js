/**
 * Created by H251853 on 9/25/2017.
 */
Ext.define('Mdc.controller.setup.IssueAlarmDetail', {
    extend: 'Isu.controller.IssueDetail',
    requires: [
        'Isu.privileges.Issue',
        'Isu.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses',
        'Bpm.monitorissueprocesses.store.AlarmProcesses',
        'Uni.util.FormEmptyMessage',
        'Isu.view.issues.EditCommentForm',
        'Mdc.store.device.IssuesAlarms'
    ],

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Issues',
        'Isu.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses',
        'Bpm.monitorissueprocesses.store.AlarmProcesses'
    ],


    onShowOverview: function (deviceId, issueId) {
        this.showOverview(issueId);
    },

    getIssueStore: function () {
        return this.getStore('Mdc.store.device.IssuesAlarms');
    }
})