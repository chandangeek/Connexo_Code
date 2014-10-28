Ext.define('Isu.controller.IssueDetail', {
    extend: 'Ext.app.Controller',

    showOverview: function (id, issueModel, issuesStore, widgetXtype) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget(widgetXtype, {
                router: router
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        me.getModel(issueModel).load(id, {
            callback: function () {
                widget.setLoading(false);
            },
            success: function (record) {
                if (!widget.isDestroyed) {
                    me.getApplication().fireEvent('issueLoad', record);
                    widget.down('#issue-detail-top-title').setTitle(record.get('title'));
                    me.getDetailForm().loadRecord(record);
                    widget.down('#issue-detail-action-menu').record = record;
                    me.loadComments(record);
                    me.setNavigationButtons(record, me.getStore(issuesStore));
                }
            },
            failure: function () {
                router.getRoute(router.currentRoute.replace('/view', '')).forward();
            }
        });
    },

    loadComments: function (record) {
        var commentsView = this.getPage().down('#issue-comments-view'),
            commentsStore = record.comments();

        commentsStore.getProxy().url = record.getProxy().url + '/' + record.getId() + '/comments';
        commentsView.bindStore(commentsStore);
        commentsView.setLoading(true);
        commentsStore.load(function (records) {
            if (!commentsView.isDestroyed) {
                commentsStore.add(records);
                commentsView.setLoading(false);
                commentsView.previousSibling('#no-issue-comments').setVisible(!records.length);
            }
        });
        if (this.getController('Uni.controller.history.Router').queryParams.addComment) {
            this.showCommentForm();
        }
    },

    showCommentForm: function () {
        var commentsPanel = this.getCommentsPanel();

        commentsPanel.down('#issue-add-comment-form').show();
        commentsPanel.down('#issue-add-comment-area').focus();
        commentsPanel.down('#issue-comments-add-comment-button').hide();
    },

    hideCommentForm: function () {
        var commentsPanel = this.getCommentsPanel();

        commentsPanel.down('#issue-add-comment-form').hide();
        commentsPanel.down('#issue-add-comment-area').reset();
        commentsPanel.down('#issue-comments-add-comment-button').show();
    },

    validateCommentForm: function (textarea, newValue) {
        this.getCommentsPanel().down('#issue-comment-save-button').setDisabled(!newValue.trim().length);
    },

    addComment: function () {
        var me = this,
            commentsPanel = me.getCommentsPanel(),
            commentsStore = commentsPanel.down('#issue-comments-view').getStore();

        commentsStore.add(commentsPanel.down('#issue-add-comment-form').getValues());
        commentsStore.sync();
        commentsPanel.down('#no-issue-comments').hide();
        me.hideCommentForm();
    },

    setNavigationButtons: function (record, issuesStore) {
        var me = this,
            navigationPanel = this.getNavigation(),
            prevBtn = navigationPanel.down('[action=prev]'),
            nextBtn = navigationPanel.down('[action=next]'),
            currentIndex = issuesStore.indexOf(record),
            router = this.getController('Uni.controller.history.Router'),
            prevIndex,
            nextIndex;

        if (currentIndex !== -1) {
            currentIndex && (prevIndex = currentIndex - 1);
            (issuesStore.getCount() > (currentIndex + 1)) && (nextIndex = currentIndex + 1);

            if (prevIndex || prevIndex == 0) {
                prevBtn.setHref(router.getRoute(router.currentRoute).buildUrl({issueId: issuesStore.getAt(prevIndex).getId()}));
                prevBtn.on('click', function () {
                    router.getRoute(router.currentRoute).forward({issueId: issuesStore.getAt(prevIndex).getId()});
                }, me, {single: true});
            } else {
                prevBtn.setDisabled(true);
            }

            if (nextIndex) {
                nextBtn.setHref(router.getRoute(router.currentRoute).buildUrl({issueId: issuesStore.getAt(nextIndex).getId()}));
                nextBtn.on('click', function () {
                    router.getRoute(router.currentRoute).forward({issueId: issuesStore.getAt(nextIndex).getId()});
                }, me, {single: true});
            } else {
                nextBtn.setDisabled(true);
            }

            navigationPanel.show();
        } else {
            navigationPanel.hide();
        }
    },

    chooseAction: function (menu, menuItem) {
        var router = this.getController('Uni.controller.history.Router');

        if (Ext.isEmpty(menuItem.actionRecord.get('parameters'))) {
            this.applyActionImmediately(menu.record, menuItem.actionRecord);
        } else {
            router.getRoute(router.currentRoute + '/action').forward({issueId: menu.record.getId(), actionId: menuItem.actionRecord.getId()});
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