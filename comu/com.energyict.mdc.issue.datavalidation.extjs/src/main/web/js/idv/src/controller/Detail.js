Ext.define('Idv.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Idv.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.Clipboard'
    ],

    views: [
        'Idv.view.Detail'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'data-validation-issue-detail'
        },
        {
            ref: 'detailForm',
            selector: 'data-validation-issue-detail #data-validation-issue-detail-form'
        },
        {
            ref: 'commentsPanel',
            selector: 'data-validation-issue-detail #data-validation-issue-comments'
        }
    ],

    init: function () {
        this.control({
            'data-validation-issue-detail #data-validation-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'data-validation-issue-detail #data-validation-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'data-validation-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showOverview: function (id) {
        this.callParent([id, 'Idv.model.Issue', 'Idv.store.Issues', 'data-validation-issue-detail', 'workspace/datavalidationissues', 'datavalidation']);
    }
});