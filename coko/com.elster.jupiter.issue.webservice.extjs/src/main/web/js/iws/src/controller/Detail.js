/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Iws.store.Logs'
    ],

    models: [
        'Iws.model.Issue'
    ],

    views: [
        'Iws.view.Detail',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Iws.view.WebServiceDetails'
    ],

    webServiceLogStore: 'Iws.store.Logs',

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'page',
                selector: 'webservice-issue-detail'
            },
            {
                ref: 'detailForm',
                selector: 'webservice-issue-detail webservice-issue-detail-form'
            },
            {
                ref: 'commentsPanel',
                selector: 'webservice-issue-detail #webservice-issue-comments'
            },
            {
                ref: 'issueDetailForm',
                selector: 'webservice-issue-detail #issue-detail-form'
            },
            {
                ref: 'actionMenu',
                selector: 'webservice-issue-detail #issues-action-menu'
            }
        ];
        me.callParent(arguments);
    },

    itemUrl: '/api/isu/issues/',

    init: function () {
        this.control({
            'webservice-issue-detail #webservice-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'webservice-issue-detail #webservice-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'webservice-issue-detail #webservice-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'webservice-issue-detail #webservice-issue-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'webservice-issue-detail #webservice-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'webservice-issue-detail #webservice-issue-comments #issue-comment-cancel-editing-button': {
                click: this.hideEditCommentForm
            },
            'webservice-issue-detail #webservice-issue-comments #issue-comment-edit-button': {
                click: this.editComment
            },
            'webservice-issue-detail #webservice-issue-comments #issue-edit-comment-area': {
                change: this.validateEditCommentForm
            },
            'webservice-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            },
            'webservice-issue-detail #issue-timeline-view': {
                onClickLink: this.showProcesses
            },
            'webservice-issue-detail #issue-process-view': {
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
