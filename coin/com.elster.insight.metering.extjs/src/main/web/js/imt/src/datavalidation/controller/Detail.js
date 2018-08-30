/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Imt.store.Estimators',
        'Imt.datavalidation.store.NonEstimatedDataStore'
    ],

    models: [
        'Imt.datavalidation.model.Issue'
    ],

    widgetXtype: 'data-validation-issue-detail',
    issueModel: 'Imt.datavalidation.model.Issue',
    itemUrl: '/api/isu/issues/',
    nonEstimatedDataStore: 'Imt.datavalidation.store.NonEstimatedDataStore',

    views: [

        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.purpose.view.ReadingEstimationWindow',
        'Imt.datavalidation.view.Detail',
        'Imt.datavalidation.view.IssueDetailTop',
        'Imt.datavalidation.view.UsagePointIssueActionMenu',
        'Imt.datavalidation.view.NonEstimatedDataGrid'

    ],

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'page',
                selector: 'data-validation-issue-detail'
            },
            {
                ref: 'detailForm',
                selector: 'data-validation-issue-detail data-validation-issue-detail-form'
            },
            {
                ref: 'commentsPanel',
                selector: 'data-validation-issue-detail #data-validation-issue-comments'
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
                selector: 'data-validation-issue-detail #issue-detail-form'
            },
            {
                ref: 'actionMenu',
                selector: 'data-validation-issue-detail #usagepoint-issue-detail-action-menu'
            }
        ];
        me.callParent(arguments);
    },


    init: function () {
        this.control({
            'data-validation-issue-detail #data-validation-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentFormValidation
            },
            'data-validation-issue-detail #data-validation-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentFormValidation
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-comment-save-button': {
                click: this.addCommentValidation
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-comment-cancel-editing-button': {
                click: this.hideEditCommentForm
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-comment-edit-button': {
                click: this.editComment
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-edit-comment-area': {
                change: this.validateEditCommentForm
            },
            'data-validation-issue-detail #usagepoint-issue-detail-action-menu': {
                click: this.chooseAction
            }
        });
    }
});