Ext.define('Idv.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Idv.store.NonEstimatedDataStore'
    ],

    models: [
        'Idv.model.Issue'
    ],

    views: [
        'Idv.view.Detail',
        'Idv.view.NonEstimatedDataGrid',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    refs: [
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
            },
            'no-estimated-data-grid uni-actioncolumn': {
                viewData: function(record) {
                    var me = this;
                    if (record.get('registerId')) {
                        this.getController('Uni.controller.history.Router').getRoute('devices/device/registers/registerdata').forward({
                            mRID: me.getDetailForm().getRecord().get('device').serialNumber,
                            channelId: record.get('registerId')
                        });
                    } else if(record.get('channelId')) {
                        this.getController('Uni.controller.history.Router').getRoute('devices/device/channels/channelvalidationblocks').forward(
                            {
                                mRID: me.getDetailForm().getRecord().get('device').serialNumber,
                                channelId: record.get('channelId'),
                                issueId: record.getId()
                            },
                            {
                                validationBlock: record.get('startTime')
                            }
                        );
                    }
                }
            }
        });
    }
});