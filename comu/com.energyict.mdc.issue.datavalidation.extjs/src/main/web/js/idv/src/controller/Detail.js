Ext.define('Idv.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Idv.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Idv.store.NonEstimatedDataStore'
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
    },

    showOverview: function (id) {
        var me = this,
            router = this.getController('Uni.controller.history.Router');

        me.callParent([id, 'Idv.model.Issue', 'Idv.store.Issues', 'data-validation-issue-detail', 'workspace/datavalidationissues', 'datavalidation']);
        me.getApplication().on('issueLoad', function(record) {
            if (record.raw.notEstimatedData) {
                var data = [],
                    panel = me.getPage().getCenterContainer().down('#no-estimated-data-panel'),
                    store, widget;

                record.raw.notEstimatedData.map(function(item) {
                    item.notEstimatedBlocks.map(function(block) {
                        data.push(Ext.apply({}, {
                            mRID: item.readingType.mRID,
                            channelId: item.channelId,
                            registerId: item.registerId,
                            readingType: item.readingType
                        }, block))
                    });
                });

                if (data.length) {
                    store = Ext.create('Idv.store.NonEstimatedDataStore', {data: data});
                    widget = Ext.widget('no-estimated-data-grid', {store: store, router: router, issue: record});
                } else {
                    widget = Ext.widget('no-items-found-panel', {
                        title: Uni.I18n.translate('issues.validationBlocks.empty.title', 'IDV', 'No validation blocks are available'),
                        reasons: [
                            Uni.I18n.translate('issues.validationBlocks.empty.reason1', 'IDV', 'No open validation issues.')
                        ]
                    });
                }

                panel.removeAll();
                panel.add(widget);
            }
        });
    }
});