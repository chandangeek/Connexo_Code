/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.ManualIssueDetail', {
    extend: 'Isu.controller.IssueDetail',

    views: [
        'Isu.view.issues.ManualIssueDetail'
    ],

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'page',
                selector: 'manual-issue-detail'
            },
            {
                ref: 'detailForm',
                selector: 'manual-issue-detail manual-issue-detail-form'
            },
            {
                ref: 'commentsPanel',
                selector: 'manual-issue-detail #manual-issue-comments'
            },
            {
                ref: 'issueDetailForm',
                selector: 'manual-issue-detail #issue-detail-form'
            },
            {
                ref: 'actionMenu',
                selector: 'manual-issue-detail #issues-action-menu'
            }
        ];
        me.callParent(arguments);
    },

    init: function(){
            this.control({
                'manual-issue-detail #manual-issue-comments #issue-comments-add-comment-button': {
                    click: this.showCommentFormValidation
                },
                'manual-issue-detail #manual-issue-comments #empty-message-add-comment-button': {
                    click: this.showCommentFormValidation
                },
                'manual-issue-detail #manual-issue-comments #issue-comment-cancel-adding-button': {
                    click: this.hideCommentForm
                },
                'manual-issue-detail #manual-issue-comments #issue-comment-save-button': {
                    click: this.addCommentValidation
                },
                'manual-issue-detail #manual-issue-comments #issue-add-comment-area': {
                    change: this.validateCommentForm
                },
                'manual-issue-detail #manual-issue-comments #issue-comment-cancel-editing-button': {
                    click: this.hideEditCommentForm
                },
                'manual-issue-detail #manual-issue-comments #issue-comment-edit-button': {
                    click: this.editComment
                },
                'manual-issue-detail #manual-issue-comments #issue-edit-comment-area': {
                    change: this.validateEditCommentForm
                }
            });
    },
});