Ext.define('Isu.controller.IssueDetail', {
    extend: 'Ext.app.Controller',
    requires: [
        'Isu.privileges.Issue'
    ],

    showOverview: function (id, issueModel, issuesStore, widgetXtype, routeToList, issueType) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filter = me.getStore('Isu.store.Clipboard').get(issueType + '-latest-issues-filter'),
            queryParams = filter ? {filter: filter} : null,
            widget = Ext.widget(widgetXtype, {
                router: router,
                issuesListLink: me.makeLinkToList(router, routeToList, issueType)
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        me.issueModel = issueModel;
        widget.setLoading(true);
        me.getModel(issueModel).load(id, {
            callback: function () {
                widget.setLoading(false);
            },
            success: function (record) {
                if (!widget.isDestroyed) {
                    me.getApplication().fireEvent('issueLoad', record);
                    Ext.suspendLayouts();
                    widget.down('#issue-detail-top-title').setTitle(record.get('title'));
                    me.getDetailForm().loadRecord(record);
                    Ext.resumeLayouts(true);
                    widget.down('issues-action-menu').record = record;
                    me.loadComments(record);

                    // todo: disabled due to bug in data validation issues (link is hardcoded)
                    //if (queryParams) {
                    //var breadcrumbLink = Ext.ComponentQuery.query('breadcrumbLink[href=#/workspace/datacollectionissues]')[0];
                    //breadcrumbLink.getEl().down('a').set({href: router.getRoute(routeToList).buildUrl(null, queryParams)});
                    //}
                }
            },
            failure: function () {
                router.getRoute(router.currentRoute.replace('/view', '')).forward();
            }
        });
    },

    makeLinkToList: function (router, routeToList, issueType) {
        var link = '<a href="{0}">' + Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues').toLowerCase() + '</a>',
            filter = this.getStore('Isu.store.Clipboard').get(issueType + '-latest-issues-filter'),
            queryParams = filter ? filter : null;

        return Ext.String.format(link, router.getRoute(routeToList).buildUrl(null, queryParams));
    },

    loadComments: function (record) {
        var commentsView = this.getPage().down('#issue-comments-view'),
            commentsStore = record.comments(),
            router = this.getController('Uni.controller.history.Router');

        commentsStore.getProxy().url = record.getProxy().url + '/' + record.getId() + '/comments';
        commentsView.bindStore(commentsStore);
        commentsView.setLoading(true);
        commentsStore.load(function (records) {
            if (!commentsView.isDestroyed) {
                Ext.suspendLayouts();
                commentsStore.add(records);
                commentsView.show();
                commentsView.previousSibling('#no-issue-comments').setVisible(!records.length && !router.queryParams.addComment);
                commentsView.up('issue-comments').down('#issue-comments-add-comment-button').setVisible(records.length && !router.queryParams.addComment && Isu.privileges.Issue.canComment());
                Ext.resumeLayouts(true);
                commentsView.setLoading(false);
            }
        });
        if (router.queryParams.addComment) {
            this.showCommentForm();
        }
    },

    showCommentForm: function () {
        var commentsPanel = this.getCommentsPanel();

        Ext.suspendLayouts();
        commentsPanel.down('#issue-add-comment-form').show();
        commentsPanel.down('#issue-add-comment-area').focus();
        commentsPanel.down('#no-issue-comments').hide();
        commentsPanel.down('#issue-comments-add-comment-button').hide();
        Ext.resumeLayouts(true);
    },

    hideCommentForm: function () {
        var commentsPanel = this.getCommentsPanel(),
            hasComments = commentsPanel.down('#issue-comments-view').getStore().getCount() ? true : false;

        Ext.suspendLayouts();
        commentsPanel.down('#issue-add-comment-form').hide();
        commentsPanel.down('#issue-add-comment-area').reset();
        commentsPanel.down('#issue-comments-add-comment-button').setVisible(hasComments && Isu.privileges.Issue.canComment());
        commentsPanel.down('#no-issue-comments').setVisible(!hasComments);
        Ext.resumeLayouts(true);
    },

    validateCommentForm: function (textarea, newValue) {
        this.getCommentsPanel().down('#issue-comment-save-button').setDisabled(!newValue.trim().length);
    },

    addComment: function () {
        var me = this,
            commentsPanel = me.getCommentsPanel(),
            commentsView = commentsPanel.down('#issue-comments-view'),
            commentsStore = commentsView.getStore();

        commentsView.setLoading();
        commentsStore.add(commentsPanel.down('#issue-add-comment-form').getValues());
        commentsStore.sync({callback: function () {
            commentsStore.load(function (records) {
                this.add(records);
                commentsView.setLoading(false);
            })
        }});

        me.hideCommentForm();
    },

    chooseAction: function (menu, menuItem) {
        if (!Ext.isEmpty(menuItem.actionRecord)) {
            this.applyActionImmediately(menu.record, menuItem.actionRecord);
        }
    },

    applyActionImmediately: function (issue, action) {
        var me = this,
            actionModel = Ext.create(issue.actions().model);

        actionModel.setId(action.getId());
        actionModel.set('parameters', {});
        actionModel.getProxy().url = issue.getProxy().url + '/' + issue.getId() + '/actions';
        actionModel.save({
            callback: function (model, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (responseText) {
                    if (responseText.data.actions[0].success) {
                        me.getApplication().fireEvent('acknowledge', responseText.data.actions[0].message);
                        me.getModel(issue.$className).load(issue.getId(), {
                            success: function (record) {
                                var form = me.getDetailForm();

                                if (form) {
                                    form.loadRecord(record);
                                }
                            }
                        });
                    } else {
                        me.getApplication().getController('Uni.controller.Error').showError(model.get('name'), responseText.data.actions[0].message);
                    }
                }
            }
        });
    }
});