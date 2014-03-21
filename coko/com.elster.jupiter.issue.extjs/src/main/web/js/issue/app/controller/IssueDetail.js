Ext.define('Isu.controller.IssueDetail', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
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
            selector: 'issue-detail-overview issue-comments'
        },
        {
            ref: 'commentForm',
            selector: 'issue-detail-overview form'
        },
        {
            ref: 'addCommentButton',
            selector: 'issue-detail-overview button[action=addcomment]'
        },
        {
            ref: 'sendCommentButton',
            selector: 'issue-detail-overview form button[action=send]'
        }
    ],

    init: function () {
        this.control({
            'issue-detail-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issue-detail-overview button[action=addcomment]': {
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

    showOverview: function (issueId, showCommentForm) {
        var self = this,
            widget = Ext.widget('issue-detail-overview'),
            issueDetailModel = self.getModel('Isu.model.Issues'),
            detailPanel = self.getDetailPanel();

        self.commentsAPI = '/api/isu/issue/' + issueId + '/comments';

        showCommentForm && self.showCommentForm();

        issueDetailModel.load(issueId, {
            success: function (record) {
                self.detailData = detailPanel.data = record.data;
                self.loadComments();
                self.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Workspace',
                href: '#/workspace'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issues',
                href: 'issues'
            }),
            breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issue detail'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    showCommentForm: function () {
        var form = this.getCommentForm(),
            button = this.getAddCommentButton();

        button.hide();
        form.show();
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

        if (newValue.replace(/\s*/g, '')) {
            sendBtn.setDisabled(false);
        } else {
            sendBtn.setDisabled(true);
        }
    },

    loadComments: function () {
        var self = this,
            commentsPanel = self.getCommentsPanel();

        commentsPanel.store.proxy.url = self.commentsAPI;
        commentsPanel.store.load();
    },

    addComment: function () {
        var self = this,
            commentsPanel = self.getCommentsPanel(),
            formPanel = self.getCommentForm(),
            form = formPanel.getForm();

        Ext.Ajax.request({
            url: self.commentsAPI,
            method: 'POST',
            jsonData: form.getValues(),
            success: function (response) {
                var data = Ext.JSON.decode(response.responseText).data,
                    newComment;

                !commentsPanel.getStore().getTotalCount() && commentsPanel.removeAll();
                newComment = commentsPanel.addcomment(data);
            }
        });

        self.hideCommentForm();
    }
});