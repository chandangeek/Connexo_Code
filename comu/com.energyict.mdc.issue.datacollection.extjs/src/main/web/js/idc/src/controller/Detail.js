/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',
    controllers: [
        'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses'
    ],
    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Idc.store.CommunicationLogs',
        'Idc.store.ConnectionLogs',
        'Idc.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses',
        'Idc.store.Gateways'
    ],

    models: [
        'Idc.model.Issue'
    ],

    views: [
        'Idc.view.Detail',
        'Idc.view.CommunicationIssueDetailsForm',
        'Idc.view.InboundIssueDetailsForm',
        'Idc.view.OutboundIssueDetailsForm',
        'Idc.view.ConnectionIssueDetailsForm',
        'Idc.view.DefaultIssueDetailsForm',
        'Idc.view.MeterRegistrationIssueDetailsForm'
    ],

    constructor: function () {
        var me = this;
        me.refs = [
            {
                ref: 'page',
                selector: 'data-collection-issue-detail'
            },
            {
                ref: 'commentsPanel',
                selector: 'data-collection-issue-detail #data-collection-issue-comments'
            },
            {
                ref: 'issueDetailForm',
                selector: 'data-collection-issue-detail #issue-detail-form'
            },
            {
                ref: 'actionMenu',
                selector: 'data-collection-issue-detail #issue-detail-action-menu'
            }
        ]
        me.callParent(arguments);
    },


    init: function () {
        this.control({
            'data-collection-issue-detail #data-collection-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'data-collection-issue-detail #data-collection-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-comment-cancel-editing-button': {
                click: this.hideEditCommentForm
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-comment-edit-button': {
                click: this.editComment
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-edit-comment-area': {
                change: this.validateEditCommentForm
            },
            'data-collection-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            },
            'data-collection-issue-detail #issue-timeline-view': {
                onClickLink: this.showProcesses
            },
            'data-collection-issue-detail #issue-process-view': {
                onClickLink: this.showProcesses,
                onClickTaskLink: this.showTask
            }
        });
    },


    showProcesses: function(processId){
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute(router.currentRoute + '/viewProcesses');
        route.params.process = processId;
        route.forward(router.arguments, router.queryParams);

    },
    showTask: function(task){
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            route;
        router.arguments.taskId = task;
        route = 'workspace/tasks/task/performTask';
        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    }
});