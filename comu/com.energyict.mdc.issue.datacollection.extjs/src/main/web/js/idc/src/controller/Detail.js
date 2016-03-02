Ext.define('Idc.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.Clipboard'
    ],

    models: [
        'Idc.model.Issue'
    ],

    views: [
        'Idc.view.Detail'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'data-collection-issue-detail'
        },
        {
            ref: 'detailForm',
            selector: 'data-collection-issue-detail #data-collection-issue-detail-form'
        },
        {
            ref: 'commentsPanel',
            selector: 'data-collection-issue-detail #data-collection-issue-comments'
        }
    ],

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
            'data-collection-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            }
        });
    }
});