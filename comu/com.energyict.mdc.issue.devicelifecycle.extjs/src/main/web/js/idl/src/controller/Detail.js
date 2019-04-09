/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Idl.store.TransitionStore',
        'Mdc.store.Estimators'
    ],

    models: [
        'Idl.model.Issue',
        'Idl.model.DeviceChannelDataSaveEstimate'
    ],

    views: [
        'Idl.view.Detail',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.devicechannels.ReadingEstimationWindow'
    ],

    transitionStore: 'Idl.store.TransitionStore',

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'page',
                selector: 'device-lifecycle-issue-detail'
            },
            {
                ref: 'detailForm',
                selector: 'device-lifecycle-issue-detail device-lifecycle-issue-detail-form'
            },
            {
                ref: 'commentsPanel',
                selector: 'device-lifecycle-issue-detail #device-lifecycle-issue-comments'
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
                selector: 'device-lifecycle-issue-detail #issue-detail-form'
            },
            {
                ref: 'actionMenu',
                selector: 'device-lifecycle-issue-detail #issues-action-menu'
            }
        ];
        me.callParent(arguments);
    },

    itemUrl: '/api/isu/issues/',

    init: function () {
        this.control({
            'device-lifecycle-issue-detail #device-lifecycle-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'device-lifecycle-issue-detail #device-lifecycle-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'device-lifecycle-issue-detail #device-lifecycle-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'device-lifecycle-issue-detail #device-lifecycle-issue-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'device-lifecycle-issue-detail #device-lifecycle-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'device-lifecycle-issue-detail #device-lifecycle-issue-comments #issue-comment-cancel-editing-button': {
                click: this.hideEditCommentForm
            },
            'device-lifecycle-issue-detail #device-lifecycle-issue-comments #issue-comment-edit-button': {
                click: this.editComment
            },
            'device-lifecycle-issue-detail #device-lifecycle-issue-comments #issue-edit-comment-area': {
                change: this.validateEditCommentForm
            },
            'device-lifecycle-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            }

        });
    }
});