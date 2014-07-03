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
            ref: 'page',
            selector: 'issue-detail-overview'
        },
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
            ref: 'commentInput',
            selector: 'issue-detail-overview issue-comments comment-add-form textareafield'
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

                store.proxy.url = '/api/isu/issue/' + id + '/comments';
                store.load();
                self.getApplication().fireEvent('changecontentevent', widget);
                self.getCommentsPanel().bindStore(store);
                self.setNavigationButtons(record);
            },
            failure: function () {
                window.location.href = '#/workspace/datacollection/issues';
            }
        });
    },

    showCommentForm: function (btn) {
        this.getCommentForm().show();
        this.getCommentInput().focus();
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
    },

    setNavigationButtons: function (model) {
        var prevBtn = this.getPage().down('[action=prev]'),
            nextBtn = this.getPage().down('[action=next]'),
            issueStore = this.getStore('Isu.store.Issues'),
            currentIndex = issueStore.indexOf(model),
            router = this.getController('Uni.controller.history.Router'),
            prevIndex,
            nextIndex;

        if (currentIndex != -1) {
            currentIndex && (prevIndex = currentIndex - 1);
            (issueStore.getCount() > (currentIndex + 1)) && (nextIndex = currentIndex + 1);

            if (prevIndex || prevIndex == 0) {
                prevBtn.on('click', function () {
                    router.getRoute('workspace/datacollection/issues/view').forward({id: issueStore.getAt(prevIndex).getId()});
                });
            } else {
                prevBtn.setDisabled(true);
            }

            if (nextIndex) {
                nextBtn.on('click', function () {
                    router.getRoute('workspace/datacollection/issues/view').forward({id: issueStore.getAt(nextIndex).getId()});
                });
            } else {
                nextBtn.setDisabled(true);
            }

            prevBtn.show();
            nextBtn.show();
        } else {
            prevBtn.hide();
            nextBtn.hide();
        }
    }
});