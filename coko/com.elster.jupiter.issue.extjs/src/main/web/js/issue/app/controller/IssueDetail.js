Ext.define('Isu.controller.IssueDetail', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.Issues',
        'Isu.store.IssueComments'
    ],

    views: [
        'workspace.issues.DetailOverview'
    ],

    refs: [
        {
            ref: 'detailPanel',
            selector: 'issue-detail-overview issue-detail'
        },
        {
            ref: 'commentsPanel',
            selector: 'issue-detail-overview issue-comments dataview'
        },
        {
            ref: 'commentForm',
            selector: 'issue-detail-overview issue-comments comment-add-form'
        },
        {
            ref: 'addCommentButton',
            selector: 'issue-detail-overview issue-comments button[action=add]'
        },
        {
            ref: 'sendCommentButton',
            selector: 'issue-detail-overview form button[action=send]'
        }
    ],

    init: function () {
        this.control({
            'issue-detail-overview issue-comments button[action=add]': {
                click: this.showCommentForm
            },
            'issue-detail-overview form button[action=cancel]': {
                click: this.hideCommentForm
            },
            'issue-detail-overview form button[action=send]': {
                click: this.addComment
            },
            'issue-detail-overview form textareafield': {
                change: this.validateCommentForm
            }
        });
    },

    showOverview: function (id, showCommentForm) {
        var self = this,
            widget = Ext.widget('issue-detail-overview'),
            issueDetailModel = self.getModel('Isu.model.Issues'),
            form = widget.down('issue-form');

        showCommentForm && self.showCommentForm();
        issueDetailModel.load(id, {
            success: function (record) {
                form.loadRecord(record);
                form.setTitle(record.get('title'));

                var store = record.comments();

                // todo: this is dirty solution, rewrite in to the more solid one
                store.getProxy().url = store.getProxy().url.replace('{issue_id}', record.getId());
                store.clearFilter();
                self.getCommentsPanel().bindStore(record.comments());
                store.proxy.url = '/api/isu/issue/' + id + '/comments';
                store.load();
                self.getApplication().fireEvent('changecontentevent', widget);
            },
            failure: function () {
                window.location.href = '#/workspace/datacollection/issues';
            }
        });
    },

    showCommentForm: function (btn) {
        this.getCommentForm().show();
        btn.hide();
    },

    hideCommentForm: function () {
        var form = this.getCommentForm(),
            button = this.getAddCommentButton();

        form.down('textareafield').reset();
        form.hide();
        button.show();
    },

    validateCommentForm: function (form, newValue) {
        var sendBtn = this.getSendCommentButton();
        sendBtn.setDisabled(!newValue.trim().length);
    },

    addComment: function () {
        var self = this,
            commentsPanel = self.getCommentsPanel(),
            commentsStore = commentsPanel.getStore()
        ;

        var store = commentsPanel.getStore();
        store.add(self.getCommentForm().getValues());
        store.sync();

        self.hideCommentForm();
    }
});