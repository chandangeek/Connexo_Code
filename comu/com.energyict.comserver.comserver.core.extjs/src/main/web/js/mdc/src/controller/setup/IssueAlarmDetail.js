/**
 * Created by H251853 on 9/25/2017.
 */
Ext.define('Mdc.controller.setup.IssueAlarmDetail', {
    extend: 'Ext.app.Controller',
    requires: [
        'Isu.privileges.Issue',
        'Isu.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses',
        'Bpm.monitorissueprocesses.store.AlarmProcesses',
        'Uni.util.FormEmptyMessage',
        'Isu.view.issues.EditCommentForm',
        'Mdc.store.device.IssuesAlarms',
        'Isu.controller.ApplyIssueAction',
        'Isu.controller.SetPriority'
    ],

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Issues',
        'Isu.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses',
        'Bpm.monitorissueprocesses.store.AlarmProcesses'
    ],


    onShowOverview: function (deviceId, issueId) {
        var me = this,
            store = me.getStore('Mdc.store.device.IssuesAlarms'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            issueType = queryString.issueType;

        if (store.getCount()) {
            var issueActualType = store.getById(parseInt(issueId)).get('issueType').uid;
            if (issueActualType != issueType) {
                queryString.issueType = issueActualType;
                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                issueType = issueActualType;
            }
        }

        if ((issueType === 'datacollection') || (issueType === 'datavalidation') || (issueType === 'devicelifecycle')) {
            this.getController('Isu.controller.IssueDetail').showOverview(issueId);
        }
        else if (issueType === 'devicealarm') {
            this.getController('Dal.controller.Detail').showOverview(issueId);
        }
    },

    /*   showActionOverview: function (deviceId, issueId, actionId) {
        var me = this,
            store = me.getStore('Mdc.store.device.IssuesAlarms'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            issueType = queryString.issueType;

        if (store.getCount()) {
            var issueActualType = store.getById(parseInt(issueId)).get('issueType').uid;
            if (issueActualType != issueType) {
                queryString.issueType = issueActualType;
                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                issueType = issueActualType;
            }
        }

        if ((issueType === 'datacollection') || (issueType === 'datavalidation')) {
            if (actionId) {
                me.getController('Isu.controller.ApplyIssueAction').queryParams = {activeTab: 'issues'};
                me.getController('Isu.controller.ApplyIssueAction').showOverview(issueId, actionId);
            } else {
                me.getController('Isu.controller.ApplyIssueAction').queryParams = {activeTab: 'issues'};
                me.getController('Isu.controller.ApplyIssueAction').showOverview(deviceId, issueId);
            }
        }
        else if (issueType === 'devicealarm') {
            if (actionId) {
                me.getController('Dal.controller.ApplyAction').queryParams = {activeTab: 'issues'};
                me.getController('Dal.controller.ApplyAction').showOverview(issueId, actionId);
            } else {
                me.getController('Dal.controller.ApplyAction').queryParams = {activeTab: 'issues'};
                me.getController('Dal.controller.ApplyAction').showOverview(deviceId, issueId);
            }
        }
    },
     */
    getIssueStore: function () {
        return this.getStore('Mdc.store.device.IssuesAlarms');
    }
})