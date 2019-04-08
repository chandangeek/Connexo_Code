/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Itk.store.OccurrenceStore',
        'Itk.store.RecurrentTaskStore'
    ],

    models: [
        'Itk.model.Issue'
    ],

    views: [
        'Itk.view.Detail',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    taskStore: 'Itk.store.OccurrenceStore',

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'page',
                selector: 'task-issue-detail'
            },
            {
                ref: 'detailForm',
                selector: 'task-issue-detail task-issue-detail-form'
            },
            {
                ref: 'commentsPanel',
                selector: 'task-issue-detail #task-issue-comments'
            },
            {
                ref: 'readingEstimationWindow',
                selector: '#reading-estimation-window'
            },
            {
                ref: 'noEstimatedDataGrid',
                selector: '#validation-no-estimated-data-grid'
            },
            {
                ref: 'issueDetailForm',
                selector: 'task-issue-detail #issue-detail-form'
            },
            {
                ref: 'actionMenu',
                selector: 'task-issue-detail #issues-action-menu'
            }
        ];
        me.callParent(arguments);
    },

    itemUrl: '/api/isu/issues/',

    init: function () {
        this.control({
            'task-issue-detail #task-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'task-issue-detail #task-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'task-issue-detail #task-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'task-issue-detail #task-issue-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'task-issue-detail #task-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'task-issue-detail #task-issue-comments #issue-comment-cancel-editing-button': {
                click: this.hideEditCommentForm
            },
            'task-issue-detail #task-issue-comments #issue-comment-edit-button': {
                click: this.editComment
            },
            'task-issue-detail #task-issue-comments #issue-edit-comment-area': {
                change: this.validateEditCommentForm
            },
            'task-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            }

        });
    }
});