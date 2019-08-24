/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Isc.store.Logs'
    ],

    models: [
        'Isc.model.Issue'
    ],

    views: [
        'Isc.view.Detail',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Isc.view.ServiceCallDetails'
    ],

    serviceCallLogStore: 'Isc.store.Logs',

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'page',
                selector: 'servicecall-issue-detail'
            },
            {
                ref: 'detailForm',
                selector: 'servicecall-issue-detail servicecall-issue-detail-form'
            },
            {
                ref: 'commentsPanel',
                selector: 'servicecall-issue-detail #servicecall-issue-comments'
            },
            {
                ref: 'issueDetailForm',
                selector: 'servicecall-issue-detail #issue-detail-form'
            },
            {
                ref: 'actionMenu',
                selector: 'servicecall-issue-detail #issues-action-menu'
            }
        ];
        me.callParent(arguments);
    },

    itemUrl: '/api/isu/issues/',

    init: function () {
        this.control({
            'servicecall-issue-detail #servicecall-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'servicecall-issue-detail #servicecall-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'servicecall-issue-detail #servicecall-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'servicecall-issue-detail #servicecall-issue-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'servicecall-issue-detail #servicecall-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'servicecall-issue-detail #servicecall-issue-comments #issue-comment-cancel-editing-button': {
                click: this.hideEditCommentForm
            },
            'servicecall-issue-detail #servicecall-issue-comments #issue-comment-edit-button': {
                click: this.editComment
            },
            'servicecall-issue-detail #servicecall-issue-comments #issue-edit-comment-area': {
                change: this.validateEditCommentForm
            },
            'servicecall-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            },
            'servicecall-issue-detail #issue-timeline-view': {
                onClickLink: this.showProcesses
            },
            'servicecall-issue-detail #issue-process-view': {
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
