Ext.define('Isu.controller.IssueDetail', {
    extend: 'Ext.app.Controller',
    requires: [
        'Isu.privileges.Issue',
        'Idc.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses'
    ],

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Issues',
        'Bpm.monitorissueprocesses.store.IssueProcesses'
    ],

    showOverview: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            filter = me.getStore('Isu.store.Clipboard').get('latest-issues-filter'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            issueType = queryString.issueType,
            store = me.getStore('Isu.store.Issues'),
            processStore = me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses'),
            widgetXtype,
            issueModel,
            widget;

        processStore.getProxy().setUrl(id);
        processStore.load(function (records) {
        });

        if (store.getCount()) {
            var issueActualType = store.getById(parseInt(id)).get('issueType').uid;
            if (issueActualType != issueType) {
                queryString.issueType = issueActualType;
                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                issueType = issueActualType;
            }
        }

        if (issueType == 'datacollection') {
            widgetXtype = 'data-collection-issue-detail';
            issueModel = 'Idc.model.Issue';
        } else if (issueType == 'datavalidation') {
            widgetXtype = 'data-validation-issue-detail';
            issueModel = 'Idv.model.Issue';
        }

        widget = Ext.widget(widgetXtype, {
            router: router,
            issuesListLink: me.makeLinkToList(router)
        });
        me.widget = widget;
        me.getApplication().fireEvent('changecontentevent', widget);
        me.issueModel = issueModel;
        widget.setLoading(true);

        me.getModel(issueModel).load(id, {
            callback: function () {
                widget.setLoading(false);
            },
            success: function (record) {
                if (!widget.isDestroyed) {
                    Ext.getStore('Isu.store.Clipboard').set('issue', record);
                    me.getApplication().fireEvent('issueLoad', record);
                    Ext.suspendLayouts();
                    widget.down('#issue-detail-top-title').setTitle(record.get('title'));
                    widget.down('#issue-detail-form').loadRecord(record);
                    Ext.resumeLayouts(true);
                    widget.down('issues-action-menu').record = record;
                    me.loadComments(record);
                }
            },
            failure: function () {
                router.getRoute(router.currentRoute.replace('/view', '')).forward();
            }
        });
        if (issueType == 'datavalidation') {
            me.getApplication().on('issueLoad', function (rec) {
                var panel = widget.down('#no-estimated-data-panel');
                if (rec.raw.notEstimatedData && panel) {
                    var data = [],
                        store, validationBlocksWidget;

                    rec.raw.notEstimatedData.map(function (item) {
                        item.notEstimatedBlocks.map(function (block) {
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
                        validationBlocksWidget = Ext.widget('no-estimated-data-grid', {
                            store: store,
                            router: router,
                            issue: rec
                        });
                    } else {
                        validationBlocksWidget = Ext.widget('no-items-found-panel', {
                            title: Uni.I18n.translate('issues.validationBlocks.empty.title', 'ISU', 'No validation blocks are available'),
                            reasons: [
                                Uni.I18n.translate('issues.validationBlocks.empty.reason1', 'ISU', 'No open validation issues.')
                            ]
                        });
                    }

                    panel.removeAll();
                    panel.add(validationBlocksWidget);
                }
            }, me, {
                single: true
            });
        }
    },

    loadTimeline: function(commentsStore){
        var me = this,

            timelineView = this.widget ? this.widget.down('#issue-timeline-view') : this.getPage().down('#issue-timeline-view'),
            processView = this.widget ? this.widget.down('#issue-process-view') :this.getPage().down('#issue-process-view'),
            timelineStore = me.getStore('Idc.store.TimelineEntries'),
            procesStore = me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses'),
            router = me.getController('Uni.controller.history.Router'),
            data=[];

        timelineStore.data.clear();

        commentsStore.each(function(rec)
            {
                data.push({
                    user: rec.data.author.name,
                    actionText: Uni.I18n.translate('issue.workspace.datacollection.added.comment','ISU','added a comment'),
                    creationDate: rec.data.creationDate,
                    contentText: rec.data.splittedComments,
                });
            }
        );
        if(Isu.privileges.Issue.canViewProcesses()) {
            procesStore.each(function (rec) {
                    data.push({
                        user: rec.data.startedBy,
                        actionText: Uni.I18n.translate('issue.workspace.datacollection.processStarted', 'ISU', 'started a process'),
                        creationDate: rec.data.startDate,
                        contentText: rec.data.name,
                        forProcess: true,
                        processId: rec.data.processId,
                        status: ' (' + rec.data.statusDisplay + ')',
                    });
                }
            );
        }
        Ext.Array.each(data, function (item) {
            timelineStore.add(item);
        })

        timelineStore.sort('creationDate', 'DESC');
        timelineView.bindStore(timelineStore);
        timelineView.previousSibling('#no-issue-timeline').setVisible(timelineStore.data.items.length <= 0);

        if(!Isu.privileges.Issue.canViewProcesses()) return;

        procesStore.sort('startDate', 'DESC');
        processView.bindStore(procesStore);
        processView.previousSibling('#no-issue-processes').setVisible(procesStore.data.items.length <= 0);
    },

    makeLinkToList: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues').toLowerCase() + '</a>',
            filter = this.getStore('Isu.store.Clipboard').get('latest-issues-filter'),
            queryParams = filter ? filter : null;

        return Ext.String.format(link, router.getRoute('workspace/issues').buildUrl(null, queryParams));
    },

    loadComments: function (record) {
        var
            me = this,
            commentsView = this.widget ? this.widget.down('#issue-comments-view') : this.getPage().down('#issue-comments-view'),
            commentsStore = record.comments(),
            router = this.getController('Uni.controller.history.Router');

        commentsStore.getProxy().url = '/api/isu/issues/' + record.getId() + '/comments';
        commentsStore.sort('creationDate', 'DESC');
        commentsView.bindStore(commentsStore);
        commentsView.setLoading(true);
        commentsStore.load(function (records) {
            if (!commentsView.isDestroyed) {
                Ext.suspendLayouts();
                commentsStore.add(records);
                commentsView.show();
                commentsView.previousSibling('#no-issue-comments').setVisible(!records.length && !router.queryParams.addComment);
                commentsView.up('issue-comments').down('#issue-comments-add-comment-button').setVisible(records.length && !router.queryParams.addComment && Isu.privileges.Issue.canComment());
                me.loadTimeline(commentsStore);
                Ext.resumeLayouts(true);
                commentsView.setLoading(false);
            }
        });
        if (router.queryParams.addComment) {
            this.showCommentForm();
        }
    },

    showCommentForm: function () {
        var page = this.widget ? this.widget : this.getCommentsPanel();

        Ext.suspendLayouts();
        page.down('#issue-add-comment-form').show();
        page.down('#issue-add-comment-area').focus();
        page.down('#no-issue-comments').hide();
        page.down('#issue-comments-add-comment-button').hide();
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
                me.loadTimeline(commentsStore);
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
        actionModel.getProxy().url = '/api/isu/issues/' + issue.getId() + '/actions';
        actionModel.save({
            callback: function (model, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (responseText) {
                    if (responseText.data.actions[0].success) {
                        me.getApplication().fireEvent('acknowledge', responseText.data.actions[0].message);
                        me.getModel('Isu.model.Issue').load(issue.getId(), {
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